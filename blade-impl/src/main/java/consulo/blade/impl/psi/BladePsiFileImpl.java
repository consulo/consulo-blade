package consulo.blade.impl.psi;

import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.psi.BladeFileImpl;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.virtualFileSystem.fileType.FileType;

public final class BladePsiFileImpl extends PsiFileBase implements BladeFileImpl {
    public BladePsiFileImpl(FileViewProvider provider) {
        super(provider, BladeLanguage.INSTANCE);
    }

    @Override
    public FileType getFileType() {
        return BladeFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "BladeFile:" + getName();
    }
}
