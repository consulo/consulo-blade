package consulo.blade;

import com.jetbrains.php.blade.psi.BladeDirectiveElementType;
import consulo.application.Application;
import consulo.component.extension.ExtensionPoint;

import java.util.*;
import static com.jetbrains.php.blade.psi.BladeTokenTypes.*;

/**
 * Shared, case-sensitive directive vocabulary. Each lexer takes an immutable snapshot.
 */
public final class BladeDirectives {
    public enum Kind {
        OPEN,
        MIDDLE,
        CLOSE,
        INLINE
    }

    public record PhpTemplate(String prefix, String suffix) {
        public static PhpTemplate call(String expression) {
            return new PhpTemplate("<?php " + expression + "(", "); ?>");
        }

        public static PhpTemplate statement(String code) {
            return new PhpTemplate("<?php " + code + " ?>", "");
        }
    }

    public record Definition(String name, BladeDirectiveElementType tokenType, Kind kind, String pair, PhpTemplate phpTemplate) {
        public Definition(String name, BladeDirectiveElementType tokenType, Kind kind, String pair) {
            this(name, tokenType, kind, pair, defaultPhpTemplate(name));
        }
    }

    private record Snapshot(long modificationCount, Map<String, Definition> definitions) {
    }

    // the providers change only when plugins are reloaded, which changes the modification count of the extension point
    private static volatile Snapshot ourSnapshot = new Snapshot(-1, Map.of());

    private static final Map<String, Definition> BUILT_INS;

    static {
        Map<String, Definition> definitions = new LinkedHashMap<>();
        add(definitions, "if", IF_DIRECTIVE, Kind.OPEN, "endif");
        add(definitions, "elseif", ELSEIF_DIRECTIVE, Kind.MIDDLE, "");
        add(definitions, "else", ELSE_DIRECTIVE, Kind.MIDDLE, "");
        add(definitions, "endif", ENDIF_DIRECTIVE, Kind.CLOSE, "if");
        add(definitions, "unless", UNLESS_DIRECTIVE, Kind.OPEN, "endunless");
        add(definitions, "endunless", ENDUNLESS_DIRECTIVE, Kind.CLOSE, "unless");
        add(definitions, "isset", ISSET_DIRECTIVE, Kind.OPEN, "endisset");
        add(definitions, "endisset", ENDISSET_DIRECTIVE, Kind.CLOSE, "isset");
        add(definitions, "empty", EMPTY_DIRECTIVE, Kind.OPEN, "endempty");
        add(definitions, "endempty", ENDEMPTY_DIRECTIVE, Kind.CLOSE, "empty");
        add(definitions, "auth", AUTH_DIRECTIVE, Kind.OPEN, "endauth");
        add(definitions, "endauth", ENDAUTH_DIRECTIVE, Kind.CLOSE, "auth");
        add(definitions, "guest", GUEST_DIRECTIVE, Kind.OPEN, "endguest");
        add(definitions, "endguest", ENDGUEST_DIRECTIVE, Kind.CLOSE, "guest");
        add(definitions, "production", PRODUCTION_DIRECTIVE, Kind.OPEN, "endproduction");
        add(definitions, "endproduction", ENDPRODUCTION_DIRECTIVE, Kind.CLOSE, "production");
        add(definitions, "env", ENV_DIRECTIVE, Kind.OPEN, "endenv");
        add(definitions, "endenv", ENDENV_DIRECTIVE, Kind.CLOSE, "env");
        add(definitions, "hasSection", HAS_SECTION_DIRECTIVE, Kind.OPEN, "endif");
        add(definitions, "sectionMissing", SECTION_MISSING_DIRECTIVE, Kind.OPEN, "endif");
        add(definitions, "switch", SWITCH_DIRECTIVE, Kind.OPEN, "endswitch");
        add(definitions, "case", CASE_DIRECTIVE, Kind.MIDDLE, "");
        add(definitions, "break", BREAK_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "default", DEFAULT_DIRECTIVE, Kind.MIDDLE, "");
        add(definitions, "endswitch", ENDSWITCH_DIRECTIVE, Kind.CLOSE, "switch");
        add(definitions, "for", FOR_DIRECTIVE, Kind.OPEN, "endfor");
        add(definitions, "endfor", ENDFOR_DIRECTIVE, Kind.CLOSE, "for");
        add(definitions, "foreach", FOREACH_DIRECTIVE, Kind.OPEN, "endforeach");
        add(definitions, "endforeach", ENDFOREACH_DIRECTIVE, Kind.CLOSE, "foreach");
        add(definitions, "forelse", FORELSE_DIRECTIVE, Kind.OPEN, "endforelse");
        add(definitions, "endforelse", ENDFORELSE_DIRECTIVE, Kind.CLOSE, "forelse");
        add(definitions, "while", WHILE_DIRECTIVE, Kind.OPEN, "endwhile");
        add(definitions, "endwhile", ENDWHILE_DIRECTIVE, Kind.CLOSE, "while");
        add(definitions, "continue", CONTINUE_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "extends", EXTENDS_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "section", SECTION_DIRECTIVE, Kind.OPEN, "endsection");
        add(definitions, "endsection", ENDSECTION_DIRECTIVE, Kind.CLOSE, "section");
        add(definitions, "show", SHOW_DIRECTIVE, Kind.CLOSE, "section");
        add(definitions, "stop", STOP_DIRECTIVE, Kind.CLOSE, "section");
        add(definitions, "append", APPEND_DIRECTIVE, Kind.CLOSE, "section");
        add(definitions, "overwrite", OVERWRITE_DIRECTIVE, Kind.CLOSE, "section");
        add(definitions, "yield", YIELD_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "parent", PARENT_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "push", PUSH_DIRECTIVE, Kind.OPEN, "endpush");
        add(definitions, "endpush", ENDPUSH_DIRECTIVE, Kind.CLOSE, "push");
        add(definitions, "pushOnce", PUSH_ONCE_DIRECTIVE, Kind.OPEN, "endPushOnce");
        add(definitions, "endPushOnce", END_PUSH_ONCE_DIRECTIVE, Kind.CLOSE, "pushOnce");
        add(definitions, "pushIf", PUSH_IF_DIRECTIVE, Kind.OPEN, "endPushIf");
        add(definitions, "endPushIf", END_PUSH_IF_DIRECTIVE, Kind.CLOSE, "pushIf");
        add(definitions, "prepend", PREPEND_DIRECTIVE, Kind.OPEN, "endprepend");
        add(definitions, "endprepend", ENDPREPEND_DIRECTIVE, Kind.CLOSE, "prepend");
        add(definitions, "prependOnce", PREPEND_ONCE_DIRECTIVE, Kind.OPEN, "endPrependOnce");
        add(definitions, "endPrependOnce", END_PREPEND_ONCE_DIRECTIVE, Kind.CLOSE, "prependOnce");
        add(definitions, "stack", STACK_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "once", ONCE_DIRECTIVE, Kind.OPEN, "endonce");
        add(definitions, "endonce", ENDONCE_DIRECTIVE, Kind.CLOSE, "once");
        add(definitions, "fragment", FRAGMENT_DIRECTIVE, Kind.OPEN, "endfragment");
        add(definitions, "endfragment", ENDFRAGMENT_DIRECTIVE, Kind.CLOSE, "fragment");
        add(definitions, "include", INCLUDE_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "includeIf", INCLUDE_IF_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "includeWhen", INCLUDE_WHEN_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "includeUnless", INCLUDE_UNLESS_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "includeFirst", INCLUDE_FIRST_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "each", EACH_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "component", COMPONENT_DIRECTIVE, Kind.OPEN, "endcomponent");
        add(definitions, "endcomponent", ENDCOMPONENT_DIRECTIVE, Kind.CLOSE, "component");
        add(definitions, "componentFirst", COMPONENT_FIRST_DIRECTIVE, Kind.OPEN, "endcomponent");
        add(definitions, "slot", SLOT_DIRECTIVE, Kind.OPEN, "endslot");
        add(definitions, "endslot", ENDSLOT_DIRECTIVE, Kind.CLOSE, "slot");
        add(definitions, "props", PROPS_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "aware", AWARE_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "php", PHP_DIRECTIVE, Kind.OPEN, "endphp");
        add(definitions, "endphp", ENDPHP_DIRECTIVE, Kind.CLOSE, "php");
        add(definitions, "verbatim", VERBATIM_DIRECTIVE, Kind.OPEN, "endverbatim");
        add(definitions, "endverbatim", ENDVERBATIM_DIRECTIVE, Kind.CLOSE, "verbatim");
        add(definitions, "use", USE_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "inject", INJECT_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "lang", LANG_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "choice", CHOICE_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "json", JSON_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "js", JS_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "csrf", CSRF_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "method", METHOD_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "error", ERROR_DIRECTIVE, Kind.OPEN, "enderror");
        add(definitions, "enderror", ENDERROR_DIRECTIVE, Kind.CLOSE, "error");
        add(definitions, "can", CAN_DIRECTIVE, Kind.OPEN, "endcan");
        add(definitions, "cannot", CANNOT_DIRECTIVE, Kind.OPEN, "endcannot");
        add(definitions, "canany", CANANY_DIRECTIVE, Kind.OPEN, "endcanany");
        add(definitions, "endcan", ENDCAN_DIRECTIVE, Kind.CLOSE, "can");
        add(definitions, "endcannot", ENDCANNOT_DIRECTIVE, Kind.CLOSE, "cannot");
        add(definitions, "endcanany", ENDCANANY_DIRECTIVE, Kind.CLOSE, "canany");
        add(definitions, "class", CLASS_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "style", STYLE_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "checked", CHECKED_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "selected", SELECTED_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "disabled", DISABLED_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "readonly", READONLY_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "required", REQUIRED_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "dd", DD_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "dump", DUMP_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "vite", VITE_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "viteReactRefresh", VITE_REACT_REFRESH_DIRECTIVE, Kind.INLINE, "");
        add(definitions, "session", SESSION_DIRECTIVE, Kind.OPEN, "endsession");
        add(definitions, "endsession", ENDSESSION_DIRECTIVE, Kind.CLOSE, "session");
        add(definitions, "context", CONTEXT_DIRECTIVE, Kind.OPEN, "endcontext");
        add(definitions, "endcontext", ENDCONTEXT_DIRECTIVE, Kind.CLOSE, "context");
        BUILT_INS = Collections.unmodifiableMap(definitions);
    }

    private BladeDirectives() {
    }

    private static PhpTemplate defaultPhpTemplate(String name) {
        return switch (name) {
            case "if", "elseif", "while", "for", "foreach", "switch" -> new PhpTemplate("<?php " + name + "(", "): ?>");
            case "unless" -> new PhpTemplate("<?php if(!(", ")): ?>");
            case "isset", "empty" -> new PhpTemplate("<?php if(" + name + "(", ")): ?>");
            case "case" -> new PhpTemplate("<?php case ", ": ?>");
            case "else", "default" -> PhpTemplate.statement(name + ":");
            case "endif", "endunless", "endisset", "endempty", "endauth", "endguest", "endproduction", "endenv",
                 "enderror", "endcan", "endcannot", "endcanany", "endsession", "endcontext" -> PhpTemplate.statement("endif;");
            case "endfor", "endforeach", "endwhile", "endswitch" -> PhpTemplate.statement(name + ";");
            case "break", "continue" -> new PhpTemplate("<?php if(", ") " + name + "; ?>");
            case "php" -> new PhpTemplate("<?php ", "; ?>");
            case "extends", "include" -> new PhpTemplate("<?php echo $__env->make(", ")->render(); ?>");
            case "includeIf", "includeWhen", "includeUnless", "includeFirst", "each", "component", "componentFirst", "slot" ->
                PhpTemplate.call("echo $__env->" + switch (name) {
                    case "each" -> "renderEach";
                    case "component" -> "startComponent";
                    case "componentFirst" -> "startComponentFirst";
                    case "slot" -> "slot";
                    default -> name.replace("include", "render");
                });
            case "section" -> PhpTemplate.call("$__env->startSection");
            case "yield" -> PhpTemplate.call("echo $__env->yieldContent");
            case "parent" -> PhpTemplate.statement("echo $__env->yieldContent('__parent');");
            case "use" -> PhpTemplate.call("app");
            case "push", "pushOnce", "pushIf" -> PhpTemplate.call("$__env->startPush");
            case "prepend", "prependOnce" -> PhpTemplate.call("$__env->startPrepend");
            case "stack" -> PhpTemplate.call("echo $__env->yieldPushContent");
            case "hasSection" -> new PhpTemplate("<?php if($__env->hasSection(", ")): ?>");
            case "sectionMissing" -> new PhpTemplate("<?php if(!$__env->hasSection(", ")): ?>");
            case "endsection", "stop" -> PhpTemplate.statement("$__env->stopSection();");
            case "show" -> PhpTemplate.statement("echo $__env->yieldSection();");
            case "append" -> PhpTemplate.statement("$__env->appendSection();");
            case "overwrite" -> PhpTemplate.statement("$__env->stopSection(true);");
            case "endpush", "endPushOnce", "endPushIf" -> PhpTemplate.statement("$__env->stopPush();");
            case "endprepend", "endPrependOnce" -> PhpTemplate.statement("$__env->stopPrepend();");
            case "endcomponent" -> PhpTemplate.statement("echo $__env->renderComponent();");
            case "endslot" -> PhpTemplate.statement("$__env->endSlot();");
            case "lang", "choice" ->
                PhpTemplate.call("echo \\Illuminate\\Support\\Facades\\Lang::" + (name.equals("lang") ? "get" : "choice"));
            case "inject" -> PhpTemplate.call("$__inject = app");
            case "json", "js", "class", "style", "checked", "selected", "disabled", "readonly", "required" ->
                new PhpTemplate("<?php echo ", "; ?>");
            case "auth", "guest" -> new PhpTemplate("<?php if(" + (name.equals("guest") ? "!" : "") + "auth(", ")->check()): ?>");
            case "production" -> PhpTemplate.statement("if(app()->environment('production')):");
            case "env" -> new PhpTemplate("<?php if(app()->environment(", ")): ?>");
            case "can", "cannot", "canany" -> new PhpTemplate(
                "<?php if(" + (name.equals("cannot") ? "!" : "") + "app('Illuminate\\\\Contracts\\\\Auth\\\\Access\\\\Gate')->"
                    + (name.equals("canany") ? "any" : "check") + "(",
                ")): ?>"
            );
            case "error", "session", "context" -> new PhpTemplate("<?php if(" + name + "(", ")): ?>");
            case "once", "endonce", "verbatim", "endverbatim", "endphp" -> new PhpTemplate("", "");
            case "fragment" -> PhpTemplate.call("$__env->startFragment");
            case "endfragment" -> PhpTemplate.statement("$__env->stopFragment();");
            default -> PhpTemplate.call("echo " + name);
        };
    }

    private static void add(Map<String, Definition> target, String name, BladeDirectiveElementType type, Kind kind, String pair) {
        target.put(name, new Definition(name, type, kind, pair));
    }

    public static Map<String, Definition> builtIns() {
        return BUILT_INS;
    }

    public static Map<String, Definition> snapshot() {
        ExtensionPoint<BladeDirectiveProvider> providers = Application.get().getExtensionPoint(BladeDirectiveProvider.class);
        long modificationCount = providers.getModificationCount();
        Snapshot snapshot = ourSnapshot;
        if (snapshot.modificationCount() == modificationCount) {
            return snapshot.definitions();
        }
        Map<String, Definition> result = new LinkedHashMap<>(BUILT_INS);
        for (BladeDirectiveProvider provider : providers.getExtensionList()) {
            for (Definition definition : provider.getDirectives()) {
                result.put(definition.name(), definition);
            }
        }
        Map<String, Definition> definitions = Collections.unmodifiableMap(result);
        ourSnapshot = new Snapshot(modificationCount, definitions);
        return definitions;
    }
}
