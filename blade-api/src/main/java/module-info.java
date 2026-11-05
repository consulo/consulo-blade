import org.jspecify.annotations.NullMarked;

/**
 * The Blade language, its file type, element types and PSI, and the directive registry
 */
@NullMarked
module consulo.blade.api {
    requires transitive consulo.language.api;
    requires transitive consulo.application.api;
    requires transitive consulo.component.api;
    requires transitive consulo.color.scheme.api;
    requires transitive consulo.localize.api;
    requires transitive consulo.ui.api;
    requires transitive consulo.virtual.file.system.api;
    requires transitive com.intellij.xml.html.api;

    exports com.jetbrains.php.blade;
    exports com.jetbrains.php.blade.parser;
    exports com.jetbrains.php.blade.psi;
    exports consulo.blade;
    exports consulo.blade.icon;
    exports consulo.blade.localize;
}
