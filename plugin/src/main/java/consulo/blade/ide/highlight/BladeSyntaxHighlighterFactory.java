package consulo.blade.ide.highlight;

import com.jetbrains.php.blade.BladeLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.highlight.*;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public final class BladeSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    @Override
    public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile file) {
        return new BladeSyntaxHighlighter();
    }

    @Override
    public Language getLanguage() {
        return BladeLanguage.INSTANCE;
    }
}
