package com.jetbrains.php.blade.lexer;

import com.jetbrains.php.blade.psi.BladeTokenTypes;
import consulo.blade.BladeDirectives;
import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import java.util.Map;
import static com.jetbrains.php.blade.psi.BladeTokenTypes.*;

%%
%class _BladeLexer
%extends LexerBase
%public
%unicode
%function advanceImpl
%type IElementType
%eof{ return;
%eof}
%state ST_PARAMETER_START ST_PARAMETER ST_PARAMETER_END ST_ECHO ST_RAW_ECHO ST_PHP_BLOCK ST_PHP_END ST_VERBATIM
%{
    private Map<String, BladeDirectives.Definition> directives;

    public _BladeLexer() { this(BladeDirectives.snapshot()); }

    public _BladeLexer(Map<String, BladeDirectives.Definition> directives) {
        this.directives = Map.copyOf(directives);
    }

    private boolean at(int offset, String text) {
        if (offset + text.length() > zzEndRead) return false;
        for (int i = 0; i < text.length(); i++) {
            if (zzBuffer.charAt(offset + i) != text.charAt(i)) return false;
        }
        return true;
    }

    private boolean word(char c) { return Character.isLetterOrDigit(c) || c == '_'; }

    private boolean directiveEnd(int offset, String name) {
        return at(offset, name) && (offset + name.length() == zzEndRead || !word(zzBuffer.charAt(offset + name.length())));
    }

    // Consume a whole PHP fragment as one leaf. Local depth and quote state are
    // rebuilt for every fragment, so lexer restarts never depend on earlier runs.
    private int contentEnd(int offset, String delimiter, boolean parameter) {
        int round = 0, square = 0, curly = 0;
        char quote = 0;
        boolean lineComment = false, blockComment = false;
        for (int i = offset; i < zzEndRead; i++) {
            char c = zzBuffer.charAt(i);
            if (lineComment) {
                if (c == '\n' || c == '\r') lineComment = false;
                continue;
            }
            if (blockComment) {
                if (at(i, "*/")) { blockComment = false; i++; }
                continue;
            }
            if (quote != 0) {
                if (c == '\\') { i++; continue; }
                if (c == quote) quote = 0;
                continue;
            }
            if (c == '\'' || c == '"' || c == '`') { quote = c; continue; }
            if (at(i, "/*")) { blockComment = true; i++; continue; }
            if (at(i, "//") || c == '#') { lineComment = true; continue; }
            if (round == 0 && square == 0 && curly == 0 && at(i, delimiter)) return i;
            if (parameter || !delimiter.startsWith("@")) {
                switch (c) {
                    case '(': round++; break;
                    case ')': if (round > 0) round--; break;
                    case '[': square++; break;
                    case ']': if (square > 0) square--; break;
                    case '{': curly++; break;
                    case '}': if (curly > 0) curly--; break;
                }
            }
        }
        return zzEndRead;
    }

    private IElementType directive() {
        int start = zzStartRead;
        if (start > 0 && word(zzBuffer.charAt(start - 1))) return HTML_TEMPLATE;
        String name = yytext().toString().substring(1);
        BladeDirectives.Definition definition = directives.get(name);
        int next = zzMarkedPos;
        while (next < zzEndRead && Character.isWhitespace(zzBuffer.charAt(next))) next++;
        boolean parameter = next < zzEndRead && zzBuffer.charAt(next) == '(';
        // like Blade, an unknown @name is text: CSS at-rules such as @supports (…), mail addresses, Alpine attributes
        if (definition == null) return HTML_TEMPLATE;
        if (parameter) yybegin(ST_PARAMETER_START);
        else if (name.equals("php")) yybegin(ST_PHP_BLOCK);
        else if (name.equals("verbatim")) yybegin(ST_VERBATIM);
        return definition.tokenType();
    }
%}
IDENTIFIER=[a-zA-Z_][a-zA-Z_0-9]*
WHITESPACE=[ \t\r\n\f]+
%%
<YYINITIAL> {
    "{{--" {
        int end = zzMarkedPos;
        while (end < zzEndRead && !at(end, "--}}")) end++;
        zzMarkedPos = Math.min(zzEndRead, end + 4);
        return BLADE_COMMENT;
    }
    "@{{" {
        int end = contentEnd(zzMarkedPos, "}}", false);
        zzMarkedPos = Math.min(zzEndRead, end + 2);
        return HTML_TEMPLATE;
    }
    "@@"{IDENTIFIER}? { return HTML_TEMPLATE; }
    "{{" { yybegin(ST_ECHO); return ECHO_START; }
    "{!!" { yybegin(ST_RAW_ECHO); return RAW_ECHO_START; }
    "@"{IDENTIFIER} { return directive(); }
    [^] { return HTML_TEMPLATE; }
}
<ST_PARAMETER_START> {
    {WHITESPACE} { return WHITE_SPACE; }
    "(" { yybegin(ST_PARAMETER); return DIRECTIVE_PARAMETER_START; }
    [^] { yypushback(1); yybegin(YYINITIAL); }
}
<ST_PARAMETER> {
    ")" { yybegin(YYINITIAL); return DIRECTIVE_PARAMETER_END; }
    [^] {
        zzMarkedPos = contentEnd(zzStartRead, ")", true);
        yybegin(ST_PARAMETER_END);
        return DIRECTIVE_PARAMETER_CONTENT;
    }
}
<ST_PARAMETER_END> {
    ")" { yybegin(YYINITIAL); return DIRECTIVE_PARAMETER_END; }
    [^] { yypushback(1); yybegin(YYINITIAL); }
}
<ST_ECHO> {
    "}}" { yybegin(YYINITIAL); return ECHO_END; }
    [^] {
        zzMarkedPos = contentEnd(zzStartRead, "}}", false);
        return TEXT_BLOCK_CONTENT;
    }
}
<ST_RAW_ECHO> {
    "!!}" { yybegin(YYINITIAL); return RAW_ECHO_END; }
    [^] {
        zzMarkedPos = contentEnd(zzStartRead, "!!}", false);
        return TEXT_BLOCK_CONTENT;
    }
}
<ST_PHP_BLOCK> {
    "@endphp" {
        if (directiveEnd(zzStartRead, "@endphp")) { yybegin(YYINITIAL); return ENDPHP_DIRECTIVE; }
        yypushback(yylength());
        zzMarkedPos = contentEnd(zzStartRead + 1, "@endphp", false);
        yybegin(ST_PHP_END);
        return PHP_CONTENT;
    }
    [^] {
        int end = contentEnd(zzStartRead, "@endphp", false);
        // @endphpSuffix is PHP content, not the closing directive.
        while (end < zzEndRead && !directiveEnd(end, "@endphp")) {
            end = contentEnd(end + 1, "@endphp", false);
        }
        zzMarkedPos = end;
        yybegin(ST_PHP_END);
        return PHP_CONTENT;
    }
}
<ST_PHP_END> {
    "@endphp" { yybegin(YYINITIAL); return ENDPHP_DIRECTIVE; }
    [^] { yypushback(1); yybegin(YYINITIAL); }
}
<ST_VERBATIM> {
    "@endverbatim" {
        if (directiveEnd(zzStartRead, "@endverbatim")) {
            yybegin(YYINITIAL); return ENDVERBATIM_DIRECTIVE;
        }
        return HTML_TEMPLATE;
    }
    [^] { return HTML_TEMPLATE; }
}
