package consulo.blade.impl;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import consulo.language.ast.IElementType;
import consulo.language.impl.psi.template.OuterLanguageElementType;
import consulo.language.impl.psi.template.TemplateDataElementType;

/**
 * The element types that join the Blade tree and the HTML tree of a Blade file
 */
public interface BladeTemplateElementTypes {
    /**
     * A Blade fragment in the HTML tree
     */
    IElementType OUTER_ELEMENT_TYPE = new OuterLanguageElementType("BLADE_FRAGMENT", BladeLanguage.INSTANCE);

    /**
     * The HTML tree of a Blade file, built from the HTML parts of its Blade tree
     */
    TemplateDataElementType TEMPLATE_DATA =
        new TemplateDataElementType("BLADE_TEMPLATE_DATA", BladeLanguage.INSTANCE, BladeTokenTypes.HTML_TEMPLATE, OUTER_ELEMENT_TYPE);
}
