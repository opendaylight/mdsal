/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractConcurrentDataBrokerTest;

public class DataBrokerTestModule {
    private final boolean useMTDataTreeChangeListenerExecutor;

    public DataBrokerTestModule(final boolean useMTDataTreeChangeListenerExecutor) {
        this.useMTDataTreeChangeListenerExecutor = useMTDataTreeChangeListenerExecutor;
    }

    public static DataBroker dataBroker() {
        return new DataBrokerTestModule(false).getDataBroker();
    }

    // Suppress IllegalCatch because of AbstractDataBrokerTest (change later)
    @SuppressWarnings({ "checkstyle:IllegalCatch", "checkstyle:IllegalThrows" })
    public DataBroker getDataBroker() throws RuntimeException {
        try {
            // This is a little bit "upside down" - in the future,
            // we should probably put what is in AbstractDataBrokerTest
            // into this DataBrokerTestModule, and make AbstractDataBrokerTest
            // use it, instead of the way around it currently is (the opposite);
            // this is just for historical reasons... and works for now.
            AbstractConcurrentDataBrokerTest dataBrokerTest =
                    new AbstractConcurrentDataBrokerTest(useMTDataTreeChangeListenerExecutor) { };
            dataBrokerTest.setup();
            return dataBrokerTest.getDataBroker();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
