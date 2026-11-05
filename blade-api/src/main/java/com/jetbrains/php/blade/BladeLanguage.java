package com.jetbrains.php.blade;

import consulo.language.Language;
import consulo.language.template.TemplateLanguage;

public final class BladeLanguage extends Language implements TemplateLanguage {
    public static final BladeLanguage INSTANCE = new BladeLanguage();

    private BladeLanguage() {
        super("Blade");
    }
}
