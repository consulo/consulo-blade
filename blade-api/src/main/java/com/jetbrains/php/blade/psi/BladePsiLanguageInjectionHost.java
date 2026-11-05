package com.jetbrains.php.blade.psi;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiLanguageInjectionHost;

public interface BladePsiLanguageInjectionHost extends PsiLanguageInjectionHost {
    TextRange getContentRange();
}
