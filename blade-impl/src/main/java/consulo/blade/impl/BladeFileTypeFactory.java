package consulo.blade.impl;

import com.jetbrains.php.blade.BladeFileType;
import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.*;

@ExtensionImpl
public final class BladeFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(FileTypeConsumer consumer) {
        consumer.consume(BladeFileType.INSTANCE, FileNameMatcherFactory.getInstance().createMatcher("*.blade.php"));
    }
}
