package consulo.blade.ide.format;

import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.AbstractBlock;
import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.Indent;
import consulo.language.codeStyle.Spacing;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A Blade construct: a directive, an echo, a {@code @php} block or a comment. It keeps its text.
 */
final class BladeLeafBlock extends AbstractBlock {
    private final Indent myIndent;

    BladeLeafBlock(ASTNode node, Indent indent) {
        super(node, null, null);
        myIndent = indent;
    }

    @Override
    public Indent getIndent() {
        return myIndent;
    }

    @Override
    protected List<Block> buildChildren() {
        return EMPTY;
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block child1, Block child2) {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
}
