/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import java.util.Optional;
import org.opendaylight.mdsal.binding.generator.impl.FixedModuleInfoSchemaContextProvider;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.mdsal.dom.spi.schema.FixedDOMSchemaService;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * {@link SchemaWiring} which only supports YANG models from build time generated code (via DOM and binding).
 *
 * <p>Use {@link PurelyDynamicSchemaWiring} if instead you want to only load YANG models at run-time.
 *
 * @author Michael Vorburger.ch
 */
public class PurelyClassLoadingSchemaWiring implements SchemaWiring {

    private final FixedModuleInfoSchemaContextProvider schemaContextProvider
        = new FixedModuleInfoSchemaContextProvider();

    private final FixedDOMSchemaService schemaService
        = new FixedDOMSchemaService(schemaContextProvider, schemaContextProvider);

    @Override
    public SchemaContextProvider getSchemaContextProvider() {
        return schemaContextProvider;
    }

    @Override
    public SchemaSourceProvider<YangTextSchemaSource> getSchemaSourceProvider() {
        return schemaContextProvider;
    }

    @Override
    public DOMSchemaService getDOMSchemaService() {
        return schemaService;
    }

    @Override
    public DOMYangTextSourceProvider getDOMYangTextSourceProvider() {
        return schemaService;
    }

    @Override
    public Optional<YangRegisterer> getYangRegisterer() {
        return Optional.empty();
    }
}
