package consulo.blade.it;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.psi.BladeFileImpl;
import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.application.internal.TransferredWriteActionService;
import consulo.blade.BladeDirectivePairs;
import consulo.blade.ide.BladeFoldingBuilder;
import consulo.blade.ide.BladeStructureViewFactory;
import consulo.blade.ide.completion.BladeDirectiveCompletionContributor;
import consulo.blade.ide.editor.BladeEnterHandler;
import consulo.blade.ide.editor.BladeTypedHandler;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorFactory;
import consulo.dataContext.DataContext;
import consulo.it.AllowLogError;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.editor.action.EnterHandlerDelegate;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.PrioritizedLookupElement;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.util.lang.ref.SimpleReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(HeadlessProjectExtension.class)
@AllowLogError("consulo.xml.impl.internal.ExternalResource")
public class BladeEditorIntegrationTest {
    private static void onEdt(Runnable action) throws Exception {
        WriteAction.run(() -> Application.get().getInstance(TransferredWriteActionService.class)
            .runOnEdtWithTransferredWriteActionAndWait(action));
    }
    private static void withEditor(CodeInsightTestFixture fixture, Consumer<Editor> action) throws Exception {
        onEdt(() -> {
            var xml = consulo.xml.application.options.editor.XmlEditorOptions.getInstance();
            boolean syncTags = xml.isSyncTagEditing();
            xml.setSyncTagEditing(false);
            var factory = EditorFactory.getInstance();
            var editor = factory.createEditor(fixture.getDocument(), fixture.getProject(), fixture.getFile().getVirtualFile(), false);
            try { consulo.undoRedo.CommandProcessor.getInstance().executeCommand(fixture.getProject(), () -> action.accept(editor), "Blade editor test", null); }
            finally { factory.releaseEditor(editor); xml.setSyncTagEditing(syncTags); }
        });
    }
    private static void type(Editor editor, PsiFile file, String text) {
        var handler = new BladeTypedHandler();
        for (char c : text.toCharArray()) {
            int offset = editor.getCaretModel().getOffset();
            editor.getDocument().insertString(offset, String.valueOf(c));
            editor.getCaretModel().moveToOffset(offset + 1);
            handler.charTyped(c, file.getProject(), editor, file);
        }
        PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
    }
    @Test public void typingCompletesDelimitersAndCommentConversion(CodeInsightTestFixture fixture) throws Exception {
        for (var entry : Map.of("{{", "{{  }}", "{!!", "{!!  !!}", "{{--", "{{--  --}}", "@{{", "@{{").entrySet()) {
            var file = fixture.configureByText("typing-" + entry.getKey().hashCode() + ".blade.php", "");
            withEditor(fixture, editor -> {
                type(editor, file, entry.getKey());
                assertEquals(entry.getValue(), editor.getDocument().getText());
                if (!entry.getKey().equals("@{{")) assertEquals(entry.getValue().length() / 2, editor.getCaretModel().getOffset());
            });
        }
        for (String source : List.of("{{}}", "{{}", "{!!}")) {
            var file = fixture.configureByText("existing-" + source.hashCode() + ".blade.php", source);
            withEditor(fixture, editor -> {
                boolean raw = source.startsWith("{!!");
                editor.getCaretModel().moveToOffset(raw ? 3 : 2);
                new BladeTypedHandler().charTyped(raw ? '!' : '{', file.getProject(), editor, file);
                assertEquals(raw ? "{!!  !!}" : "{{  }}", editor.getDocument().getText());
            });
        }
    }
    @Test public void commentConversionWorksInsideTheInjectedPhpEditor(CodeInsightTestFixture fixture) throws Exception {
        var file = fixture.configureByText("injected-typing.blade.php", "{{ - }}");
        withEditor(fixture, editor -> {
            editor.getDocument().insertString(4, "-");
            editor.getCaretModel().moveToOffset(5);
            PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
            var window = consulo.language.editor.inject.InjectedEditorManager.getInstance(file.getProject())
                .getEditorForInjectedLanguageNoCommit(editor, file, 5);
            assertInstanceOf(consulo.language.editor.inject.EditorWindow.class, window);
            var injectedFile = ((consulo.language.editor.inject.EditorWindow)window).getInjectedFile();
            new BladeTypedHandler().charTyped('-', file.getProject(), window, injectedFile);
            assertEquals("{{--  --}}", editor.getDocument().getText());
        });
    }

    @Test public void typingLeavesPhpCommentsAndVerbatimAlone(CodeInsightTestFixture fixture) throws Exception {
        for (String prefix : List.of("@php $text = '", "{{-- a ", "@verbatim ")) {
            var file = fixture.configureByText("opaque-" + prefix.hashCode() + ".blade.php", prefix);
            withEditor(fixture, editor -> {
                editor.getCaretModel().moveToOffset(prefix.length());
                type(editor, file, "{{");
                assertEquals(prefix + "{{", editor.getDocument().getText());
            });
        }
    }
    @Test public void enterClosesOnlyUnclosedBlocks(CodeInsightTestFixture fixture) throws Exception {
        for (String opening : List.of("@if($ready)", "@section('content')", "@forelse($items as $item)", "@php", "@verbatim")) {
            var file = fixture.configureByText("enter-" + opening.hashCode() + ".blade.php", "  " + opening);
            String close = switch (opening) {
                case "@if($ready)" -> "@endif";
                case "@section('content')" -> "@endsection";
                case "@forelse($items as $item)" -> "@endforelse";
                case "@php" -> "@endphp";
                default -> "@endverbatim";
            };
            withEditor(fixture, editor -> {
                int offset = file.getTextLength();
                var options = consulo.language.codeStyle.CodeStyle.getIndentOptions(file);
                options.INDENT_SIZE = 4;
                options.USE_TAB_CHARACTER = false;
                editor.getCaretModel().moveToOffset(offset);
                var result = new BladeEnterHandler().preprocessEnter(file, editor, SimpleReference.create(offset), SimpleReference.create(0), DataContext.EMPTY_CONTEXT, null);
                assertEquals(EnterHandlerDelegate.Result.Stop, result, opening);
                assertEquals("  " + opening + "\n      \n  " + close, editor.getDocument().getText());
                assertEquals(offset + 7, editor.getCaretModel().getOffset());
            });
        }
        for (String source : List.of("@if($ready)\n@endif", "@section('title', 'Home')", "@php($x = 1)")) {
            var file = fixture.configureByText("paired-" + source.hashCode() + ".blade.php", source);
            withEditor(fixture, editor -> {
                int offset = source.contains("\n") ? source.indexOf('\n') : source.length();
                editor.getCaretModel().moveToOffset(offset);
                var result = new BladeEnterHandler().preprocessEnter(file, editor, SimpleReference.create(offset), SimpleReference.create(0), DataContext.EMPTY_CONTEXT, null);
                assertEquals(EnterHandlerDelegate.Result.Continue, result);
                assertEquals(source, editor.getDocument().getText());
            });
        }
    }
    @Test public void foldingAndStructureUseTheSamePairs(CodeInsightTestFixture fixture) throws Exception {
        String source = "@section('content')\n@component('card')\n@slot('title')\nTitle\n@endslot\n@endcomponent\n@endsection\n@push('scripts')\n<script></script>\n@endpush\n{{--\nComment\n--}}\n@php\n$x = 1;\n@endphp\n@section('inline', 'Title')";
        var file = (BladeFileImpl)fixture.configureByText("structure.blade.php", source);
        var document = fixture.getDocument();
        ReadAction.run(() -> {
            var pairs = BladeDirectivePairs.get(file);
            assertSame(pairs, BladeDirectivePairs.get(file));
            var folds = new BladeFoldingBuilder().buildFoldRegions(file, document, false);
            assertEquals(6, folds.length);
            assertTrue(Arrays.stream(folds).anyMatch(f -> f.getRange().substring(source).equals("@slot('title')\nTitle\n@endslot")));
            var roots = new BladeStructureViewFactory.Node(file).getChildrenBase().stream().map(e -> (BladeStructureViewFactory.Node)e).toList();
            assertEquals(List.of("@section('content')", "@push('scripts')", "@section('inline', 'Title')"), roots.stream().map(n -> n.getPresentableText().get()).toList());
            var component = (BladeStructureViewFactory.Node)roots.getFirst().getChildrenBase().iterator().next();
            assertEquals("@component('card')", component.getPresentableText().get());
            assertEquals("@slot('title')", ((BladeStructureViewFactory.Node)component.getChildrenBase().iterator().next()).getPresentableText().get());
        });
        onEdt(() -> {
            document.setText("@if($a)\nA\n@endif");
            PsiDocumentManager.getInstance(file.getProject()).commitDocument(document);
        });
        ReadAction.run(() -> {
            var pairs = BladeDirectivePairs.get(file).pairs();
            assertEquals(1, pairs.size());
            assertEquals("@if", pairs.getFirst().opening().getName());
        });
    }
    @Test public void completionFiltersPrefixesAndPrioritizesTheInnerClose(CodeInsightTestFixture fixture) throws Exception {
        var file = fixture.configureByText("completion.blade.php", "@section('content')\n@if($ready)\n@en");
        withEditor(fixture, editor -> {
            var results = complete(file, editor);
            assertTrue(results.stream().anyMatch(e -> e.getLookupString().equals("endif")));
            assertTrue(results.stream().allMatch(e -> e.getLookupString().startsWith("en")));
            double ifPriority = priority(results, "endif"), sectionPriority = priority(results, "endsection");
            assertTrue(ifPriority > sectionPriority);
            assertTrue(sectionPriority > priority(results, "endforeach"));
        });
        for (String source : List.of("{{-- @en", "@verbatim @en", "@@en", "mail@en", "{{ '@en")) {
            var ignored = fixture.configureByText("ignored-" + source.hashCode() + ".blade.php", source);
            withEditor(fixture, editor -> assertTrue(complete(ignored, editor).isEmpty(), source));
        }
    }
    private static double priority(List<LookupElement> elements, String name) {
        return elements.stream().filter(e -> e.getLookupString().equals(name)).findFirst().orElseThrow()
            .as(PrioritizedLookupElement.CLASS_CONDITION_KEY).getPriority();
    }
    private static List<LookupElement> complete(PsiFile file, Editor editor) {
        var blade = (BladeFileImpl)file.getViewProvider().getPsi(BladeLanguage.INSTANCE);
        var completion = BladeDirectiveCompletionContributor.collect(blade, file.getTextLength());
        return completion == null ? List.of() : completion.elements();
    }
}
