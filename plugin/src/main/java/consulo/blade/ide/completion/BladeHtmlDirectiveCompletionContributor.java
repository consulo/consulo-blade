package consulo.blade.ide.completion;

import com.jetbrains.php.blade.BladeHTMLLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;

@ExtensionImpl
public final class BladeHtmlDirectiveCompletionContributor extends BladeDirectiveCompletionContributor {
    @Override
    public Language getLanguage() {
        return BladeHTMLLanguage.INSTANCE;
    }
}
