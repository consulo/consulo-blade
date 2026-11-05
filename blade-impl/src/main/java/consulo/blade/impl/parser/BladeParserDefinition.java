package consulo.blade.impl.parser;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.lexer.BladeLexer;
import com.jetbrains.php.blade.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.blade.impl.psi.*;
import consulo.language.Language;
import consulo.language.ast.*;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.*;
import consulo.language.psi.*;
import consulo.language.version.LanguageVersion;
import static com.jetbrains.php.blade.parser.BladeElementTypes.*;

@ExtensionImpl
public final class BladeParserDefinition implements ParserDefinition {
    @Override
    public Language getLanguage() {
        return BladeLanguage.INSTANCE;
    }

    @Override
    public Lexer createLexer(LanguageVersion version) {
        return new BladeLexer();
    }

    @Override
    public PsiParser createParser(LanguageVersion version) {
        return new BladeParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public TokenSet getWhitespaceTokens(LanguageVersion version) {
        return TokenSet.create(BladeTokenTypes.WHITE_SPACE);
    }

    @Override
    public TokenSet getCommentTokens(LanguageVersion version) {
        return TokenSet.create(BladeTokenTypes.BLADE_COMMENT);
    }

    @Override
    public TokenSet getStringLiteralElements(LanguageVersion version) {
        return TokenSet.EMPTY;
    }

    @Override
    public PsiFile createFile(FileViewProvider provider) {
        return new BladePsiFileImpl(provider);
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        IElementType type = node.getElementType();
        if (type == DIRECTIVE) {
            return new BladePsiDirectiveImpl(node);
        }
        if (type == DIRECTIVE_PARAMETER) {
            return new BladePsiDirectiveParameterImpl(node);
        }
        if (type == TEXT_BLOCK) {
            return new BladePsiEchoImpl(node);
        }
        if (type == RAW_TEXT_BLOCK) {
            return new BladePsiRawEchoImpl(node);
        }
        if (type == PHP_BLOCK) {
            return new BladePsiPhpBlockImpl(node);
        }
        throw new IllegalArgumentException("Unknown Blade composite: " + type);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
