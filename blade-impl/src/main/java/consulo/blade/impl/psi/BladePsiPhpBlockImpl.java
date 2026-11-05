package consulo.blade.impl.psi;

import com.jetbrains.php.blade.psi.BladePsiPhpBlock;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import consulo.language.ast.ASTNode;

public final class BladePsiPhpBlockImpl extends BladePsiHostImpl implements BladePsiPhpBlock {
    public BladePsiPhpBlockImpl(ASTNode node) {
        super(node, BladeTokenTypes.PHP_CONTENT);
    }
}
