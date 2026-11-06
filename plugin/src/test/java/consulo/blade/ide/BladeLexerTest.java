package consulo.blade.ide;

import consulo.blade.BladeDirectives;
import com.jetbrains.php.blade.lexer.BladeLexer;
import consulo.language.lexer.Lexer;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@org.jspecify.annotations.NullUnmarked
@ExtendWith(ConsuloProjectLoader.class)
public class BladeLexerTest {
    private record Token(String type, String text, int offset, int state) {
    }

    private List<Token> lex(String text) {
        Lexer lexer = new BladeLexer(BladeDirectives.builtIns());
        lexer.start(text);
        List<Token> result = new ArrayList<>();
        int end = 0;
        while (lexer.getTokenType() != null) {
            assertEquals(end, lexer.getTokenStart());
            assertTrue(lexer.getTokenEnd() > end, "Lexer must advance");
            end = lexer.getTokenEnd();
            result.add(new Token(lexer.getTokenType().toString(), lexer.getTokenText(), lexer.getTokenStart(), lexer.getState()));
            lexer.advance();
        }
        assertEquals(text.length(), end);
        // Editor lexers must be able to resume at each token boundary.
        for (Token token : result) {
            lexer.start(text, token.offset(), text.length(), token.state());
            assertEquals(token.type(), lexer.getTokenType().toString());
            assertEquals(token.text(), lexer.getTokenText());
        }
        return result;
    }

    private void types(String text, String... expected) {
        assertEquals(List.of(expected), lex(text).stream().map(Token::type).toList());
    }

    @Test
    public void templateAndEscapes() {
        for (String text : List.of("foo@bar.com", "@@if($x)", "@{{ x }}", "<button @click=\"x\" @input=\"update\">", "@unknown")) {
            types(text, "HTML_TEMPLATE");
        }
    }

    /**
     * Like Blade, an unknown {@code @name(…)} is text, for example the CSS at-rules of the inline Tailwind CSS of a
     * Laravel welcome page
     */
    @Test
    public void unknownDirectivesAreText() {
        for (String text : List.of(
            "@supports (((-webkit-hyphens:none)) and (not (margin-trim:inline))) {a{b:c}}",
            "@media (min-width: 40rem) {a{b:c}}",
            "@container (width > 400px) {a{b:c}}",
            "@layer theme{:root{--x:1}}",
            "@scope (.card) {img{b:c}}",
            "@datetime($d)"
        )) {
            types(text, "HTML_TEMPLATE");
        }
    }

    @Test
    public void echoesSkipStringsAndNestedExpressions() {
        types("{{ '}}' . foo(['x' => '}']) }}", "ECHO_START", "TEXT_BLOCK_CONTENT", "ECHO_END");
        types("{!! \"!!}\" !!}", "RAW_ECHO_START", "TEXT_BLOCK_CONTENT", "RAW_ECHO_END");
        types("{{}}", "ECHO_START", "ECHO_END");
        types("{!! !!}", "RAW_ECHO_START", "TEXT_BLOCK_CONTENT", "RAW_ECHO_END");
    }

    @Test
    public void directiveParametersAreSingleLeaves() {
        var tokens = lex("@if \n (foo([1, fn() => ')'], (object) ['x' => 2]))");
        assertEquals(
            List.of(
                "IF_DIRECTIVE",
                "WHITE_SPACE",
                "DIRECTIVE_PARAMETER_START",
                "DIRECTIVE_PARAMETER_CONTENT",
                "DIRECTIVE_PARAMETER_END"
            ),
            tokens.stream().map(Token::type).toList()
        );
        assertEquals("foo([1, fn() => ')'], (object) ['x' => 2])", tokens.get(3).text());
        types("@section()", "SECTION_DIRECTIVE", "DIRECTIVE_PARAMETER_START", "DIRECTIVE_PARAMETER_END");
        types("@csrf @endif", "CSRF_DIRECTIVE", "HTML_TEMPLATE", "ENDIF_DIRECTIVE");
    }

    @Test
    public void phpAndVerbatim() {
        types("@php $x = '@endphp'; @endphp", "PHP_DIRECTIVE", "PHP_CONTENT", "ENDPHP_DIRECTIVE");
        types(
            "@php($x = 1)",
            "PHP_DIRECTIVE",
            "DIRECTIVE_PARAMETER_START",
            "DIRECTIVE_PARAMETER_CONTENT",
            "DIRECTIVE_PARAMETER_END"
        );
        types("@verbatim {{ $x }} @if(x) @endverbatim", "VERBATIM_DIRECTIVE", "HTML_TEMPLATE", "ENDVERBATIM_DIRECTIVE");
        types("@verbatim @endverbatimSuffix @endverbatim", "VERBATIM_DIRECTIVE", "HTML_TEMPLATE", "ENDVERBATIM_DIRECTIVE");
    }

    @Test
    public void commentsAndUnclosedFragments() {
        types("{{-- @if {{ ' --}}", "BLADE_COMMENT");
        types("{{-- unfinished", "BLADE_COMMENT");
        types("{{ 'unfinished", "ECHO_START", "TEXT_BLOCK_CONTENT");
        types("@if(foo(", "IF_DIRECTIVE", "DIRECTIVE_PARAMETER_START", "DIRECTIVE_PARAMETER_CONTENT");
        types("@php $x = 1;", "PHP_DIRECTIVE", "PHP_CONTENT");
    }

    @Test
    public void providerSnapshot() {
        var definitions = new LinkedHashMap<>(BladeDirectives.builtIns());
        var token = new com.jetbrains.php.blade.psi.BladeDirectiveElementType("LIVEWIRE_STYLES");
        definitions.put(
            "livewireStyles",
            new BladeDirectives.Definition("livewireStyles", token, BladeDirectives.Kind.INLINE, "")
        );
        Lexer lexer = new BladeLexer(Map.copyOf(definitions));
        lexer.start("@livewireStyles");
        assertSame(token, lexer.getTokenType());
        definitions.clear(); // The lexer retains its original snapshot.
        assertSame(token, lexer.getTokenType());
        assertEquals(BladeDirectives.Kind.OPEN, BladeDirectives.builtIns().get("foreach").kind());
        assertEquals("endPushOnce", BladeDirectives.builtIns().get("pushOnce").pair());
    }

    @Test
    public void allBuiltInDirectives() {
        for (var definition : BladeDirectives.builtIns().values()) {
            Lexer lexer = new BladeLexer(BladeDirectives.builtIns());
            lexer.start("@" + definition.name());
            assertSame(definition.tokenType(), lexer.getTokenType(), definition.name());
        }
        types("@php@endphpSuffix @endphp", "PHP_DIRECTIVE", "PHP_CONTENT", "ENDPHP_DIRECTIVE");
    }

    @Test
    public void phpCommentsDoNotCloseFragments() {
        types("{{ /* }} */ $x }}", "ECHO_START", "TEXT_BLOCK_CONTENT", "ECHO_END");
        types(
            "@if($x // )\n)",
            "IF_DIRECTIVE",
            "DIRECTIVE_PARAMETER_START",
            "DIRECTIVE_PARAMETER_CONTENT",
            "DIRECTIVE_PARAMETER_END"
        );
    }
}
