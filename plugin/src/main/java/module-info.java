import org.jspecify.annotations.NullMarked;

/**
 * The editor features for Blade templates
 */
@NullMarked
module consulo.blade {
    requires consulo.blade.impl;
    requires consulo.language.impl;
    requires consulo.project.api;
    requires consulo.document.api;
    requires consulo.code.editor.api;
    requires consulo.language.editor.api;
    requires consulo.language.code.style.api;
    requires consulo.language.spellchecker.api;
    requires consulo.php.api;
    requires com.intellij.xml;

    exports consulo.blade.ide;
    exports consulo.blade.ide.completion;
    exports consulo.blade.ide.editor;
    exports consulo.blade.ide.format;
    exports consulo.blade.ide.highlight;
}
