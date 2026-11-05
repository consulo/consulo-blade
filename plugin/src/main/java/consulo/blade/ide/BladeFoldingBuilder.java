package consulo.blade.ide;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.blade.BladeDirectivePairs;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.editor.folding.FoldingBuilderEx;
import consulo.language.editor.folding.FoldingDescriptor;
import consulo.language.psi.PsiElement;

import java.util.*;

@ExtensionImpl
public final class BladeFoldingBuilder extends FoldingBuilderEx {
    @Override
    public Language getLanguage() {
        return BladeLanguage.INSTANCE;
    }

    @Override
    public FoldingDescriptor[] buildFoldRegions(PsiElement root, Document document, boolean quick) {
        if (!(root instanceof BladeFileImpl file)) {
            return FoldingDescriptor.EMPTY;
        }
        List<FoldingDescriptor> result = new ArrayList<>();
        for (var pair : BladeDirectivePairs.get(file).pairs()) {
            var closing = pair.closing();
            if (closing != null) {
                add(
                    result,
                    document,
                    pair.opening(),
                    new TextRange(pair.opening().getTextOffset(), closing.getTextRange().getEndOffset())
                );
            }
        }
        for (var child : file.getChildren()) {
            if (child instanceof BladePsiPhpBlock || child.getNode().getElementType() == BladeTokenTypes.BLADE_COMMENT) {
                add(result, document, child, child.getTextRange());
            }
        }
        return result.toArray(FoldingDescriptor.EMPTY);
    }

    private static void add(List<FoldingDescriptor> result, Document document, PsiElement element, TextRange range) {
        if (range.getLength() > 0 && document.getLineNumber(range.getStartOffset()) < document.getLineNumber(range.getEndOffset() - 1)) {
            result.add(new FoldingDescriptor(element.getNode(), range));
        }
    }

    @Override
    public String getPlaceholderText(ASTNode node) {
        if (node.getElementType() == BladeTokenTypes.BLADE_COMMENT) {
            return "{{-- … --}}";
        }
        if (node.getPsi() instanceof BladePsiDirective directive) {
            return directive.getText().replaceAll("\\s+", " ") + " …";
        }
        return "@php … @endphp";
    }

    @Override
    public boolean isCollapsedByDefault(ASTNode node) {
        return false;
    }
}
