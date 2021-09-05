/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri;

import static java.util.Objects.requireNonNull;

import java.util.Deque;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@NonNullByDefault
final class SubstatementYangStatementPath extends NestedYangStatementPath {
    private final NestedYangStatementPath parent;

    SubstatementYangStatementPath(final NestedYangStatementPath parent, final EffectiveStatement<?, ?> statement) {
        super(statement);
        this.parent = requireNonNull(parent);
    }

    @Override
    public List<EffectiveStatement<?, ?>> statementPath() {
        // Short-circuit for top-level substatements
        return parent instanceof ModuleYangStatementPath ? List.of(parent.statement, statement)
            // Lazily evaluated
            : new EffectiveStatementList(this);
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return parent.getEffectiveModelContext();
    }

    @Override
    public int hashCode() {
        return parent.hashCode() * 31 + statement.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SubstatementYangStatementPath)) {
            return false;
        }
        final SubstatementYangStatementPath other = (SubstatementYangStatementPath) obj;
        return statement.equals(other.statement) && parent.equals(other.parent);
    }

    @Override
    void appendTo(final Deque<EffectiveStatement<?, ?>> deque) {
        super.appendTo(deque);
        parent.appendTo(deque);
    }
}
