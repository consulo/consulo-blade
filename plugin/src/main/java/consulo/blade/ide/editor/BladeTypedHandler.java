package consulo.blade.ide.editor;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.lexer.BladeLexer;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.ast.IElementType;
import consulo.language.editor.action.TypedHandlerDelegate;
import consulo.language.editor.inject.EditorWindow;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

@ExtensionImpl
public final class BladeTypedHandler extends TypedHandlerDelegate {
    private static final Pattern COMMENT_START = Pattern.compile("\\{\\{[ \\t]*--$");

    @Override
    public Result charTyped(char c, Project project, Editor editor, PsiFile file) {
        if (c != '{' && c != '!' && c != '-') {
            return Result.CONTINUE;
        }
        editor = EditorWindow.getTopLevelEditor(editor);
        var hostFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (hostFile == null || hostFile.getViewProvider().getBaseLanguage() != BladeLanguage.INSTANCE) {
            return Result.CONTINUE;
        }
        var document = editor.getDocument();
        int offset = editor.getCaretModel().getOffset();
        String before = document.getCharsSequence()
            .subSequence(document.getLineStartOffset(document.getLineNumber(offset)), offset)
            .toString();
        int start;
        String closing;
        boolean comment = false;
        if (c == '-' && COMMENT_START.matcher(before).find()) {
            var matcher = COMMENT_START.matcher(before);
            matcher.find();
            start = offset - before.length() + matcher.start();
            closing = "--}}";
            comment = true;
        }
        else if (before.endsWith("{{")) {
            start = offset - 2;
            closing = "}}";
        }
        else if (before.endsWith("{!!")) {
            start = offset - 3;
            closing = "!!}";
        }
        else {
            return Result.CONTINUE;
        }
        IElementType token = tokenAt(document.getCharsSequence(), start);
        if (token != BladeTokenTypes.ECHO_START
            && token != BladeTokenTypes.RAW_ECHO_START
            && token != BladeTokenTypes.BLADE_COMMENT) {
            return Result.CONTINUE;
        }
        int end = offset;
        String after = document.getCharsSequence().subSequence(offset, document.getTextLength()).toString();
        if (comment) {
            int whitespace = 0;
            while (whitespace < after.length() && (after.charAt(whitespace) == ' ' || after.charAt(whitespace) == '\t')) {
                whitespace++;
            }
            if (after.startsWith("}}", whitespace)) {
                end += whitespace + 2;
            }
            document.replaceString(start, end, "{{--  --}}");
            editor.getCaretModel().moveToOffset(start + 5);
        }
        else {
            if (after.startsWith(closing)) {
                end += closing.length();
            }
            else if (after.startsWith("}")) {
                end++; // The platform may have paired the initial '{'.
            }
            document.replaceString(offset, end, "  " + closing);
            editor.getCaretModel().moveToOffset(offset + 1);
        }
        return Result.STOP;
    }

    private static @Nullable IElementType tokenAt(CharSequence text, int offset) {
        var lexer = new BladeLexer();
        lexer.start(text);
        while (lexer.getTokenType() != null && lexer.getTokenEnd() <= offset) {
            lexer.advance();
        }
        return lexer.getTokenStart() == offset ? lexer.getTokenType() : null;
    }
}
