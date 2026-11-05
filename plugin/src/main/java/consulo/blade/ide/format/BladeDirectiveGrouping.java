package consulo.blade.ide.format;

import com.jetbrains.php.blade.psi.BladeFileImpl;
import com.jetbrains.php.blade.psi.BladePsiDirective;
import consulo.blade.BladeDirectivePairs;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.ASTBlock;
import consulo.language.codeStyle.Block;
import consulo.language.psi.PsiElement;
import consulo.xml.language.psi.XmlElementType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Groups the blocks of one level, merged from HTML and Blade blocks, into {@link BladeDirectiveBlock}s.
 * <p>
 * The PSI keeps paired directives as siblings, and HTML text nodes hold the Blade constructs inside them: an
 * {@code @if} can be in one text node and its {@code @endif} in another. So text nodes with Blade blocks are replaced by
 * their sub-blocks first, then each pair whose closing directive is on the same level becomes one block.
 */
final class BladeDirectiveGrouping {
    private BladeDirectiveGrouping() {
    }

    static List<Block> group(List<Block> blocks) {
        List<Block> flat = flattenTextWithBlade(blocks);
        if (flat.stream().noneMatch(block -> block instanceof BladeLeafBlock)) {
            return blocks;
        }
        return groupPairs(flat);
    }

    private static List<Block> flattenTextWithBlade(List<Block> blocks) {
        List<Block> result = new ArrayList<>(blocks.size());
        for (Block block : blocks) {
            if (isTextWithBlade(block)) {
                result.addAll(block.getSubBlocks());
            }
            else {
                result.add(block);
            }
        }
        return result;
    }

    private static boolean isTextWithBlade(Block block) {
        if (!(block instanceof ASTBlock astBlock) || block.isLeaf()) {
            return false;
        }
        ASTNode node = astBlock.getNode();
        if (node == null || node.getElementType() != XmlElementType.XML_TEXT) {
            return false;
        }
        for (Block child : block.getSubBlocks()) {
            if (child instanceof BladeLeafBlock || child instanceof BladeDirectiveBlock) {
                return true;
            }
        }
        return false;
    }

    private static List<Block> groupPairs(List<Block> blocks) {
        List<Block> result = new ArrayList<>(blocks.size());
        int index = 0;
        while (index < blocks.size()) {
            Block block = blocks.get(index);
            BladeDirectivePairs.Pair pair = openedPair(block);
            int closingIndex = pair == null || pair.closing() == null ? -1 : indexOf(blocks, pair.closing(), index + 1);
            // a pair whose closing directive is on another level, for example inside an HTML element, stays flat
            if (pair == null || pair.closing() != null && closingIndex < 0) {
                result.add(block);
                index++;
                continue;
            }
            boolean closed = closingIndex >= 0;
            int end = closed ? closingIndex : blocks.size() - 1;
            result.add(createPairBlock(blocks.subList(index, end + 1), pair, closed));
            index = end + 1;
        }
        return result;
    }

    private static Block createPairBlock(List<Block> blocks, BladeDirectivePairs.Pair pair, boolean closed) {
        List<Block> children = new ArrayList<>();
        children.add(blocks.get(0));
        int contentEnd = closed ? blocks.size() - 1 : blocks.size();
        List<Block> section = new ArrayList<>();
        for (int index = 1; index < contentEnd; index++) {
            Block block = blocks.get(index);
            BladePsiDirective directive = directiveOf(block);
            if (directive != null && pair.branches().contains(directive)) {
                addSection(children, section);
                section = new ArrayList<>();
                children.add(block);
            }
            else {
                section.add(block);
            }
        }
        addSection(children, section);
        if (closed) {
            children.add(blocks.get(blocks.size() - 1));
        }
        return new BladeDirectiveBlock(children, closed);
    }

    private static void addSection(List<Block> children, List<Block> section) {
        if (!section.isEmpty()) {
            children.add(new BladeSectionBlock(groupPairs(section)));
        }
    }

    private static int indexOf(List<Block> blocks, BladePsiDirective directive, int from) {
        for (int index = from; index < blocks.size(); index++) {
            if (directiveOf(blocks.get(index)) == directive) {
                return index;
            }
        }
        return -1;
    }

    /**
     * The pair that {@code block} opens
     */
    private static BladeDirectivePairs.@Nullable Pair openedPair(Block block) {
        BladePsiDirective directive = directiveOf(block);
        if (directive == null || !(directive.getContainingFile() instanceof BladeFileImpl file)) {
            return null;
        }
        BladeDirectivePairs.Pair pair = BladeDirectivePairs.get(file).find(directive);
        return pair != null && pair.opening() == directive ? pair : null;
    }

    private static @Nullable BladePsiDirective directiveOf(Block block) {
        if (block instanceof BladeLeafBlock leaf) {
            PsiElement psi = leaf.getNode().getPsi();
            return psi instanceof BladePsiDirective directive ? directive : null;
        }
        return null;
    }
}
