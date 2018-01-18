/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.broker.DOMNotificationRouter;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;

@Beta
public class DataBrokerTestModule {
    private final boolean useMTDataTreeChangeListenerExecutor;

    private AbstractConcurrentDataBrokerTest dataBrokerTest;

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
            dataBrokerTest = new AbstractConcurrentDataBrokerTest(useMTDataTreeChangeListenerExecutor) { };
            dataBrokerTest.setup();
            return dataBrokerTest.getDataBroker();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DOMDataBroker getDOMDataBroker() {
        return dataBrokerTest.getDomBroker();
    }

    public BindingToNormalizedNodeCodec getBindingToNormalizedNodeCodec() {
        return dataBrokerTest.getDataBrokerTestCustomizer().getBindingToNormalized();
    }

    public DOMNotificationRouter getDOMNotificationRouter() {
        return dataBrokerTest.getDataBrokerTestCustomizer().getDomNotificationRouter();
    }

    public DOMSchemaService getSchemaService() {
        return dataBrokerTest.getDataBrokerTestCustomizer().getSchemaService();
    }

    public SchemaContextProvider getSchemaContextProvider() {
        return dataBrokerTest.getDataBrokerTestCustomizer().getSchemaService()::getGlobalContext;
    }
}
