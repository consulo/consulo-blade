package consulo.blade.impl.inject;

import com.jetbrains.php.blade.psi.*;
import com.jetbrains.php.lang.PhpLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.blade.BladeDirectives;
import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.psi.PsiElement;
import consulo.language.version.LanguageVersionUtil;

import java.util.*;

/**
 * Compiles all PHP-bearing hosts into one injection, retaining document order and PHP scope.
 */
@ExtensionImpl
public final class BladePhpInjector implements MultiHostInjector {
    public record Place(BladePsiLanguageInjectionHost host, String prefix, String suffix) {
    }

    private static final String HEADER = "<?php /** @var \\Illuminate\\View\\Factory $__env */ ?>";

    @Override
    public Class<? extends PsiElement> getElementClass() {
        return BladeFileImpl.class;
    }

    @Override
    public void injectLanguages(MultiHostRegistrar registrar, PsiElement element) {
        if (!(element.getContainingFile() instanceof BladeFileImpl file)) {
            return;
        }
        List<Place> places = collectPlaces(file);
        if (places.isEmpty()) {
            return;
        }
        registrar.startInjecting(
            LanguageVersionUtil.findLanguageVersion(PhpLanguage.INSTANCE, file.getProject(), file.getVirtualFile()),
            "php"
        );
        for (Place place : places) {
            registrar.addPlace(place.prefix(), place.suffix(), place.host(), place.host().getContentRange());
        }
        registrar.doneInjecting();
    }

    public static List<Place> collectPlaces(BladeFileImpl file) {
        var definitions = BladeDirectives.snapshot();
        List<Place> places = new ArrayList<>();
        StringBuilder pending = new StringBuilder(HEADER);
        Deque<Integer> forelse = new ArrayDeque<>();
        Set<Integer> emptyBranches = new HashSet<>();
        int loopId = 0;
        boolean switchAwaitingCase = false;
        for (PsiElement child : file.getChildren()) {
            if (child instanceof BladePsiEcho || child instanceof BladePsiRawEcho) {
                add(places, pending, (BladePsiLanguageInjectionHost) child, new BladeDirectives.PhpTemplate("<?php echo ", "; ?>"));
            }
            else if (child instanceof BladePsiPhpBlock block) {
                // A newline prevents a trailing PHP line comment from swallowing the closing tag.
                add(places, pending, block, new BladeDirectives.PhpTemplate("<?php ", "\n ?>"));
            }
            else if (child instanceof BladePsiDirective directive) {
                String name = directive.getName().substring(1);
                var parameter = directive.getParameter();
                var definition = definitions.get(name);
                var template = definition == null ? BladeDirectives.PhpTemplate.call("echo " + name) : definition.phpTemplate();
                if (name.equals("switch")) {
                    template = new BladeDirectives.PhpTemplate("<?php switch(", "): ");
                    switchAwaitingCase = true;
                }
                else if (switchAwaitingCase && (name.equals("case") || name.equals("default"))) {
                    template = name.equals("case")
                        ? new BladeDirectives.PhpTemplate("case ", ": ?>")
                        : new BladeDirectives.PhpTemplate("default: ?>", "");
                    switchAwaitingCase = false;
                }
                else if (name.equals("forelse")) {
                    int id = ++loopId;
                    forelse.push(id);
                    template = new BladeDirectives.PhpTemplate(
                        "<?php $__empty_" + id + " = true; foreach(",
                        "): $__empty_" + id + " = false; ?>"
                    );
                }
                else if (name.equals("empty") && parameter == null && !forelse.isEmpty()) {
                    int id = forelse.peek();
                    emptyBranches.add(id);
                    template = BladeDirectives.PhpTemplate.statement("endforeach; if($__empty_" + id + "):");
                }
                else if (name.equals("endforelse")) {
                    template = BladeDirectives.PhpTemplate.statement(
                        !forelse.isEmpty() && emptyBranches.remove(forelse.pop()) ? "endif;" : "endforeach;"
                    );
                }
                else if ((name.equals("break") || name.equals("continue")) && parameter == null) {
                    template = BladeDirectives.PhpTemplate.statement(name + ";");
                }
                if (parameter != null) {
                    add(places, pending, parameter, template);
                }
                else {
                    pending.append(template.prefix()).append(template.suffix());
                }
            }
        }
        if (!places.isEmpty()) {
            int last = places.size() - 1;
            Place place = places.get(last);
            places.set(last, new Place(place.host(), place.prefix(), place.suffix() + pending));
        }
        return List.copyOf(places);
    }

    private static void add(
        List<Place> places,
        StringBuilder pending,
        BladePsiLanguageInjectionHost host,
        BladeDirectives.PhpTemplate template
    ) {
        places.add(new Place(host, pending + template.prefix(), "\n" + template.suffix()));
        pending.setLength(0);
    }
}
