package consulo.blade.impl;

import com.jetbrains.php.blade.BladeLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Commenter;
import consulo.language.Language;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public final class BladeCommenter implements Commenter {
    @Override
    public Language getLanguage() {
        return BladeLanguage.INSTANCE;
    }

    @Override
    public @Nullable String getLineCommentPrefix() {
        return null;
    }

    @Override
    public String getBlockCommentPrefix() {
        return "{{--";
    }

    @Override
    public String getBlockCommentSuffix() {
        return "--}}";
    }

    @Override
    public @Nullable String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public @Nullable String getCommentedBlockCommentSuffix() {
        return null;
    }
}
