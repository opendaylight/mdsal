/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * An explicit {@link Generator}, associated with a particular {@link EffectiveStatement}.
 *
 */
abstract class AbstractExplicitGenerator<T extends EffectiveStatement<?, ?>> extends Generator {
    private final @NonNull T statement;

    private Optional<Member> member;

    AbstractExplicitGenerator(final T statement) {
        this.statement = requireNonNull(statement);
    }

    AbstractExplicitGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(parent);
        this.statement = requireNonNull(statement);
    }

    /**
     * Return the {@link EffectiveStatement} associated with this generator.
     *
     * @return An EffectiveStatement
     */
    public final @NonNull T statement() {
        return statement;
    }

    @Override
    boolean producesType() {
        // We process nodes introduced through augment or uses separately
        if (statement instanceof AddedByUsesAware && ((AddedByUsesAware) statement).isAddedByUses()
            || statement instanceof CopyableNode && ((CopyableNode) statement).isAugmenting()) {
            return false;
        }
        return true;
    }

    @Override
    final Member ensureMember() {
        if (member == null) {
            member = producesType() ? Optional.of(createMember()) : Optional.empty();
        }
        return member.orElse(null);
    }

    @NonNull Member createMember() {
        // FIXME: this should be done in a nicer way
        final Object argument = statement.argument();
        verify(argument instanceof AbstractQName, "Illegal argument %s", argument);
        return parent().domain().addPrimary(new CamelCaseNamingStrategy(namespace(), (AbstractQName) argument));
    }
}
