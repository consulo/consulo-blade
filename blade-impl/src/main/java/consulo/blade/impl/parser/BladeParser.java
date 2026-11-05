package consulo.blade.impl.parser;

import com.jetbrains.php.blade.psi.BladeDirectiveElementType;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiParser;
import consulo.language.parser.WhitespacesBinders;
import consulo.language.version.LanguageVersion;
import static com.jetbrains.php.blade.psi.BladeTokenTypes.*;
import static com.jetbrains.php.blade.parser.BladeElementTypes.*;

/**
 * Directive pairs remain siblings; editor features compute their pairing separately.
 */
public final class BladeParser implements PsiParser {
    @Override
    public ASTNode parse(IElementType root, PsiBuilder builder, LanguageVersion version) {
        PsiBuilder.Marker file = builder.mark();
        while (!builder.eof()) {
            IElementType type = builder.getTokenType();
            if (type == ECHO_START || type == RAW_ECHO_START) {
                block(
                    builder,
                    type == ECHO_START ? ECHO_END : RAW_ECHO_END,
                    type == ECHO_START ? TEXT_BLOCK : RAW_TEXT_BLOCK
                );
            }
            else if (type == PHP_DIRECTIVE && builder.lookAhead(1) != DIRECTIVE_PARAMETER_START) {
                block(builder, ENDPHP_DIRECTIVE, PHP_BLOCK);
            }
            else if (type instanceof BladeDirectiveElementType || type == CUSTOM_DIRECTIVE) {
                PsiBuilder.Marker directive = builder.mark();
                builder.advanceLexer();
                if (builder.getTokenType() == DIRECTIVE_PARAMETER_START) {
                    PsiBuilder.Marker parameter = builder.mark();
                    parameter.setCustomEdgeTokenBinders(WhitespacesBinders.GREEDY_LEFT_BINDER, null);
                    builder.advanceLexer();
                    if (builder.getTokenType() == DIRECTIVE_PARAMETER_CONTENT) {
                        builder.advanceLexer();
                    }
                    if (builder.getTokenType() == DIRECTIVE_PARAMETER_END) {
                        builder.advanceLexer();
                    }
                    parameter.done(DIRECTIVE_PARAMETER);
                }
                directive.done(DIRECTIVE);
            }
            else {
                builder.advanceLexer();
            }
        }
        file.done(root);
        return builder.getTreeBuilt();
    }

    private void block(PsiBuilder builder, IElementType end, IElementType composite) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (builder.getTokenType() == TEXT_BLOCK_CONTENT || builder.getTokenType() == PHP_CONTENT) {
            builder.advanceLexer();
        }
        if (builder.getTokenType() == end) {
            builder.advanceLexer();
        }
        marker.done(composite);
    }
}
