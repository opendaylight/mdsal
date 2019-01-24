/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import javax.inject.Singleton;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;

/**
 * Guice Module which binds an in-memory DOMDataBroker.
 *
 * @author Michael Vorburger.ch
 */
public class InMemoryModule implements Module {

    @Override
    public void configure(Binder binder) {
    }

    @Provides
    @Singleton DOMDataBroker getDOMDataBroker(InMemoryWiring inMemoryWiring) {
        return inMemoryWiring.getDOMDataBroker();
    }
}
