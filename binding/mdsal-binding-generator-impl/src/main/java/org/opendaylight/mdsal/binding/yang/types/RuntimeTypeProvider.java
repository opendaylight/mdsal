/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * {@link AbstractTypeProvider} which generates enough type information for runtime support. For a codegen-compatible
 * provider use {@link CodegenTypeProvider}.
 */
@Beta
public final class RuntimeTypeProvider extends AbstractTypeProvider {
    public RuntimeTypeProvider(final SchemaContext schemaContext) {
        super(schemaContext);
    }
}
