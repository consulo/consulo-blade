package consulo.blade.ide.completion;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.lexer.BladeLexer;
import com.jetbrains.php.blade.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.blade.BladeDirectivePairs;
import consulo.blade.BladeDirectives;
import consulo.blade.icon.BladeIconGroup;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.PrioritizedLookupElement;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

import java.util.*;

@ExtensionImpl
public class BladeDirectiveCompletionContributor extends CompletionContributor {
    @Override
    public Language getLanguage() {
        return BladeLanguage.INSTANCE;
    }

    @Override
    public boolean invokeAutoPopup(PsiElement position, char c) {
        var file = position.getContainingFile();
        return c == '@' && file != null && file.getViewProvider().getBaseLanguage() == BladeLanguage.INSTANCE;
    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        var original = parameters.getOriginalFile();
        if (original.getViewProvider().getBaseLanguage() != BladeLanguage.INSTANCE) {
            return;
        }
        var blade = original.getViewProvider().getPsi(BladeLanguage.INSTANCE);
        if (!(blade instanceof BladeFileImpl file)) {
            return;
        }
        var completion = collect(file, parameters.getOffset());
        if (completion != null) {
            result.withPrefixMatcher(completion.prefix()).addAllElements(completion.elements());
        }
    }

    public record Completion(String prefix, List<LookupElement> elements) {
    }

    public static @Nullable Completion collect(BladeFileImpl file, int offset) {
        String text = file.getText();
        if (offset > text.length()) {
            return null;
        }
        int at = offset;
        while (at > 0 && Character.isJavaIdentifierPart(text.charAt(at - 1))) {
            at--;
        }
        if (at == 0 || text.charAt(at - 1) != '@') {
            return null;
        }
        at--;
        if (at > 0 && (text.charAt(at - 1) == '@' || Character.isJavaIdentifierPart(text.charAt(at - 1)))) {
            return null;
        }
        var lexer = new BladeLexer();
        lexer.start(text);
        boolean verbatim = false;
        while (lexer.getTokenType() != null && lexer.getTokenEnd() <= at) {
            if (lexer.getTokenType() == BladeTokenTypes.VERBATIM_DIRECTIVE) {
                verbatim = true;
            }
            if (lexer.getTokenType() == BladeTokenTypes.ENDVERBATIM_DIRECTIVE) {
                verbatim = false;
            }
            lexer.advance();
        }
        var token = lexer.getTokenType();
        if (verbatim
            || token != BladeTokenTypes.HTML_TEMPLATE
            && !(token instanceof BladeDirectiveElementType)
            && token != BladeTokenTypes.CUSTOM_DIRECTIVE) {
            return null;
        }
        String prefix = text.substring(at + 1, offset);
        List<LookupElement> elements = new ArrayList<>();
        var definitions = BladeDirectives.snapshot();
        Map<String, Integer> closers = new HashMap<>();
        int priority = 1000;
        for (var pair : BladeDirectivePairs.get(file).enclosing(at)) {
            var definition = definitions.get(pair.opening().getName().substring(1));
            if (definition != null) {
                closers.putIfAbsent(definition.pair(), priority--);
            }
        }
        for (var definition : definitions.values()) {
            if (!definition.name().startsWith(prefix)) {
                continue;
            }
            var lookup = LookupElementBuilder.create(definition.name())
                .withPresentableText("@" + definition.name())
                .withIcon(BladeIconGroup.blade());
            elements.add(PrioritizedLookupElement.withPriority(lookup, closers.getOrDefault(definition.name(), 0)));
        }
        return new Completion(prefix, List.copyOf(elements));
    }
}
