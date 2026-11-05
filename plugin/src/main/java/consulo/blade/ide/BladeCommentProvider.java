package consulo.blade.ide;

import com.jetbrains.php.blade.BladeLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.blade.impl.BladeCommenter;
import consulo.codeEditor.Editor;
import consulo.language.Commenter;
import consulo.language.Language;
import consulo.language.editor.MultipleLangCommentProvider;
import consulo.language.file.FileViewProvider;
import consulo.language.psi.PsiFile;
import org.jspecify.annotations.Nullable;

/**
 * Applies Blade comments to both Blade and HTML regions.
 */
@ExtensionImpl
public final class BladeCommentProvider implements MultipleLangCommentProvider {
    private static final Commenter COMMENTER = new BladeCommenter();

    @Override
    public boolean canProcess(PsiFile file, FileViewProvider provider) {
        return provider.getBaseLanguage() == BladeLanguage.INSTANCE;
    }

    @Override
    public @Nullable Commenter getLineCommenter(PsiFile file, Editor editor, Language start, Language end) {
        return canProcess(file, file.getViewProvider()) ? COMMENTER : null;
    }
}
