package consulo.blade.impl.psi;

import com.jetbrains.php.blade.psi.BladePsiEcho;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import consulo.language.ast.ASTNode;

public final class BladePsiEchoImpl extends BladePsiHostImpl implements BladePsiEcho {
    public BladePsiEchoImpl(ASTNode node) {
        super(node, BladeTokenTypes.TEXT_BLOCK_CONTENT);
    }
}
