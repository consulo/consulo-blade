package consulo.blade.impl;

import com.jetbrains.php.blade.BladeLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.file.FileViewProvider;
import consulo.language.file.LanguageFileViewProviderFactory;
import consulo.language.psi.PsiManager;
import consulo.virtualFileSystem.VirtualFile;

@ExtensionImpl
public final class BladeFileViewProviderFactory implements LanguageFileViewProviderFactory {
    @Override
    public FileViewProvider createFileViewProvider(VirtualFile file, Language language, PsiManager manager, boolean physical) {
        return new BladeFileViewProvider(manager, file, physical);
    }

    @Override
    public Language getLanguage() {
        return BladeLanguage.INSTANCE;
    }
}
