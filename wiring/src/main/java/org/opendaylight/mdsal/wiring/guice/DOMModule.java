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
import com.google.inject.Provides;
import javax.inject.Singleton;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.mdsal.wiring.DOMWiring;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * Guice Module which Guice binds mdsal DOM related services.
 *
 * <p>This modules expects another module to bind a {@link DOMDataBroker}.
 *
 * @author Michael Vorburger.ch
 */
public class DOMModule implements Module {

    @Override
    public void configure(Binder binder) {
    }

    @Provides
    @Singleton SchemaContextProvider getSchemaContextProvider(DOMWiring mdsalWiring) {
        return mdsalWiring.getSchemaContextProvider();
    }

    @Provides
    @Singleton SchemaSourceProvider<YangTextSchemaSource> getSchemaSourceProvider(DOMWiring mdsalWiring) {
        return mdsalWiring.getSchemaSourceProvider();
    }

    @Provides
    @Singleton DOMYangTextSourceProvider getDOMYangTextSourceProvider(DOMWiring mdsalWiring) {
        return mdsalWiring.getDOMYangTextSourceProvider();
    }

    @Provides
    @Singleton DOMSchemaService getSchemaService(DOMWiring mdsalWiring) {
        return mdsalWiring.getDOMSchemaService();
    }
}
