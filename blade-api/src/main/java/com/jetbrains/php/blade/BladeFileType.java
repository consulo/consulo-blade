package com.jetbrains.php.blade;

import consulo.blade.icon.BladeIconGroup;
import consulo.blade.localize.BladeLocalize;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

public final class BladeFileType extends LanguageFileType {
    public static final BladeFileType INSTANCE = new BladeFileType();

    private BladeFileType() {
        super(BladeLanguage.INSTANCE);
    }

    @Override
    public String getId() {
        return "BLADE";
    }

    @Override
    public LocalizeValue getDescription() {
        return BladeLocalize.bladeFileTypeDescription();
    }

    @Override
    public String getDefaultExtension() {
        return "blade.php";
    }

    @Override
    public Image getIcon() {
        return BladeIconGroup.blade();
    }
}
