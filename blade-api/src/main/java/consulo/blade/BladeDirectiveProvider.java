package consulo.blade;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;

import java.util.Collection;

@ExtensionAPI(ComponentScope.APPLICATION)
public interface BladeDirectiveProvider {
    Collection<BladeDirectives.Definition> getDirectives();
}
