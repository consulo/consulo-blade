Consumer compatibility fixtures from the local reference projects:

- `component.blade.php`: Laravel plugin's `BladePsiUtilTest` fixture.
- `includes.blade.php`: inline source from `BladeIncludeStubIndexTest.testIndex`.
- `each.blade.php`: sources from `BladeEachStubIndexTest` and `BladePsiUtilTest`.
- `slot.blade.php`: `BladePsiUtilTest.testFindComponentForSlotScope`, with the caret marker removed.

These tests verify Blade's syntax/PSI contracts. The consumers' indexes and references will be tested when they are ported.
