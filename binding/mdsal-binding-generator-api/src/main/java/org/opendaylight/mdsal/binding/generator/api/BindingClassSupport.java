/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
public class BindingClassSupport extends AbstractBindingClassSupport {
    private final ClassLoaderResolver classLoaderResolver;

    protected BindingClassSupport(final SchemaContext schemaContext, final BindingRuntimeTypes bindingTypes,
            final ClassLoaderResolver classLoaderResolver) {
        super(schemaContext, bindingTypes);
        this.classLoaderResolver = requireNonNull(classLoaderResolver);
    }

    public final ClassLoaderResolver getClassLoaderResolver() {
        return classLoaderResolver;
    }
}
