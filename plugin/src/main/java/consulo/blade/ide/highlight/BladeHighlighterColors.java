package consulo.blade.ide.highlight;

import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.colorScheme.TextAttributesKey;

public interface BladeHighlighterColors {
    TextAttributesKey DIRECTIVE = TextAttributesKey.createTextAttributesKey("BLADE_DIRECTIVE", DefaultLanguageHighlighterColors.KEYWORD);
    TextAttributesKey ECHO_BRACES =
        TextAttributesKey.createTextAttributesKey("BLADE_ECHO_BRACES", DefaultLanguageHighlighterColors.MARKUP_TAG);
    TextAttributesKey RAW_ECHO_BRACES =
        TextAttributesKey.createTextAttributesKey("BLADE_RAW_ECHO_BRACES", DefaultLanguageHighlighterColors.MARKUP_TAG);
    TextAttributesKey COMMENT = TextAttributesKey.createTextAttributesKey("BLADE_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    TextAttributesKey PARAMETER_BRACES =
        TextAttributesKey.createTextAttributesKey("BLADE_PARAMETER_BRACES", DefaultLanguageHighlighterColors.PARENTHESES);
    TextAttributesKey PHP_BACKGROUND =
        TextAttributesKey.createTextAttributesKey("BLADE_PHP_BACKGROUND", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR);
}
