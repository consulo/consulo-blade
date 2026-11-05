package com.jetbrains.php.blade.psi;

import com.jetbrains.php.blade.BladeLanguage;
import consulo.language.ast.IElementType;

public final class BladeDirectiveElementType extends IElementType {
    public BladeDirectiveElementType(String name) {
        super(name, BladeLanguage.INSTANCE);
    }
}
