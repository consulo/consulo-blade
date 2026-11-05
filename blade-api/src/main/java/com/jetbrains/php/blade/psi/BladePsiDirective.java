package com.jetbrains.php.blade.psi;

import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * A directive, for example {@code @if($user)} or {@code @endif}
 */
public interface BladePsiDirective extends PsiElement {
    /**
     * The directive name with its {@code @}
     */
    String getName();

    @Nullable BladePsiDirectiveParameter getParameter();

    IElementType getDirectiveElementType();
}
