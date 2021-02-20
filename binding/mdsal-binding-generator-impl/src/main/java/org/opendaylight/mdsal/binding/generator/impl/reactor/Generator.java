/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A single node in generator tree. Each node will eventually resolve to a generated Java class. Each node also can have
 * a number of children, which are generators corresponding to the YANG subtree of this node.
 */
public abstract class Generator<T extends EffectiveStatement<?, ?>> implements Iterable<Generator<?>> {
    private final @NonNull T statement;

    private JavaTypeName typeName;
    private String assignedName;

    Generator(final T statement) {
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
    public Iterator<Generator<?>> iterator() {
        return Collections.emptyIterator();
    }

    final @NonNull String assignedName() {
        final String local = assignedName;
        checkState(local != null, "Attempted to access simple name of %s", this);
        return local;
    }

    final void setSimpleName(final String name) {
        checkState(assignedName == null, "Attempted to simple name %s with %s in %s", assignedName, name, this);
        this.assignedName = requireNonNull(name);
    }

    final @NonNull JavaTypeName typeName() {
        final JavaTypeName local = typeName;
        checkState(local != null, "Attempted to access Java type of %s", this);
        return local;
    }

    final void setTypeName(final JavaTypeName name) {
        checkState(typeName == null, "Attempted to replace Java type %s with %s in %s", typeName, name, this);
        this.typeName = requireNonNull(name);
    }

    /**
     * Return the namespace of this statement.
     *
     * @return Corresponding namespace
     * @throws UnsupportedOperationException if this node does not have a corresponding namespace
     */
    @NonNull StatementNamespace namespace() {
        return StatementNamespace.DEFAULT;
    }

    /**
     * Return the preferred Java class name based on Simple Name Mapping rules. This name is used for class name if it
     * does not incur a conflict among siblings.
     *
     * @return Preferred class name
     */
    @NonNull String preferredName() {
        // FIXME: this should be done in a nicer way
        final Object argument = statement.argument();
        verify(argument instanceof QName, "Illegal argument %s", argument);
        return BindingMapping.getClassName((QName) argument);
    }
}
