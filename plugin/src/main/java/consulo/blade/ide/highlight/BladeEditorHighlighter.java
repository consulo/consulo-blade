package consulo.blade.ide.highlight;

import consulo.colorScheme.EditorColorsScheme;
import consulo.html.language.HTMLLanguage;
import consulo.language.editor.highlight.*;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;
import static com.jetbrains.php.blade.psi.BladeTokenTypes.HTML_TEMPLATE;

public final class BladeEditorHighlighter extends LayeredLexerEditorHighlighter {
    public BladeEditorHighlighter(@Nullable Project project, @Nullable VirtualFile file, EditorColorsScheme colors) {
        super(new BladeSyntaxHighlighter(), colors);
        SyntaxHighlighter html = SyntaxHighlighterFactory.getSyntaxHighlighter(HTMLLanguage.INSTANCE, project, file);
        if (html != null) {
            registerLayer(HTML_TEMPLATE, new LayerDescriptor(html, ""));
        }
    }
}
