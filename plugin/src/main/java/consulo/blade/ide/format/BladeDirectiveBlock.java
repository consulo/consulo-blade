package consulo.blade.ide.format;

import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.ChildAttributes;
import consulo.language.codeStyle.Indent;
import consulo.xml.template.formatter.CompositeTemplateBlock;

import java.util.List;

/**
 * A directive pair: the opening directive, the sections and branches ({@code @else}, {@code @case}, …) and the closing
 * directive. Without a closing directive the pair is incomplete, so a new line typed at its end goes into its content.
 */
final class BladeDirectiveBlock extends CompositeTemplateBlock {
    private final boolean myClosed;

    BladeDirectiveBlock(List<Block> subBlocks, boolean closed) {
        super(subBlocks);
        myClosed = closed;
    }

    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        boolean inContent = newChildIndex > 0 && (newChildIndex < getSubBlocks().size() || !myClosed);
        return new ChildAttributes(inContent ? Indent.getNormalIndent() : Indent.getNoneIndent(), null);
    }

    @Override
    public boolean isIncomplete() {
        return !myClosed;
    }
}
