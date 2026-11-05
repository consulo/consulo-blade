package consulo.blade.it;

import com.jetbrains.php.blade.injection.BladeVariableTypeProvider;
import com.jetbrains.php.blade.psi.BladeFileImpl;
import com.jetbrains.php.blade.psi.BladePsiLanguageInjectionHost;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.Variable;
import consulo.application.ReadAction;
import consulo.it.AllowLogError;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(HeadlessProjectExtension.class)
@AllowLogError("consulo.xml.impl.internal.ExternalResource")
public class BladePhpIntegrationTest {
    @Test public void injectionRebuildsAfterEditing(CodeInsightTestFixture fixture) throws Exception {
        var file = fixture.configureByText("edit.blade.php", "@php $before = 1; @endphp {{ $before }}");
        ReadAction.run(() -> {
            var host = PsiTreeUtil.findChildOfType(file, BladePsiLanguageInjectionHost.class);
            assertNotNull(InjectedLanguageManager.getInstance(file.getProject()).getInjectedPsiFiles(host));
        });
        var document = fixture.getDocument();
        consulo.application.WriteAction.run(() -> consulo.application.Application.get()
            .getInstance(consulo.application.internal.TransferredWriteActionService.class)
            .runOnEdtWithTransferredWriteActionAndWait(() -> {
                document.setText("@php $afterx = 2; @endphp {{ $afterx }}");
                PsiDocumentManager.getInstance(file.getProject()).commitDocument(document);
            }));
        ReadAction.run(() -> {
            var manager = InjectedLanguageManager.getInstance(file.getProject());
            var hosts = PsiTreeUtil.findChildrenOfType(file, BladePsiLanguageInjectionHost.class);
            PsiFile shared = null;
            for (var host : hosts) {
                var injected = manager.getInjectedPsiFiles(host);
                assertNotNull(injected);
                var php = injected.getFirst().getFirst().getContainingFile();
                assertTrue(php.isValid());
                assertTrue(php.getText().contains("$afterx = 2"));
                assertFalse(php.getText().contains("$before"));
                if (shared == null) shared = php;
                else assertSame(shared, php);
            }
        });
    }

    @Test public void injectionUsesTheModulePhpLevel(CodeInsightTestFixture fixture) throws Exception {
        consulo.application.WriteAction.run(() -> {
            var model = consulo.module.content.ModuleRootManager.getInstance(fixture.getModule()).getModifiableModel();
            var extension = (consulo.php.impl.module.extension.PhpMutableModuleExtensionImpl)
                model.getExtensionWithoutCheck(consulo.php.module.extension.PhpMutableModuleExtension.class);
            assertNotNull(extension);
            extension.setEnabled(true);
            extension.getInheritableLanguageLevel().set(null, "7.4");
            model.commit();
        });
        var file = fixture.configureByText("version.blade.php", "@inject('service', 'App\\\\Service') {{ $service }}");
        ReadAction.run(() -> {
            var manager = InjectedLanguageManager.getInstance(file.getProject());
            var host = PsiTreeUtil.findChildOfType(file, BladePsiLanguageInjectionHost.class);
            var injected = manager.getInjectedPsiFiles(host);
            assertNotNull(injected);
            var php = injected.getFirst().getFirst().getContainingFile();
            assertSame(com.jetbrains.php.config.PhpLanguageLevel.PHP_7_4, php.getLanguageVersion());
            var variable = PsiTreeUtil.findChildrenOfType(php, Variable.class).stream()
                .filter(v -> "service".equals(v.getName())).findFirst().orElseThrow();
            assertSame(file, BladeVariableTypeProvider.getHostPhpFileForInjectedIfExists(variable));
        });
    }
    @Test public void allHostsShareOnePhpFile(CodeInsightTestFixture fixture) throws Exception {
        String text = "@php $users = []; @endphp @foreach($users as $user){{ $user }}@endforeach";
        var file = fixture.configureByText("example.blade.php", text);
        ReadAction.run(() -> {
            assertInstanceOf(BladeFileImpl.class, file);
            var manager = InjectedLanguageManager.getInstance(file.getProject());
            var hosts = PsiTreeUtil.findChildrenOfType(file, BladePsiLanguageInjectionHost.class);
            PsiFile shared = null;
            for (var host : hosts) {
                var injected = manager.getInjectedPsiFiles(host);
                assertNotNull(injected, host.getText());
                assertEquals(1, injected.size());
                var php = injected.getFirst().getFirst().getContainingFile();
                assertSame(PhpLanguage.INSTANCE, php.getLanguage());
                assertTrue(manager.isInjectedFragment(php));
                assertSame(file, BladeVariableTypeProvider.getHostPhpFileForInjectedIfExists(php));
                assertTrue(PsiTreeUtil.findChildrenOfType(php, PsiErrorElement.class).isEmpty(), php.getText());
                if (shared == null) shared = php;
                else assertSame(shared, php);
            }
            assertEquals(3, hosts.size());
            assertTrue(shared.getText().contains("endforeach;"));
            var injectedLeaf = manager.findInjectedElementAt(file, text.indexOf("$user }}") + 1);
            assertNotNull(injectedLeaf);
            assertSame(shared, injectedLeaf.getContainingFile(), () -> injectedLeaf + " text=" + injectedLeaf.getText() + " offset=" + injectedLeaf.getTextOffset());
            var variable = PsiTreeUtil.getParentOfType(injectedLeaf, Variable.class, false);
            assertNotNull(variable);
            assertEquals("user", variable.getName());
            assertFalse(variable.resolveLocal().isEmpty(), "echo must resolve the foreach variable");
            assertSame(file, BladeVariableTypeProvider.getHostPhpFileForInjectedIfExists(injectedLeaf));
            assertNull(BladeVariableTypeProvider.getHostPhpFileForInjectedIfExists(file));
        });
    }
}
