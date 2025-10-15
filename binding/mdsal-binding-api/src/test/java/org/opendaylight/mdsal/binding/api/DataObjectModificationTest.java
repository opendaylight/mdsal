/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yang.gen.v1.lal.norev.Foo;
import org.opendaylight.yang.gen.v1.lal.norev.foo.Bar;
import org.opendaylight.yang.gen.v1.lal.norev.foo.BarKey;
import org.opendaylight.yang.gen.v1.mdsal533.norev.foo_cont.FooList;
import org.opendaylight.yangtools.binding.KeyStep;
import org.opendaylight.yangtools.binding.NodeStep;

@ExtendWith(MockitoExtension.class)
class DataObjectModificationTest {
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private DataObjectModification<Foo> deletedFoo;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private DataObjectModification<Bar> deletedBar;

    @Test
    void coerceGoodKeyStep() {
        final var step = new KeyStep<>(Bar.class, new BarKey(0));
        doReturn(step).when(deletedBar).step();
        assertSame(step, deletedBar.coerceKeyStep(Bar.class));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void coerceNonEntryObject() {
        final DataObjectModification deleted = deletedFoo;
        assertThrows(ClassCastException.class, () -> deleted.coerceKeyStep(String.class));
    }

    @Test
    void coerceMismatchedKeyStep() {
        final var step = new KeyStep<>(Bar.class, new BarKey(0));
        doReturn(step).when(deletedBar).step();

        final var ex = assertThrows(IllegalArgumentException.class, () -> deletedBar.coerceKeyStep(FooList.class));
        assertEquals("""
            KeyStep[type=interface org.opendaylight.yang.gen.v1.lal.norev.foo.Bar, caseType=null, key=BarKey{id=0}] \
            does not match type org.opendaylight.yang.gen.v1.mdsal533.norev.foo_cont.FooList""", ex.getMessage());
    }

    @Test
    void coerceNodeKeyStep() {
        doReturn(new NodeStep<>(Foo.class)).when(deletedFoo).step();

        final var ex = assertThrows(IllegalArgumentException.class, () -> deletedFoo.coerceKeyStep(Bar.class));
        assertEquals(
            "Cannot coerce NodeStep[type=interface org.opendaylight.yang.gen.v1.lal.norev.Foo, caseType=null]",
            ex.getMessage());
    }
}
