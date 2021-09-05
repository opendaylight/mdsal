/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Deque;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.YangStatementPath;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

@Beta
@NonNullByDefault
public abstract class NestedYangStatementPath implements YangStatementPath {
    final EffectiveStatement<?, ?> statement;

    NestedYangStatementPath(final EffectiveStatement<?, ?> statement) {
        this.statement = requireNonNull(statement);
    }

    public static NestedYangStatementPath of(final EffectiveModelContext modelContext,
            final ModuleEffectiveStatement module) {
        return new ModuleYangStatementPath(modelContext, module);
    }

    public static NestedYangStatementPath of(final EffectiveModelContext modelContext,
            final ModuleEffectiveStatement module, final EffectiveStatement<?, ?>... others) {
        NestedYangStatementPath ret = of(modelContext, module);
        for (EffectiveStatement<?, ?> other : others) {
            ret = ret.createChild(other);
        }
        return ret;
    }

    public NestedYangStatementPath createChild(final EffectiveStatement<?, ?> statement) {
        return new SubstatementYangStatementPath(this, statement);
    }

    void appendTo(final Deque<EffectiveStatement<?, ?>> deque) {
        deque.addFirst(statement);
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("path", statementPath()).toString();
    }
}
