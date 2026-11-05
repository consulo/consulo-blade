package com.jetbrains.php.blade;

import consulo.html.language.HTMLLanguage;

public final class BladeHTMLLanguage extends HTMLLanguage {
    public static final BladeHTMLLanguage INSTANCE = new BladeHTMLLanguage();

    private BladeHTMLLanguage() {
        super(HTMLLanguage.INSTANCE, "BladeHTML");
    }
}
