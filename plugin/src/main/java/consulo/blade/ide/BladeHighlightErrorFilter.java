package consulo.blade.ide;

import com.jetbrains.php.blade.BladeHTMLLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.blade.impl.BladeFileViewProvider;
import consulo.language.editor.HighlightErrorFilter;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;

/**
 * Suppress HTML errors caused by adjacent template fragments, preserving ordinary HTML errors.
 */
@ExtensionImpl
public final class BladeHighlightErrorFilter extends HighlightErrorFilter {
    @Override
    public boolean shouldHighlightErrorElement(PsiErrorElement error) {
        PsiFile file = error.getContainingFile();
        if (file == null || !(file.getViewProvider() instanceof BladeFileViewProvider)
            || !file.getLanguage().isKindOf(BladeHTMLLanguage.INSTANCE)) {
            return true;
        }
        PsiElement before = PsiTreeUtil.prevLeaf(error, true);
        while (before instanceof PsiWhiteSpace) {
            before = PsiTreeUtil.prevLeaf(before, true);
        }
        PsiElement after = PsiTreeUtil.nextLeaf(error, true);
        while (after instanceof PsiWhiteSpace) {
            after = PsiTreeUtil.nextLeaf(after, true);
        }
        return !(before instanceof OuterLanguageElement) && !(after instanceof OuterLanguageElement);
    }
}
