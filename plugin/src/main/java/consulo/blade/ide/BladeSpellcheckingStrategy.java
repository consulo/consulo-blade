package consulo.blade.ide;

import com.jetbrains.php.blade.BladeLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.spellcheker.SpellcheckingStrategy;

/**
 * Checks {@code {{-- --}}} comments of the Blade layer. The HTML layer is checked by the HTML strategy through the
 * {@code BladeHTML} dialect, the PHP fragments by the PHP strategy of the injected file.
 */
@ExtensionImpl
public final class BladeSpellcheckingStrategy extends SpellcheckingStrategy {
    @Override
    public Language getLanguage() {
        return BladeLanguage.INSTANCE;
    }
}
