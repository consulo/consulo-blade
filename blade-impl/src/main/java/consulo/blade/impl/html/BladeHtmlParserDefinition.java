package consulo.blade.impl.html;

import com.jetbrains.php.blade.BladeHTMLLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IFileElementType;
import consulo.xml.lang.html.HTMLParserDefinition;

@ExtensionImpl
public final class BladeHtmlParserDefinition extends HTMLParserDefinition {
    private static final IFileElementType FILE = new IFileElementType("BLADE_HTML_FILE", BladeHTMLLanguage.INSTANCE);

    @Override
    public consulo.language.psi.PsiFile createFile(consulo.language.file.FileViewProvider provider) {
        return new consulo.xml.psi.impl.source.html.HtmlFileImpl(provider, FILE);
    }

    @Override
    public Language getLanguage() {
        return BladeHTMLLanguage.INSTANCE;
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }
}
