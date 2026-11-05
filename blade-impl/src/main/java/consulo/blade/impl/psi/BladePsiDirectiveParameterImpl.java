package consulo.blade.impl.psi;

import com.jetbrains.php.blade.psi.BladePsiDirective;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;

public final class BladePsiDirectiveParameterImpl extends BladePsiHostImpl implements BladePsiDirectiveParameter {
    public BladePsiDirectiveParameterImpl(ASTNode node) {
        super(node, BladeTokenTypes.DIRECTIVE_PARAMETER_CONTENT);
    }

    public String getParameterText() {
        return getContentRange().substring(getText());
    }

    public IElementType getDirectiveElementType() {
        return ((BladePsiDirective)java.util.Objects.requireNonNull(getParent())).getDirectiveElementType();
    }
}
