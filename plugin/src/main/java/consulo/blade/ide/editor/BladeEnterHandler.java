package consulo.blade.ide.editor;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.blade.BladeDirectivePairs;
import consulo.blade.BladeDirectives;
import consulo.codeEditor.Editor;
import consulo.codeEditor.action.EditorActionHandler;
import consulo.dataContext.DataContext;
import consulo.language.codeStyle.CodeStyle;
import consulo.language.editor.action.EnterHandlerDelegateAdapter;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.ref.SimpleReference;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public final class BladeEnterHandler extends EnterHandlerDelegateAdapter {
    @Override
    public Result preprocessEnter(
        PsiFile file,
        Editor editor,
        SimpleReference<Integer> caretOffset,
        SimpleReference<Integer> caretAdvance,
        DataContext context,
        @Nullable EditorActionHandler originalHandler
    ) {
        if (file.getViewProvider().getBaseLanguage() != BladeLanguage.INSTANCE) {
            return Result.Continue;
        }
        var document = editor.getDocument();
        int offset = java.util.Objects.requireNonNull(caretOffset.get());
        if (offset == 0) {
            return Result.Continue;
        }
        PsiDocumentManager.getInstance(file.getProject()).commitDocument(document);
        var blade = file.getViewProvider().getPsi(BladeLanguage.INSTANCE);
        if (!(blade instanceof BladeFileImpl bladeFile)) {
            return Result.Continue;
        }
        var leaf = bladeFile.findElementAt(offset - 1);
        var directive = PsiTreeUtil.getParentOfType(leaf, BladePsiDirective.class, false);
        String closing;
        int openingOffset;
        if (directive != null && directive.getTextRange().getEndOffset() == offset) {
            var pair = BladeDirectivePairs.get(bladeFile).find(directive);
            if (pair == null || pair.opening() != directive || pair.closing() != null) {
                return Result.Continue;
            }
            var definition = BladeDirectives.snapshot().get(directive.getName().substring(1));
            if (definition == null || definition.pair().isEmpty()) {
                return Result.Continue;
            }
            closing = definition.pair();
            openingOffset = directive.getTextOffset();
        }
        else {
            var php = PsiTreeUtil.getParentOfType(leaf, BladePsiPhpBlock.class, false);
            if (php == null || php.getNode().findChildByType(BladeTokenTypes.ENDPHP_DIRECTIVE) != null
                || php.getFirstChild() == null || php.getFirstChild().getTextRange().getEndOffset() != offset) {
                return Result.Continue;
            }
            closing = "endphp";
            openingOffset = php.getTextOffset();
        }
        int line = document.getLineNumber(offset), start = document.getLineStartOffset(line), end = document.getLineEndOffset(line);
        var chars = document.getCharsSequence();
        if (!chars.subSequence(offset, end).toString().isBlank()) {
            return Result.Continue;
        }
        String indent = chars.subSequence(start, openingOffset).toString();
        if (!indent.isBlank()) {
            return Result.Continue;
        }
        var options = CodeStyle.getIndentOptions(file);
        String unit = options.USE_TAB_CHARACTER ? "\t" : " ".repeat(options.INDENT_SIZE);
        document.replaceString(offset, end, "\n" + indent + unit + "\n" + indent + "@" + closing);
        editor.getCaretModel().moveToOffset(offset + 1 + indent.length() + unit.length());
        return Result.Stop;
    }
}
