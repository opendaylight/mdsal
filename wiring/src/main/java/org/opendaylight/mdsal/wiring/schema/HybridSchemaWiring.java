/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import java.util.Optional;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * {@link SchemaWiring} which combines the
 * {@link PurelyClassLoadingSchemaWiring} and the
 * {@link PurelyDynamicSchemaWiring}.
 *
 * @author Michael Vorburger.ch
 */
public class HybridSchemaWiring implements SchemaWiring {

    private final SchemaWiring dynamicSchemaWiring;
    private final SchemaWiring fixedSchemaWiring;

    protected HybridSchemaWiring(
            PurelyDynamicSchemaWiring dynamicSchemaWiring, PurelyClassLoadingSchemaWiring fixedSchemaWiring) {
        this.dynamicSchemaWiring = dynamicSchemaWiring;
        this.fixedSchemaWiring = fixedSchemaWiring;
    }

    public HybridSchemaWiring() {
        this(new PurelyDynamicSchemaWiring(), new PurelyClassLoadingSchemaWiring());
    }

    @Override
    public SchemaContextProvider getSchemaContextProvider() {
        return () -> new HybridSchemaContext(
            dynamicSchemaWiring.getSchemaContextProvider().getSchemaContext(),
            fixedSchemaWiring.getSchemaContextProvider().getSchemaContext());
    }

    @Override
    public SchemaSourceProvider<YangTextSchemaSource> getSchemaSourceProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DOMSchemaService getDOMSchemaService() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DOMYangTextSourceProvider getDOMYangTextSourceProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<YangRegisterer> getYangRegisterer() {
        return dynamicSchemaWiring.getYangRegisterer();
    }
}
