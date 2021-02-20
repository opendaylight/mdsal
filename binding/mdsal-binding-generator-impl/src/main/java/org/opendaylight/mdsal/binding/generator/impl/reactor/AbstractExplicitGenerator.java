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

import com.google.common.base.CharMatcher;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * An explicit {@link Generator}, associated with a particular {@link EffectiveStatement}.
 *
 */
abstract class AbstractExplicitGenerator<T extends EffectiveStatement<?, ?>> extends Generator {
    private static final CharMatcher DASH_MATCHER = CharMatcher.is('-');

    private final @NonNull T statement;

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
    String preferredName() {
        // FIXME: this should be done in a nicer way
        final Object argument = statement.argument();
        verify(argument instanceof QName, "Illegal argument %s", argument);
        return BindingMapping.getClassName((QName) argument);
    }

    @Override
    String preferredSubpackage() {
        // FIXME: this should be done in a nicer way
        final Object argument = statement.argument();
        verify(argument instanceof QName, "Illegal argument %s", argument);
        final String localName = ((AbstractQName) argument).getLocalName();

        // Strip dashes, as they are not legal in package names.
        return DASH_MATCHER.removeFrom(localName);
    }
}
