package consulo.blade.impl;

import com.jetbrains.php.blade.BladeLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.BracePair;
import consulo.language.Language;
import consulo.language.PairedBraceMatcher;
import consulo.language.ast.IElementType;
import org.jspecify.annotations.Nullable;
import static com.jetbrains.php.blade.psi.BladeTokenTypes.*;

@ExtensionImpl
public final class BladePairedBraceMatcher implements PairedBraceMatcher {
    @Override
    public Language getLanguage() {
        return BladeLanguage.INSTANCE;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(IElementType left, @Nullable IElementType next) {
        return true;
    }

    @Override
    public BracePair[] getPairs() {
        return new BracePair[]{
            new BracePair(ECHO_START, ECHO_END, false),
            new BracePair(RAW_ECHO_START, RAW_ECHO_END, false),
            new BracePair(DIRECTIVE_PARAMETER_START, DIRECTIVE_PARAMETER_END, false)
        };
    }
}
