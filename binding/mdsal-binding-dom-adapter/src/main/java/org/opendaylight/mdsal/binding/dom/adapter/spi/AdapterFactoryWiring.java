/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.spi;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.dom.adapter.BindingWiring;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;

/**
 * Wiring for dependency injection (DI).
 *
 * @see BindingWiring
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class AdapterFactoryWiring {

    private final DataBroker dataBroker;

    @Inject
    public AdapterFactoryWiring(BindingWiring bindingWiring, DOMDataBroker domDataBroker) {
        AdapterFactory adapterFactory = bindingWiring.getAdapterFactory();
        dataBroker = adapterFactory.createDataBroker(domDataBroker);
    }

    public DataBroker getDataBroker() {
        return dataBroker;
    }
}
