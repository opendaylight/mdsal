/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.schema;

import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.NoOpListenerRegistration;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * {@link DOMSchemaService} (and {@link DOMYangTextSourceProvider}) implementation
 * back by a {@link SchemaContextProvider} which is known to be fixed and never change schemas.

 * @author Michael Vorburger.ch
 */
public class FixedDOMSchemaService extends AbstractDOMSchemaService {

    public FixedDOMSchemaService(SchemaContextProvider schemaContextProvider,
            SchemaSourceProvider<YangTextSchemaSource> schemaSourceProvider) {
        super(schemaContextProvider, schemaSourceProvider);
    }

    @Override
    public ListenerRegistration<SchemaContextListener> registerSchemaContextListener(SchemaContextListener listener) {
        listener.onGlobalContextUpdated(getGlobalContext());
        return NoOpListenerRegistration.of(listener);
    }
}
