package consulo.blade.ide.highlight;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.colorScheme.setting.ColorDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.localize.LocalizeValue;
import static consulo.blade.ide.highlight.BladeHighlighterColors.*;

@ExtensionImpl
public final class BladeColorsPage implements ColorSettingsPage {
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.of("Blade");
    }

    @Override
    public SyntaxHighlighter getHighlighter() {
        return new BladeSyntaxHighlighter();
    }

    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return new AttributesDescriptor[]{
            new AttributesDescriptor(LocalizeValue.of("Directive"), DIRECTIVE),
            new AttributesDescriptor(LocalizeValue.of("Echo braces"), ECHO_BRACES),
            new AttributesDescriptor(LocalizeValue.of("Raw echo braces"), RAW_ECHO_BRACES),
            new AttributesDescriptor(LocalizeValue.of("Comment"), COMMENT),
            new AttributesDescriptor(LocalizeValue.of("Parameter parentheses"), PARAMETER_BRACES),
            new AttributesDescriptor(LocalizeValue.of("PHP background"), PHP_BACKGROUND)
        };
    }

    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public String getDemoText() {
        return """
            @extends('layouts.app')
            @section('content')
                {{-- A Blade comment --}}
                @foreach($users as $user)
                    <p>{{ $user->name }}</p>
                    {!! $user->bio !!}
                @endforeach
                @php $title = 'Users'; @endphp
            @endsection
            """;
    }
}
