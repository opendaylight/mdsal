/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;

public interface DOMSchemaService extends DOMService<DOMSchemaService, DOMSchemaServiceExtension> {
    /**
     * Returns global schema context.
     *
     * @return schemaContext
     */
    EffectiveModelContext getGlobalContext();

    /**
     * Register a listener for changes in schema context.
     *
     * @param listener Listener which should be registered
     * @return Listener registration handle
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    @NonNull Registration registerSchemaContextListener(EffectiveModelContextListener listener);
}
