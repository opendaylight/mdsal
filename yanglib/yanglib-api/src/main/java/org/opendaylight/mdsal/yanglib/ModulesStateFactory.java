/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
@NonNullByDefault
public interface ModulesStateFactory {
    /**
     * Create 'modules-state' top-level container for specified {@link SchemaContext}.
     *
     * @param schemaContext Input SchemaContext.
     * @return A container node
     * @throws NullPointerException if {@code schemaContext} is null
     */
    ContainerNode createModulesState(SchemaContext schemaContext);
}
