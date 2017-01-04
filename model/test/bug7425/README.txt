This directory is only for Bug reporting purposes.
It should NOT be part of ODL as is,
it should be reworked into a lightweight unit test instead.

Build of this should fail unless Bug 7425 is fixed.

The tricky thing is that java code generation passes fine,
only its subsequent compilation fails,
that is why usual unit tests are not easily editable to test this.
