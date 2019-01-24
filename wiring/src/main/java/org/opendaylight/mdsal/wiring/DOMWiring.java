/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring;

import org.opendaylight.mdsal.binding.generator.impl.FixedModuleInfoSchemaContextProvider;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.mdsal.dom.spi.schema.FixedDOMSchemaService;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * Wiring for DI of mdsal's DOM related services in a standalone environment.
 *
 * @author Michael Vorburger.ch
 */
public class DOMWiring {

    private final FixedModuleInfoSchemaContextProvider schemaContextProvider
        = new FixedModuleInfoSchemaContextProvider();

    private final FixedDOMSchemaService schemaService
        = new FixedDOMSchemaService(schemaContextProvider, schemaContextProvider);

    public SchemaContextProvider getSchemaContextProvider() {
        return schemaContextProvider;
    }

    public SchemaSourceProvider<YangTextSchemaSource> getSchemaSourceProvider() {
        return schemaContextProvider;
    }

    public DOMSchemaService getDOMSchemaService() {
        return schemaService;
    }

    public DOMYangTextSourceProvider getDOMYangTextSourceProvider() {
        return schemaService;
    }
}
