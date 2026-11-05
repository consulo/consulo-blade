package consulo.blade.ide;

import consulo.blade.BladeDirectivePairs;
import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.psi.*;
import consulo.language.psi.PsiFileFactory;
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
public class BladeDirectivePairsTest {
    public record Context(PsiFileFactory factory) implements InjectingRecord {
    }

    private BladeFileImpl parse(Context context, String text) {
        return (BladeFileImpl) context.factory().createFileFromText("pairs.blade.php", BladeLanguage.INSTANCE, text);
    }

    @Test
    public void nestedBranchesAndAlternateClosers(Context context) {
        var file = parse(
            context,
            "@section('content') @if($a) @foreach($items as $item) x @endforeach @elseif($b) y @else z @endif @show"
        );
        var view = BladeDirectivePairs.get(file);
        var pairs = view.pairs();
        assertEquals(List.of("@section", "@if", "@foreach"), pairs.stream().map(p -> p.opening().getName()).toList());
        assertEquals(List.of("@show", "@endif", "@endforeach"), pairs.stream().map(p -> p.closing().getName()).toList());
        assertEquals(List.of("@elseif", "@else"), pairs.get(1).branches().stream().map(BladePsiDirective::getName).toList());
        for (var directive : PsiTreeUtil.findChildrenOfType(file, BladePsiDirective.class)) {
            assertSame(file, directive.getParent());
            var pair = view.find(directive);
            assertNotNull(pair, directive.getText());
        }
    }

    @Test
    public void forelseEmptyIsContextual(Context context) {
        var file = parse(
            context,
            "@forelse($rows as $row) @empty($row) x @endempty @forelse($row as $cell) x @empty y @endforelse @empty z @endforelse"
        );
        var pairs = BladeDirectivePairs.get(file).pairs();
        assertEquals(List.of("@forelse", "@empty", "@forelse"), pairs.stream().map(p -> p.opening().getName()).toList());
        assertEquals(1, pairs.getFirst().branches().size());
        assertEquals(0, pairs.get(1).branches().size());
        assertEquals(1, pairs.get(2).branches().size());
        assertNotSame(pairs.getFirst().branches().getFirst(), pairs.get(2).branches().getFirst());
    }

    @Test
    public void inlineFormsDoNotOpenBlocks(Context context) {
        var file = parse(
            context,
            "@section('title', 'Hello') @slot('title', 'Card') @push('js', 'code') @prepend('js', 'code') @php($x = 1) @section(foo('a', 'b')) x @endsection @section('a,b') y @stop"
        );
        var pairs = BladeDirectivePairs.get(file).pairs();
        assertEquals(2, pairs.size());
        assertEquals("@section(foo('a', 'b'))", pairs.getFirst().opening().getText());
        assertEquals("@section('a,b')", pairs.get(1).opening().getText());
        var comments = parse(context, "@section(/* , */ 'title') x @endsection");
        assertEquals(1, BladeDirectivePairs.get(comments).pairs().size());
    }

    @Test
    public void malformedInputRecoversWithoutCrossingPairs(Context context) {
        var file = parse(context, "@endif @if($a) @foreach($xs as $x) @endif @endforeach @component('card')");
        var pairs = BladeDirectivePairs.get(file).pairs();
        assertEquals(3, pairs.size());
        assertEquals("@endif", pairs.getFirst().closing().getName());
        assertNull(pairs.get(1).closing());
        assertNull(pairs.get(2).closing());
        assertEquals(
            List.of("@component"),
            BladeDirectivePairs.get(file).enclosing(file.getTextLength()).stream().map(p -> p.opening().getName()).toList()
        );
    }

    @Test
    public void switchBranchesAndSharedComponentClose(Context context) {
        var file = parse(
            context,
            "@componentFirst(['a', 'b']) @slot('title') x @endslot @switch($x) @case(1) x @break @default y @endswitch @endcomponent"
        );
        var pairs = BladeDirectivePairs.get(file).pairs();
        assertEquals(3, pairs.size());
        assertEquals("@endcomponent", pairs.getFirst().closing().getName());
        assertEquals(List.of("@case", "@default"), pairs.get(2).branches().stream().map(BladePsiDirective::getName).toList());
    }

    @Test
    public void phpBlocksCommentsAndVerbatimContentAreOpaque(Context context) {
        var file = parse(context, "@php echo '@if($x)'; @endphp {{-- @if($x) --}} @verbatim @if($x) @endverbatim");
        var pairs = BladeDirectivePairs.get(file).pairs();
        assertEquals(1, pairs.size());
        assertEquals("@verbatim", pairs.getFirst().opening().getName());
        assertEquals("@endverbatim", pairs.getFirst().closing().getName());
    }
}
