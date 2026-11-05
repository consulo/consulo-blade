import org.jspecify.annotations.NullMarked;

/**
 * The Blade lexer, parser and PSI implementation, the Blade file view provider and the PHP injection
 */
@NullMarked
module consulo.blade.impl {
    requires transitive consulo.blade.api;
    requires consulo.language.impl;
    requires consulo.document.api;
    requires consulo.project.api;
    requires consulo.php.api;
    requires com.intellij.xml;

    exports com.jetbrains.php.blade.injection;
    exports com.jetbrains.php.blade.lexer;
    exports consulo.blade.impl;
    exports consulo.blade.impl.html;
    exports consulo.blade.impl.inject;
    exports consulo.blade.impl.parser;
    exports consulo.blade.impl.psi;
}
