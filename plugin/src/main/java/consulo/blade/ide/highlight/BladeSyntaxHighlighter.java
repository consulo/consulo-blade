package consulo.blade.ide.highlight;

import com.jetbrains.php.blade.lexer.BladeLexer;
import com.jetbrains.php.blade.psi.BladeDirectiveElementType;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import static com.jetbrains.php.blade.psi.BladeTokenTypes.*;
import static consulo.blade.ide.highlight.BladeHighlighterColors.*;

public final class BladeSyntaxHighlighter extends SyntaxHighlighterBase {
    @Override
    public Lexer getHighlightingLexer() {
        return new BladeLexer();
    }

    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType type) {
        if (type instanceof BladeDirectiveElementType || type == CUSTOM_DIRECTIVE) {
            return pack(DIRECTIVE);
        }
        if (type == ECHO_START || type == ECHO_END) {
            return pack(ECHO_BRACES);
        }
        if (type == RAW_ECHO_START || type == RAW_ECHO_END) {
            return pack(RAW_ECHO_BRACES);
        }
        if (type == BLADE_COMMENT) {
            return pack(COMMENT);
        }
        if (type == DIRECTIVE_PARAMETER_START || type == DIRECTIVE_PARAMETER_END) {
            return pack(PARAMETER_BRACES);
        }
        if (type == TEXT_BLOCK_CONTENT || type == PHP_CONTENT || type == DIRECTIVE_PARAMETER_CONTENT) {
            return pack(PHP_BACKGROUND);
        }
        return EMPTY;
    }
}
