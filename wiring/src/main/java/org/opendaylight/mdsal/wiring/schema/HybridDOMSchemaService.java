/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.mdsal.dom.spi.schema.AbstractDOMSchemaService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.NoOpListenerRegistration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * {@link DOMSchemaService} (and {@link DOMYangTextSourceProvider}) implementation
 * backed by two {@link SchemaContextProvider}s.
 *
 * @author Michael Vorburger.ch
 */
class HybridDOMSchemaService extends AbstractDOMSchemaService {
    // intentionally package local here, for now

    @GuardedBy("lock")
    private final ListenerRegistry<SchemaContextListener> listeners = ListenerRegistry.create();
    private final Object lock = new Object();

    private final DOMSchemaService scanningSchemaServiceProvider;
    private final DOMSchemaService fixedDOMSchemaService;

    HybridDOMSchemaService(SchemaContextProvider schemaContextProvider,
            SchemaSourceProvider<YangTextSchemaSource> schemaSourceProvider,
            DOMSchemaService scanningSchemaServiceProvider, DOMSchemaService fixedDOMSchemaService) {
        super(schemaContextProvider, schemaSourceProvider);
        this.scanningSchemaServiceProvider = scanningSchemaServiceProvider;
        this.fixedDOMSchemaService = fixedDOMSchemaService;
    }

    @Override
    public ListenerRegistration<SchemaContextListener> registerSchemaContextListener(SchemaContextListener listener) {
        // This is wrong, it won't work for the dynamic scenario with ScanningSchemaServiceProvider
        listener.onGlobalContextUpdated(getGlobalContext());
        return NoOpListenerRegistration.of(listener);
    }
}
