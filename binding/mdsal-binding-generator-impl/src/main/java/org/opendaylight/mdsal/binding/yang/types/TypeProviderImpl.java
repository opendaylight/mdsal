/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * {@link AbstractTypeProvider} which generates full metadata, suitable for codegen purposes. For runtime purposes,
 * considering using {@link RuntimeTypeProvider}.
 *
 * @deprecated Use {@link CodegenTypeProvider} instead.
 */
@Deprecated
public final class TypeProviderImpl extends CodegenTypeProvider {
    /**
     * Creates new instance of class <code>TypeProviderImpl</code>.
     *
     * @param schemaContext contains the schema data read from YANG files
     * @throws IllegalArgumentException if <code>schemaContext</code> is null.
     */
    public TypeProviderImpl(final SchemaContext schemaContext) {
        super(schemaContext);
    }
}
