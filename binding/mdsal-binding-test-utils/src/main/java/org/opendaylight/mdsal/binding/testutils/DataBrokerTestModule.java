/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import java.util.function.Supplier;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.dom.adapter.CurrentAdapterSerializer;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.broker.DOMNotificationRouter;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

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
    @SuppressWarnings("checkstyle:IllegalCatch")
    public DataBroker getDataBroker() {
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
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
    }

    public DOMDataBroker getDOMDataBroker() {
        return dataBrokerTest.getDomBroker();
    }

    public CurrentAdapterSerializer getBindingToNormalizedNodeCodec() {
        return dataBrokerTest.getDataBrokerTestCustomizer().getAdapterContext().currentSerializer();
    }

    public DOMNotificationRouter getDOMNotificationRouter() {
        return dataBrokerTest.getDataBrokerTestCustomizer().getDomNotificationRouter();
    }

    public DOMSchemaService getSchemaService() {
        return dataBrokerTest.getDataBrokerTestCustomizer().getSchemaService();
    }

    public Supplier<EffectiveModelContext> getModelContextSupplier() {
        return dataBrokerTest.getDataBrokerTestCustomizer().getSchemaService()::getGlobalContext;
    }
}
