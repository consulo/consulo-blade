package consulo.blade.ide.format;

import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.ChildAttributes;
import consulo.language.codeStyle.Indent;
import consulo.xml.template.formatter.CompositeTemplateBlock;

import java.util.List;

/**
 * The blocks between two directives of a pair, one indent deeper than the directives
 */
final class BladeSectionBlock extends CompositeTemplateBlock {
    BladeSectionBlock(List<Block> subBlocks) {
        super(subBlocks);
    }

    @Override
    public Indent getIndent() {
        return Indent.getNormalIndent();
    }

    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        return new ChildAttributes(Indent.getNoneIndent(), null);
    }
}
