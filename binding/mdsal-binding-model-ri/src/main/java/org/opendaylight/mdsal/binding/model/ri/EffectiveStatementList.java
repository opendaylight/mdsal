/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.Spliterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@NonNullByDefault
final class EffectiveStatementList extends AbstractList<EffectiveStatement<?, ?>> implements Immutable {
    private static final EffectiveStatement<?, ?>[] EMPTY_STATEMENTS = new EffectiveStatement<?, ?>[0];
    private static final VarHandle STATEMENTS;

    static {
        try {
            STATEMENTS = MethodHandles.lookup().findVarHandle(EffectiveStatementList.class, "statements",
                EffectiveStatement[].class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final SubstatementYangStatementPath path;

    @SuppressWarnings("unused")
    private EffectiveStatement<?, ?> @Nullable [] statements;

    EffectiveStatementList(final SubstatementYangStatementPath path) {
        this.path = requireNonNull(path);
    }

    @Override
    public Iterator<EffectiveStatement<?, ?>> iterator() {
        return Arrays.asList(statements()).iterator();
    }

    @Override
    public Spliterator<EffectiveStatement<?, ?>> spliterator() {
        return Arrays.spliterator(statements());
    }

    @Override
    public EffectiveStatement<?, ?> get(final int index) {
        return statements()[index];
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return statements().length;
    }

    private EffectiveStatement<?, ?>[] statements() {
        final EffectiveStatement<?, ?>[] local = (EffectiveStatement<?, ?>[]) STATEMENTS.getAcquire(this);
        return local != null ? local : loadStatements();
    }

    private EffectiveStatement<?, ?>[] loadStatements() {
        final Deque<EffectiveStatement<?, ?>> tmp = new ArrayDeque<>();
        path.appendTo(tmp);
        final EffectiveStatement<?, ?>[] result = tmp.toArray(EMPTY_STATEMENTS);
        // We do not care about atomicity here, results are always equivalent
        STATEMENTS.setRelease(this, result);
        return result;
    }
}
