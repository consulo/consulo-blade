package consulo.blade.ide;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.blade.BladeDirectivePairs;
import consulo.blade.icon.BladeIconGroup;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.language.Language;
import consulo.language.editor.structureView.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import org.jspecify.annotations.Nullable;

import java.util.*;

@ExtensionImpl
public final class BladeStructureViewFactory implements PsiStructureViewFactory {
    @Override
    public Language getLanguage() {
        return BladeLanguage.INSTANCE;
    }

    @Override
    public StructureViewBuilder getStructureViewBuilder(PsiFile file) {
        return TemplateLanguageStructureViewBuilder.create(file, (psi, editor) ->
            new StructureViewModelBase(psi, editor, new Node(psi)).withSuitableClasses(BladePsiDirective.class));
    }

    public static final class Node extends PsiTreeElementBase<PsiElement> {
        public Node(PsiElement element) {
            super(element);
        }

        @Override
        public Image getIcon() {
            return BladeIconGroup.blade();
        }

        @Override
        public LocalizeValue getPresentableText() {
            var element = getElement();
            if (element instanceof PsiFile file) {
                return LocalizeValue.of(file.getName());
            }
            return LocalizeValue.of(element == null ? "" : element.getText().replaceAll("\\s+", " "));
        }

        @Override
        public Collection<StructureViewTreeElement> getChildrenBase() {
            var element = getElement();
            if (element == null || !(element.getContainingFile() instanceof BladeFileImpl file)) {
                return List.of();
            }
            List<BladePsiDirective> directives = new ArrayList<>();
            for (var child : file.getChildren()) {
                if (child instanceof BladePsiDirective directive && Set.of(
                    "@section", "@push", "@pushOnce", "@pushIf", "@prepend", "@prependOnce", "@slot", "@component", "@componentFirst"
                ).contains(directive.getName())) {
                    directives.add(directive);
                }
            }
            var pairs = BladeDirectivePairs.get(file);
            List<StructureViewTreeElement> result = new ArrayList<>();
            for (var directive : directives) {
                @Nullable BladePsiDirective parent = null;
                for (var candidate : directives) {
                    if (candidate.getTextOffset() >= directive.getTextOffset()) {
                        break;
                    }
                    var pair = pairs.find(candidate);
                    if (pair != null && pair.opening() == candidate && pair.endOffset() > directive.getTextOffset()) {
                        parent = candidate;
                    }
                }
                if (element instanceof PsiFile && parent == null || element == parent) {
                    result.add(new Node(directive));
                }
            }
            return result;
        }
    }
}
