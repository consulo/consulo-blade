package consulo.blade.ide;

import com.jetbrains.php.blade.*;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.test.junit.impl.extension.*;
import consulo.xml.language.psi.XmlTag;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@NullUnmarked
@ExtendWith(ConsuloProjectLoader.class)
public class BladeHtmlLayerTest {
    public record Context(PsiFileFactory factory) implements InjectingRecord {
    }

    @Test
    public void htmlTagsKeepTheirShape(Context context) {
        for (String source : new String[]{
            "<text-input :value=\"{{ $email }}\" @if($locked) disabled @endif\n @input=\"update\"></text-input>",
            "<input :value=\"{{ $email }}\">",
            "<input @if($locked) disabled @endif>",
            "<div :items=\"@json($items)\"></div>",
            "<livewire:counter/>",
            "<flux:button>Save</flux:button>",
            "<button @click=\"save\" @input=\"update\">Save</button>"
        }) {
            var blade = context.factory().createFileFromText("example.blade.php", BladeLanguage.INSTANCE, source);
            var html = blade.getViewProvider().getPsi(BladeHTMLLanguage.INSTANCE);
            assertNotNull(html);
            assertEquals(source, html.getText());
            assertSame(BladeLanguage.INSTANCE, html.getViewProvider().getBaseLanguage());
            assertNull(PsiTreeUtil.findChildOfType(html, PsiErrorElement.class), source);
            var tags = PsiTreeUtil.findChildrenOfType(html, XmlTag.class);
            assertEquals(1, tags.size(), source);
            var tag = tags.iterator().next();
            assertEquals(source, tag.getText());
            assertEquals(0, tag.getTextRange().getStartOffset());
        }
    }
}
