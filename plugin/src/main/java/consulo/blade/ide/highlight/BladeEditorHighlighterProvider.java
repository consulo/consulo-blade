package consulo.blade.ide.highlight;

import com.jetbrains.php.blade.BladeFileType;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.EditorHighlighter;
import consulo.colorScheme.EditorColorsScheme;
import consulo.language.editor.highlight.EditorHighlighterProvider;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public final class BladeEditorHighlighterProvider implements EditorHighlighterProvider {
    @Override
    public EditorHighlighter getEditorHighlighter(
        @Nullable Project project,
        FileType type,
        @Nullable VirtualFile file,
        EditorColorsScheme colors
    ) {
        return new BladeEditorHighlighter(project, file, colors);
    }

    @Override
    public FileType getFileType() {
        return BladeFileType.INSTANCE;
    }
}
