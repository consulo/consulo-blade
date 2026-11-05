package consulo.blade.ide.format;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.parser.BladeElementTypes;
import com.jetbrains.php.blade.psi.BladeFileImpl;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import consulo.annotation.component.ExtensionImpl;
import consulo.blade.impl.BladeTemplateElementTypes;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.Alignment;
import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.Indent;
import consulo.language.codeStyle.Wrap;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.xml.psi.formatter.xml.XmlFormattingPolicy;
import consulo.xml.template.formatter.AbstractXmlTemplateFormattingModelBuilder;
import consulo.xml.template.formatter.FragmentedTemplateException;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Formats a Blade file as HTML with the Blade constructs in it. The Blade constructs keep their text; the blocks between
 * paired directives ({@code @if … @endif}, {@code @section … @endsection}, …) are indented, at file level and inside
 * HTML elements.
 */
@ExtensionImpl
public final class BladeFormattingModelBuilder extends AbstractXmlTemplateFormattingModelBuilder {
    @Override
    protected boolean isTemplateFile(PsiFile file) {
        return file instanceof BladeFileImpl;
    }

    @Override
    public boolean isOuterLanguageElement(PsiElement element) {
        return element.getNode().getElementType() == BladeTemplateElementTypes.OUTER_ELEMENT_TYPE;
    }

    @Override
    public boolean isMarkupLanguageElement(PsiElement element) {
        return element.getNode().getElementType() == BladeTokenTypes.HTML_TEMPLATE;
    }

    @Override
    protected Block createTemplateLanguageBlock(ASTNode node,
                                                CodeStyleSettings settings,
                                                XmlFormattingPolicy xmlFormattingPolicy,
                                                Indent indent,
                                                @Nullable Alignment alignment,
                                                @Nullable Wrap wrap) {
        return new BladeLeafBlock(node, indent);
    }

    @Override
    public List<Block> mergeWithTemplateBlocks(List<Block> markupBlocks,
                                               CodeStyleSettings settings,
                                               XmlFormattingPolicy xmlFormattingPolicy,
                                               Indent childrenIndent) throws FragmentedTemplateException {
        List<Block> merged = super.mergeWithTemplateBlocks(markupBlocks, settings, xmlFormattingPolicy, childrenIndent);
        return BladeDirectiveGrouping.group(merged);
    }

    @Override
    public Language getLanguage() {
        return BladeLanguage.INSTANCE;
    }
}
