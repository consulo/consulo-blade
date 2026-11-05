package consulo.blade;

import com.jetbrains.php.blade.psi.BladeFileImpl;
import com.jetbrains.php.blade.psi.BladePsiDirective;
import consulo.application.util.CachedValueProvider;
import consulo.language.psi.util.LanguageCachedValueUtil;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * A computed block view of the flat Blade tree. No PSI nodes are reparented.
 */
public final class BladeDirectivePairs {
    public record Pair(BladePsiDirective opening, @Nullable BladePsiDirective closing, List<BladePsiDirective> branches, int endOffset) {
    }

    private final List<Pair> pairs;

    private final Map<BladePsiDirective, Pair> byDirective;

    private BladeDirectivePairs(List<Pair> pairs) {
        this.pairs = List.copyOf(pairs);
        Map<BladePsiDirective, Pair> index = new HashMap<>();
        for (Pair pair : pairs) {
            index.put(pair.opening(), pair);
            if (pair.closing() != null) {
                index.put(pair.closing(), pair);
            }
            for (var branch : pair.branches()) {
                index.put(branch, pair);
            }
        }
        byDirective = Map.copyOf(index);
    }

    public static BladeDirectivePairs get(BladeFileImpl file) {
        return LanguageCachedValueUtil.getCachedValue(file, () -> CachedValueProvider.Result.create(build(file), file));
    }

    public List<Pair> pairs() {
        return pairs;
    }

    public @Nullable Pair find(BladePsiDirective directive) {
        return byDirective.get(directive);
    }

    /**
     * Innermost first, including blocks whose closing directive is still ahead of the caret.
     */
    public List<Pair> enclosing(int offset) {
        List<Pair> result = new ArrayList<>();
        for (Pair pair : pairs) {
            if (pair.opening().getTextRange().getEndOffset() <= offset
                && pair.endOffset() >= offset) {
                result.add(pair);
            }
        }
        Collections.reverse(result);
        return result;
    }

    public static boolean isBlock(BladePsiDirective directive, BladeDirectives.Definition definition) {
        if (definition.kind() != BladeDirectives.Kind.OPEN) {
            return false;
        }
        var parameter = directive.getParameter();
        String name = definition.name();
        if (name.equals("php") && parameter != null) {
            return false;
        }
        return parameter == null || !Set.of("section", "slot", "push", "prepend").contains(name)
            || !hasTopLevelComma(parameter.getParameterText());
    }

    private static BladeDirectivePairs build(BladeFileImpl file) {
        var definitions = BladeDirectives.snapshot();
        List<Block> blocks = new ArrayList<>();
        Deque<Block> stack = new ArrayDeque<>();
        for (var child : file.getChildren()) {
            if (!(child instanceof BladePsiDirective directive)) {
                continue;
            }
            String name = directive.getName().substring(1);
            var definition = definitions.get(name);
            if (definition == null) {
                continue;
            }
            boolean forelseBranch = name.equals("empty") && directive.getParameter() == null;
            if (!forelseBranch && isBlock(directive, definition)) {
                Block block = new Block(directive, definition, file.getTextLength());
                blocks.add(block);
                stack.push(block);
            }
            else if (definition.kind() == BladeDirectives.Kind.CLOSE) {
                Block match = null;
                for (Block block : stack) {
                    if (block.definition.pair().equals(name) || definition.pair().equals(block.definition.name())) {
                        match = block;
                        break;
                    }
                }
                if (match != null) {
                    while (stack.peek() != match) {
                        stack.pop().endOffset = directive.getTextOffset();
                    }
                    stack.pop();
                    match.endOffset = directive.getTextOffset();
                    match.closing = directive;
                }
            }
            else if (forelseBranch || definition.kind() == BladeDirectives.Kind.MIDDLE) {
                Block match = null;
                for (Block block : stack) {
                    boolean accepts = forelseBranch ? block.definition.name().equals("forelse")
                        : switch (name) {
                            case "case", "default" -> block.definition.name().equals("switch");
                            case "else", "elseif" -> block.definition.pair().equals("endif")
                                || Set.of(
                                    "unless", "isset", "empty", "auth", "guest", "production", "env", "error", "can", "cannot", "canany",
                                    "session", "context"
                                ).contains(block.definition.name());
                            default -> definition.pair().isEmpty() || definition.pair().equals(block.definition.name());
                        };
                    if (accepts) {
                        match = block;
                        break;
                    }
                }
                if (match != null) {
                    while (stack.peek() != match) {
                        stack.pop().endOffset = directive.getTextOffset();
                    }
                    match.branches.add(directive);
                }
            }
        }
        return new BladeDirectivePairs(
            blocks.stream().map(b -> new Pair(b.opening, b.closing, List.copyOf(b.branches), b.endOffset)).toList()
        );
    }

    private static final class Block {
        final BladePsiDirective opening;

        final BladeDirectives.Definition definition;

        final List<BladePsiDirective> branches = new ArrayList<>();

        @Nullable BladePsiDirective closing;

        int endOffset;

        Block(BladePsiDirective opening, BladeDirectives.Definition definition, int endOffset) {
            this.opening = opening;
            this.definition = definition;
            this.endOffset = endOffset;
        }
    }

    private static boolean hasTopLevelComma(String text) {
        int depth = 0;
        char quote = 0;
        boolean lineComment = false, blockComment = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i), next = i + 1 < text.length() ? text.charAt(i + 1) : 0;
            if (lineComment) {
                if (c == '\n' || c == '\r') {
                    lineComment = false;
                }
                continue;
            }
            if (blockComment) {
                if (c == '*' && next == '/') {
                    blockComment = false;
                    i++;
                }
                continue;
            }
            if (quote != 0) {
                if (c == '\\') {
                    i++;
                }
                else if (c == quote) {
                    quote = 0;
                }
                continue;
            }
            if (c == '\'' || c == '"' || c == '`') {
                quote = c;
            }
            else if (c == '/' && next == '*') {
                blockComment = true;
                i++;
            }
            else if (c == '#' || c == '/' && next == '/') {
                lineComment = true;
            }
            else if (c == '(' || c == '[' || c == '{') {
                depth++;
            }
            else if (c == ')' || c == ']' || c == '}') {
                depth--;
            }
            else if (c == ',' && depth == 0) {
                return true;
            }
        }
        return false;
    }
}
