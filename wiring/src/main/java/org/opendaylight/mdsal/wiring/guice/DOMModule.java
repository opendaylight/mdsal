/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.wiring.schema.SchemaWiring;
import org.opendaylight.mdsal.wiring.schema.guice.SchemaModule;

/**
 * Guice Module which Guice binds mdsal DOM related services.
 *
 * <p>This modules expects another module to bind a {@link DOMDataBroker}.
 *
 * @author Michael Vorburger.ch
 */
public class DOMModule implements Module {

    private final SchemaWiring schemaWiring;

    public DOMModule(SchemaWiring schemaWiring) {
        this.schemaWiring = schemaWiring;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(SchemaWiring.class).toInstance(schemaWiring);
        binder.install(new SchemaModule(schemaWiring));
    }
}
