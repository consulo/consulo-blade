package com.jetbrains.php.blade.lexer;

import com.jetbrains.php.blade.psi.BladeTokenTypes;
import consulo.blade.BladeDirectives;
import consulo.language.ast.TokenSet;
import consulo.language.lexer.MergingLexerAdapter;

import java.util.Map;

public final class BladeLexer extends MergingLexerAdapter {
    public BladeLexer() {
        this(BladeDirectives.snapshot());
    }

    public BladeLexer(Map<String, BladeDirectives.Definition> directives) {
        super(new _BladeLexer(directives), TokenSet.create(BladeTokenTypes.HTML_TEMPLATE));
    }
}
