package consulo.blade.impl.psi;

import com.jetbrains.php.blade.psi.BladePsiRawEcho;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import consulo.language.ast.ASTNode;

public final class BladePsiRawEchoImpl extends BladePsiHostImpl implements BladePsiRawEcho {
    public BladePsiRawEchoImpl(ASTNode node) {
        super(node, BladeTokenTypes.TEXT_BLOCK_CONTENT);
    }
}
