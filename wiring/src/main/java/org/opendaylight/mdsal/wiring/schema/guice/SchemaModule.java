/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.mdsal.wiring.schema.SchemaWiring;
import org.opendaylight.mdsal.wiring.schema.YangRegisterer;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * Guice Module which Guice binds a {@link SchemaWiring}.
 *
 * @author Michael Vorburger.ch
 */
public class SchemaModule implements Module {

    private final SchemaWiring schemaWiring;

    public SchemaModule(SchemaWiring schemaWiring) {
        this.schemaWiring = schemaWiring;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(SchemaContextProvider.class).toInstance(schemaWiring.getSchemaContextProvider());
        binder.bind(new TypeLiteral<SchemaSourceProvider<YangTextSchemaSource>>() {})
            .toInstance(schemaWiring.getSchemaSourceProvider());

        binder.bind(DOMYangTextSourceProvider.class).toInstance(schemaWiring.getDOMYangTextSourceProvider());
        binder.bind(DOMSchemaService.class).toInstance(schemaWiring.getDOMSchemaService());

        schemaWiring.getYangRegisterer().ifPresent(
            yangRegisterer -> binder.bind(YangRegisterer.class).toInstance(yangRegisterer));
    }
}
