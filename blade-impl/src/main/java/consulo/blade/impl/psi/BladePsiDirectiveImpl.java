package consulo.blade.impl.psi;

import com.jetbrains.php.blade.psi.BladePsiDirective;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import org.jspecify.annotations.Nullable;

public final class BladePsiDirectiveImpl extends ASTWrapperPsiElement implements BladePsiDirective {
    public BladePsiDirectiveImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return java.util.Objects.requireNonNull(getFirstChild()).getText();
    }

    public @Nullable BladePsiDirectiveParameter getParameter() {
        return findChildByClass(BladePsiDirectiveParameter.class);
    }

    public IElementType getDirectiveElementType() {
        return java.util.Objects.requireNonNull(getFirstChild()).getNode().getElementType();
    }
}
