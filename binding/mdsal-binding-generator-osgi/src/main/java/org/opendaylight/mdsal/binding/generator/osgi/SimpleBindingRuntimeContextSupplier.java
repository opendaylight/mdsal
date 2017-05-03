/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.osgi;

import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContextSupplier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A simple synchronized implementation of a {@link BindingRuntimeContextSupplier}. It allows its
 * {@link BindingRuntimeContext} to be updated with a combination of a {@link ClassLoadingStrategy}
 * and a {@link SchemaContext}.
 *
 * @author Robert Varga
 */
final class SimpleBindingRuntimeContextSupplier implements BindingRuntimeContextSupplier {
    private BindingRuntimeContext current;

    @Override
    public synchronized BindingRuntimeContext get() {
        Preconditions.checkState(current != null, "Binding context not yet initialized");
        return current;
    }

    synchronized void update(final ClassLoadingStrategy classLoadingStrategy,
            final SchemaContext schemaContext) {
        current = BindingRuntimeContext.create(classLoadingStrategy, schemaContext);
    }

    synchronized void close() {
        current = null;
    }
}
