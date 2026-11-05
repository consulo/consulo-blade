package consulo.xml.codeInsight.editorActions;

import consulo.application.ReadAction;
import consulo.it.AllowLogError;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.inject.InjectedLanguageManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(HeadlessProjectExtension.class)
@AllowLogError("consulo.xml.impl.internal.ExternalResource")
public class XmlAutoPopupInBladeTest {
    @Test public void hostOffsetsAreNotAppliedToInjectedPhpLeaves(CodeInsightTestFixture fixture) throws Exception {
        String text = "@if($ready)\n{{ $title }}\n@endif";
        var file = fixture.configureByText("popup.blade.php", text);
        ReadAction.run(() -> {
            int offset = text.indexOf("title") + 1;
            var element = InjectedLanguageManager.getInstance(file.getProject()).findInjectedElementAt(file, offset - 1);
            assertNotNull(element);
            assertNotSame(file.getViewProvider(), element.getContainingFile().getViewProvider());
            assertFalse(XmlAutoPopupHandler.doCompleteIfNeeded(offset, file, element));
        });
    }
    @Test public void htmlAutoPopupStillRecognizesAnOpeningTag(CodeInsightTestFixture fixture) throws Exception {
        var file = fixture.configureByText("popup.html", "<div></div>");
        ReadAction.run(() -> {
            var element = file.findElementAt(0);
            assertNotNull(element);
            assertTrue(XmlAutoPopupHandler.doCompleteIfNeeded(1, file, element));
            assertFalse(XmlAutoPopupHandler.doCompleteIfNeeded(0, file, element));
            assertFalse(XmlAutoPopupHandler.doCompleteIfNeeded(100, file, element));
        });
    }
}
