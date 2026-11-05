package consulo.blade.it;

import consulo.application.Application;
import consulo.application.WriteAction;
import consulo.application.internal.TransferredWriteActionService;
import consulo.it.AllowLogError;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.undoRedo.CommandProcessor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("CodeStyleInternalHelper is implemented only in ide-impl, which the headless application doesn't load")
@ExtendWith(HeadlessProjectExtension.class)
@AllowLogError("consulo.xml.impl.internal.ExternalResource")
public class BladeFormattingIntegrationTest {
    private static String reformat(CodeInsightTestFixture fixture, String name, String text) throws Exception {
        PsiFile file = fixture.configureByText(name, text);
        WriteAction.run(() -> Application.get().getInstance(TransferredWriteActionService.class)
            .runOnEdtWithTransferredWriteActionAndWait(() -> CommandProcessor.getInstance().executeCommand(
                fixture.getProject(),
                () -> {
                    CodeStyleManager.getInstance(fixture.getProject()).reformat(file);
                    PsiDocumentManager.getInstance(fixture.getProject()).doPostponedOperationsAndUnblockDocument(fixture.getDocument());
                },
                "Blade formatting test",
                null
            )));
        return fixture.getDocument().getText();
    }

    @Test
    public void contentOfDirectivePairsIsIndented(CodeInsightTestFixture fixture) throws Exception {
        String formatted = reformat(fixture, "pairs.blade.php", """
            @section('content')
            <h1>{{ $title }}</h1>
            @if($user)
            <p>{{ $user->name }}</p>
            @else
            <span>guest</span>
            @endif
            @endsection
            """);
        assertEquals("""
            @section('content')
                <h1>{{ $title }}</h1>
                @if($user)
                    <p>{{ $user->name }}</p>
                @else
                    <span>guest</span>
                @endif
            @endsection
            """, formatted);
    }

    @Test
    public void pairsInsideHtmlElementsAreIndentedLikeTheirChildren(CodeInsightTestFixture fixture) throws Exception {
        String formatted = reformat(fixture, "nested.blade.php", """
            <div>
            @if($x)
            <p>a</p>
            @else
            <ul>
            @foreach($items as $item)
            <li>{{ $item }}</li>
            @endforeach
            </ul>
            @endif
            <span>{{ $y }}</span>
            </div>
            """);
        assertEquals("""
            <div>
                @if($x)
                    <p>a</p>
                @else
                    <ul>
                        @foreach($items as $item)
                            <li>{{ $item }}</li>
                        @endforeach
                    </ul>
                @endif
                <span>{{ $y }}</span>
            </div>
            """, formatted);
    }

    @Test
    public void inlineDirectivesAndBladeTextStayAsWritten(CodeInsightTestFixture fixture) throws Exception {
        String formatted = reformat(fixture, "inline.blade.php", """
            @section('title', 'Home')
            <p>{{   $title   }}</p>
            @include('partials.footer')
            """);
        assertEquals("""
            @section('title', 'Home')
            <p>{{   $title   }}</p>
            @include('partials.footer')
            """, formatted);
    }
}
