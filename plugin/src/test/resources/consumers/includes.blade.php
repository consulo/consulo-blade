@include('foobar.include')
@includeIf( 'foobar.includeIf' , ['some' => 'data'])
@includeWhen($boolean, 'foobar.includeWhen', ['some' => 'data'])
@includeFirst(['foobar.includeFirst', 'foobar.includeFirst2'], ['some' => 'data'])
@includeFirst(   [    'foobar.includeFirst3'    , "foobar.includeFirst4"]    )
@component('foobar.component')
<h1>Home Page</h1>@endcomponent
