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
import org.opendaylight.mdsal.wiring.InMemoryDOMDataBrokerWiring;

/**
 * Guice Module which binds an in-memory DOMDataBroker.
 *
 * @author Michael Vorburger.ch
 */
public class InMemoryDOMDataBrokerModule implements Module {

    // see comment in InMemoryDOMDataBrokerWiring re. why this class is in this package

    @Override
    public void configure(Binder binder) {
    }

    @Provides
    @Singleton DOMDataBroker getDOMDataBroker(InMemoryDOMDataBrokerWiring inMemoryWiring) {
        return inMemoryWiring.getDOMDataBroker();
    }
}
