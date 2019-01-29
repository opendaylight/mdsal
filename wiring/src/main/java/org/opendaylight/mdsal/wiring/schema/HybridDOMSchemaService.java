/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import java.util.Optional;
import org.opendaylight.mdsal.dom.broker.schema.ScanningSchemaServiceProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;

/**
 * {@link ScanningSchemaServiceProvider} which gets the SchemaContext to notify
 * {@link SchemaContextListener}s with from a delegated
 * {@link SchemaContextProvider}. This is used by {@link HybridSchemaWiring}.
 *
 * @author Michael Vorburger.ch
 */
class HybridDOMSchemaService extends ScanningSchemaServiceProvider {
    // intentionally package local here, for now

    private final SchemaContextProvider hybridSchemaContextProvider;

    HybridDOMSchemaService(SchemaContextProvider hybridSchemaContextProvider) {
        this.hybridSchemaContextProvider = hybridSchemaContextProvider;
    }

    @Override
    protected Optional<SchemaContext> internalGetSchemaContextToNotify() {
        return Optional.of(hybridSchemaContextProvider.getSchemaContext());
    }
}
