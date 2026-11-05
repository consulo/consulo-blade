package consulo.blade.impl.psi;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladePsiHost;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;

public abstract class BladePsiHostImpl extends ASTWrapperPsiElement implements BladePsiHost {
    private final IElementType contentType;

    protected BladePsiHostImpl(ASTNode node, IElementType contentType) {
        super(node);
        this.contentType = contentType;
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public TextRange getContentRange() {
        ASTNode content = getNode().findChildByType(contentType);
        if (content != null) {
            return TextRange.from(content.getStartOffset() - getTextRange().getStartOffset(), content.getTextLength());
        }
        ASTNode opening = getNode().getFirstChildNode();
        while (opening != null && opening.getElementType() == BladeTokenTypes.WHITE_SPACE) {
            opening = opening.getTreeNext();
        }
        int offset = opening == null
            ? getTextLength()
            : opening.getStartOffset() - getTextRange().getStartOffset() + opening.getTextLength();
        return TextRange.from(offset, 0);
    }

    @Override
    public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        LiteralTextEscaper<BladePsiHostImpl> delegate = LiteralTextEscaper.createSimple(this);
        return new LiteralTextEscaper<BladePsiHostImpl>(this) {
            @Override
            public boolean decode(TextRange range, StringBuilder output) {
                return delegate.decode(range, output);
            }

            @Override
            public int getOffsetInHost(int offset, TextRange range) {
                return delegate.getOffsetInHost(offset, range);
            }

            @Override
            public TextRange getRelevantTextRange() {
                return getContentRange();
            }

            @Override
            public boolean isOneLine() {
                return false;
            }
        };
    }

    @Override
    public PsiLanguageInjectionHost updateText(String text) {
        String source = this instanceof BladePsiDirectiveParameter ? "@if" + text : text;
        PsiFile file = PsiFileFactory.getInstance(getProject())
            .createFileFromText("replacement.blade.php", BladeLanguage.INSTANCE, source);
        BladePsiHostImpl replacement = PsiTreeUtil.findChildOfType(file, getClass());
        if (replacement == null) {
            throw new IllegalArgumentException("Invalid Blade injection host");
        }
        return (PsiLanguageInjectionHost) replace(replacement);
    }
}
