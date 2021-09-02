/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

final class TargetAugmentEffectiveStatement implements AugmentEffectiveStatement {
    private final @NonNull List<EffectiveStatement<?, ?>> substatements;
    private final @NonNull AugmentEffectiveStatement delegate;

    TargetAugmentEffectiveStatement(final AugmentEffectiveStatement augment,
            final SchemaTreeAwareEffectiveStatement<?, ?> target) {
        delegate = requireNonNull(augment);

        final var stmts = augment.effectiveSubstatements();
        final var builder = ImmutableList.<EffectiveStatement<?, ?>>builderWithExpectedSize(stmts.size());
        for (var stmt : stmts) {
            if (stmt instanceof SchemaTreeEffectiveStatement) {
                final var qname = ((SchemaTreeEffectiveStatement<?>) stmt).getIdentifier();
                target.get(SchemaTreeNamespace.class, qname).ifPresent(builder::add);
            } else {
                builder.add(stmt);
            }
        }

        substatements = builder.build();
    }

    @Override
    public AugmentStatement getDeclared() {
        return delegate.getDeclared();
    }

    @Override
    public SchemaNodeIdentifier argument() {
        return delegate.argument();
    }

    @Override
    public StatementOrigin statementOrigin() {
        return delegate.statementOrigin();
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Optional<V> get(final Class<N> namespace, final K identifier) {
        return Optional.empty();
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        return Map.of();
    }

    @Override
    public List<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return substatements;
    }
}
