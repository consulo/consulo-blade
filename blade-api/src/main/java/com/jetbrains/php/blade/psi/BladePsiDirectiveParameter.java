package com.jetbrains.php.blade.psi;

import consulo.language.ast.IElementType;

/**
 * The parameter of a directive, with its parentheses
 */
public interface BladePsiDirectiveParameter extends BladePsiHost {
    /**
     * The text between the parentheses
     */
    String getParameterText();

    /**
     * The element type of the directive this parameter belongs to
     */
    IElementType getDirectiveElementType();
}
