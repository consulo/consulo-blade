package com.jetbrains.php.blade.parser;

import com.jetbrains.php.blade.BladeLanguage;
import consulo.language.ast.IElementType;
import consulo.language.ast.IFileElementType;

public interface BladeElementTypes {
    IFileElementType FILE = new IFileElementType("BLADE_FILE", BladeLanguage.INSTANCE);
    IElementType TEXT_BLOCK = new IElementType("TEXT_BLOCK", BladeLanguage.INSTANCE);
    IElementType RAW_TEXT_BLOCK = new IElementType("RAW_TEXT_BLOCK", BladeLanguage.INSTANCE);
    IElementType DIRECTIVE = new IElementType("DIRECTIVE", BladeLanguage.INSTANCE);
    IElementType DIRECTIVE_PARAMETER = new IElementType("DIRECTIVE_PARAMETER", BladeLanguage.INSTANCE);
    IElementType PHP_BLOCK = new IElementType("PHP_BLOCK", BladeLanguage.INSTANCE);
}
