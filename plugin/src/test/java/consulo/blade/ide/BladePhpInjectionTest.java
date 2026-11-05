package consulo.blade.ide;

import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.psi.*;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import consulo.blade.impl.inject.BladePhpInjector;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.InjectingRecord;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@NullUnmarked
@ExtendWith(ConsuloProjectLoader.class)
public class BladePhpInjectionTest {
    public record Context(PsiFileFactory factory) implements InjectingRecord {
    }

    private String compile(Context context, String blade) {
        var file = (BladeFileImpl) context.factory().createFileFromText("example.blade.php", BladeLanguage.INSTANCE, blade);
        StringBuilder php = new StringBuilder();
        int previousOffset = -1;
        for (var place : BladePhpInjector.collectPlaces(file)) {
            assertTrue(place.host().getTextRange().getStartOffset() > previousOffset);
            previousOffset = place.host().getTextRange().getStartOffset();
            var range = place.host().getContentRange();
            StringBuilder decoded = new StringBuilder();
            var escaper = place.host().createLiteralTextEscaper();
            assertTrue(escaper.decode(range, decoded));
            assertFalse(escaper.isOneLine());
            assertEquals(range.substring(place.host().getText()), decoded.toString());
            assertEquals(range.getStartOffset(), escaper.getOffsetInHost(0, range));
            php.append(place.prefix()).append(decoded).append(place.suffix());
        }
        return php.toString();
    }

    private PsiFile parsePhp(Context context, String blade) {
        String php = compile(context, blade);
        var file = context.factory().createFileFromText("compiled.php", PhpLanguage.INSTANCE, php);
        var errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class);
        assertTrue(
            errors.isEmpty(),
            () -> php + "\n" + errors.stream().map(e -> e.getErrorDescription() + " at " + e.getTextOffset()).toList()
        );
        return file;
    }

    @Test
    public void onePhpProgramAcrossHosts(Context context) {
        String blade = "@php $users = []; @endphp @foreach($users as $user)<p>{{ $user->name }}</p>@endforeach";
        String php = compile(context, blade);
        assertEquals(1, php.split("@var", -1).length - 1);
        assertTrue(php.indexOf("$users = []") < php.indexOf("foreach("));
        assertTrue(php.indexOf("foreach(") < php.indexOf("echo  $user->name"));
        assertTrue(php.endsWith("<?php endforeach; ?>"));
        parsePhp(context, blade);
    }

    @Test
    public void consumerMethodNamesAndArgumentIndexes(Context context) {
        var file = parsePhp(
            context,
            "@extends('base') @include('part') @includeWhen($ok, 'conditional') @each('row', $rows, 'row') @lang('a.b') @choice('a.b', 2)"
        );
        var calls = PsiTreeUtil.findChildrenOfType(file, MethodReference.class);
        var make = calls.stream().filter(c -> "make".equals(c.getName())).toList();
        assertEquals(2, make.size());
        var when = calls.stream().filter(c -> "renderWhen".equals(c.getName())).findFirst().orElseThrow();
        assertEquals("'conditional'", when.getParameters()[1].getText());
        var each = calls.stream().filter(c -> "renderEach".equals(c.getName())).findFirst().orElseThrow();
        assertEquals("'row'", each.getParameters()[0].getText());
        assertEquals("'row'", each.getParameters()[2].getText());
        assertTrue(file.getText().replace("\n", "").contains("\\Illuminate\\Support\\Facades\\Lang::get('a.b')"));
        assertTrue(file.getText().replace("\n", "").contains("\\Illuminate\\Support\\Facades\\Lang::choice('a.b', 2)"));
    }

    @Test
    public void branchesLoopsAndTrailingDirectives(Context context) {
        parsePhp(context, "@if($a){{ $a }}@elseif($b){!! $b !!}@else{{ $c }}@endif @unless($a){{ $a }}@endunless");
        parsePhp(
            context,
            "@forelse($rows as $row)@forelse($row as $cell){{ $cell }}@empty No cells @endforelse @empty No rows @endforelse"
        );
        parsePhp(context, "@switch($x)@case(1){{ $x }}@break @default{{ $y }}@endswitch");
        parsePhp(context, "@for($i = 0; $i < 2; $i++)@continue($i == 1){{ $i }}@endfor");
    }

    @Test
    public void injectionAndCustomDirectives(Context context) {
        parsePhp(context, "@inject('service', 'App\\\\Service') {{ $service->run() }} @datetime($date) @php($x = 1)");
        parsePhp(context, "@csrf @auth {{ $user }} @endauth @guest('web'){{ $guest }}@endguest");
        parsePhp(context, "@section('title'){{ $title }}@endsection @push('scripts'){{ $script }}@endpush");
        parsePhp(context, "@php $x = 1; // trailing comment @endphp {{ $x }}");
        parsePhp(context, "@section('title')@parent {{ $title }}@endsection @use('App\\\\Service')");
        assertEquals("", compile(context, "<p>HTML only</p> {{-- {{ ignored }} --}} @verbatim {{ ignored }} @endverbatim"));
    }
}
