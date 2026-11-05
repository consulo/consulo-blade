package com.jetbrains.php.blade.injection;

import com.jetbrains.php.blade.psi.BladeFileImpl;
import com.jetbrains.php.blade.psi.BladePsiLanguageInjectionHost;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * Consumer API for finding the Blade file that owns an injected PHP element.
 */
public final class BladeVariableTypeProvider {
    private BladeVariableTypeProvider() {
    }

    public static @Nullable BladeFileImpl getHostPhpFileForInjectedIfExists(PsiElement element) {
        var manager = InjectedLanguageManager.getInstance(element.getProject());
        var file = element.getContainingFile();
        if (file == null || !manager.isInjectedFragment(file)) {
            return null;
        }
        var host = manager.getInjectionHost(element);
        return host instanceof BladePsiLanguageInjectionHost && host.isValidHost()
            && host.getContainingFile() instanceof BladeFileImpl blade ? blade : null;
    }
}
