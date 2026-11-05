package consulo.blade.ide;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.psi.*;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.InjectingRecord;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.jetbrains.php.blade.psi.BladeTokenTypes.*;

@NullUnmarked
@ExtendWith(ConsuloProjectLoader.class)
public class BladeParserTest {
    public record Context(PsiFileFactory factory) implements InjectingRecord {
    }

    private BladeFileImpl parse(Context context, String text) {
        PsiFile file = context.factory().createFileFromText("example.blade.php", BladeLanguage.INSTANCE, text);
        assertInstanceOf(BladeFileImpl.class, file);
        assertEquals(text, file.getText());
        assertNull(PsiTreeUtil.findChildOfType(file, PsiErrorElement.class));
        return (BladeFileImpl) file;
    }

    @Test
    public void directivePairsStayFlat(Context context) {
        var file = parse(context, "@component('card')@slot('title')Hi @endslot @endcomponent");
        var directives = PsiTreeUtil.findChildrenOfType(file, BladePsiDirective.class);
        assertEquals(4, directives.size());
        for (var directive : directives) {
            assertSame(file, directive.getParent());
        }
        assertEquals(
            List.of("@component", "@slot", "@endslot", "@endcomponent"),
            directives.stream().map(BladePsiDirective::getName).toList()
        );
    }

    @Test
    public void parameterImmediatelyFollowsKeyword(Context context) {
        var file = parse(context, "@section \n ('content')");
        var directive = (BladePsiDirective) file.getFirstChild();
        var keyword = directive.getFirstChild();
        var parameter = directive.getParameter();
        assertInstanceOf(LeafPsiElement.class, keyword);
        assertSame(SECTION_DIRECTIVE, keyword.getNode().getElementType());
        assertSame(parameter, keyword.getNextSibling());
        assertSame(keyword, parameter.getPrevSibling());
        assertEquals(" \n ", parameter.getFirstChild().getText());
        assertEquals("'content'", parameter.getParameterText());
        assertSame(SECTION_DIRECTIVE, parameter.getDirectiveElementType());
        assertEquals(" \n ('content')", parameter.getText());
        assertEquals("'content'", parameter.getContentRange().substring(parameter.getText()));
    }

    @Test
    public void emptyAndCustomParameters(Context context) {
        var file = parse(context, "@datetime()@foo($d)");
        var first = (BladePsiDirective) file.getFirstChild();
        assertEquals("", first.getParameter().getParameterText());
        assertEquals(2, first.getParameter().getNode().getChildren(null).length);
        assertSame(CUSTOM_DIRECTIVE, first.getDirectiveElementType());
    }

    @Test
    public void echoesAndPhpAreHosts(Context context) {
        var file = parse(context, "{{ $x }}{!! $html !!}@php $a = 1; @endphp @php($b = 2)");
        var children = java.util.Arrays.stream(file.getChildren())
            .filter(e -> e instanceof BladePsiHost || e instanceof BladePsiDirective)
            .toArray(PsiElement[]::new);
        assertInstanceOf(BladePsiEcho.class, children[0]);
        assertInstanceOf(BladePsiRawEcho.class, children[1]);
        assertInstanceOf(BladePsiPhpBlock.class, children[2]);
        assertInstanceOf(BladePsiDirective.class, children[3]);
        assertEquals(" $x ", ((BladePsiEcho) children[0]).getContentRange().substring(children[0].getText()));
        assertSame(TEXT_BLOCK_CONTENT, children[0].getFirstChild().getNextSibling().getNode().getElementType());
        assertSame(PHP_CONTENT, children[2].getFirstChild().getNextSibling().getNode().getElementType());
    }

    @Test
    public void commentIsPsiComment(Context context) {
        var file = parse(context, "{{-- hello --}}");
        assertInstanceOf(PsiComment.class, file.getFirstChild());
        assertSame(BLADE_COMMENT, file.getFirstChild().getNode().getElementType());
    }

    @Test
    public void htmlAndFindElementAt(Context context) {
        String text = "<div>@slot('title')</div>";
        var file = parse(context, text);
        assertSame(HTML_TEMPLATE, file.getFirstChild().getNode().getElementType());
        var leaf = file.findElementAt(text.indexOf("title") + 2);
        assertSame(DIRECTIVE_PARAMETER_CONTENT, leaf.getNode().getElementType());
        assertInstanceOf(BladePsiDirectiveParameter.class, leaf.getParent());
    }

    @Test
    public void incompleteInputIsPreserved(Context context) {
        for (String text : List.of("@if(", "{{", "{!! $x", "@php $x", "@section ('a'")) {
            parse(context, text);
        }
    }

    @Test
    public void laravelConsumerFixtures(Context context) throws java.io.IOException {
        for (String name : List.of("includes", "each", "component", "slot")) {
            try (var stream = getClass().getResourceAsStream("/consumers/" + name + ".blade.php")) {
                assertNotNull(stream);
                var file = parse(context, new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
                var directives = PsiTreeUtil.findChildrenOfType(file, BladePsiDirective.class);
                if (name.equals("includes")) {
                    assertEquals(7, directives.size());
                }
                if (name.equals("each")) {
                    assertEquals(4, directives.size());
                }
                for (var directive : directives) {
                    assertSame(file, directive.getParent());
                }
            }
        }
    }
}
