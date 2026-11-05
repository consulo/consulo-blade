package consulo.blade.impl;

import com.jetbrains.php.blade.BladeHTMLLanguage;
import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.parser.BladeElementTypes;
import consulo.language.Language;
import consulo.language.impl.file.MultiplePsiFilesPerDocumentFileViewProvider;
import consulo.language.impl.psi.PsiFileImpl;
import consulo.language.parser.ParserDefinition;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.template.TemplateLanguageFileViewProvider;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public final class BladeFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider implements TemplateLanguageFileViewProvider {
    public BladeFileViewProvider(PsiManager manager, VirtualFile file, boolean physical) {
        super(manager, file, physical);
    }

    @Override
    public Language getBaseLanguage() {
        return BladeLanguage.INSTANCE;
    }

    @Override
    public Language getTemplateDataLanguage() {
        return BladeHTMLLanguage.INSTANCE;
    }

    @Override
    public Set<Language> getLanguages() {
        return Set.of(getBaseLanguage(), getTemplateDataLanguage());
    }

    @Override
    protected @Nullable PsiFile createFile(Language language) {
        ParserDefinition definition = ParserDefinition.forLanguage(language);
        if (definition == null) {
            return null;
        }
        if (language == getBaseLanguage()) {
            return definition.createFile(this);
        }
        if (language == getTemplateDataLanguage()) {
            PsiFileImpl file = (PsiFileImpl) definition.createFile(this);
            file.setContentElementType(BladeTemplateElementTypes.TEMPLATE_DATA);
            return file;
        }
        return null;
    }

    @Override
    protected BladeFileViewProvider cloneInner(VirtualFile copy) {
        return new BladeFileViewProvider(getManager(), copy, false);
    }
}
