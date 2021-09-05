/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

@NonNullByDefault
final class ModuleYangStatementPath extends NestedYangStatementPath {
    private final EffectiveModelContext modelContext;

    ModuleYangStatementPath(final EffectiveModelContext modelContext, final ModuleEffectiveStatement module) {
        super(module);
        this.modelContext = requireNonNull(modelContext);
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return modelContext;
    }

    @Override
    public List<EffectiveStatement<?, ?>> statementPath() {
        return List.of(statement);
    }

    @Override
    public int hashCode() {
        return statement.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this
            || obj instanceof ModuleYangStatementPath && statement.equals(((ModuleYangStatementPath) obj).statement);
    }

    @Override
    @Nullable NestedYangStatementPath parent() {
        return null;
    }
}
