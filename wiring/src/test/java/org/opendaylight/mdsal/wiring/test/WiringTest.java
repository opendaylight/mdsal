/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.test;

import static com.google.inject.Stage.PRODUCTION;
import static org.junit.Assert.assertEquals;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import javax.inject.Inject;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.broker.schema.ScanningSchemaServiceProvider;
import org.opendaylight.mdsal.wiring.guice.InMemoryDOMDataBrokerModule;
import org.opendaylight.mdsal.wiring.guice.MdsalModule;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Tests all the "Wiring classes in mdsal.
 *
 * @author Michael Vorburger.ch
 */
public class WiringTest {

    // DOM API
    @Inject DOMDataBroker domDataBroker;
    @Inject DOMSchemaService domSchemaService;
    // TODO @Inject SchemaContextProvider schemaContextProvider;
    // TODO @Inject DOMNotificationService domNotificationService;
    // TODO @Inject DOMNotificationPublishService domNotificationPublishService;
    // TODO @Inject DOMNotificationRouter domNotificationRouter;
    // TODO @Inject DOMDataTreeService domDataTreeService;
    // TODO @Inject DOMMountPointService domMountPointService;
    // TODO @Inject DOMRpcService domRpcService;
    // TODO @Inject DOMRpcProviderService domRpcProviderService;
    // TODO @Inject DOMActionService domActionService;
    // TODO @Inject DOMActionProviderService domActionProviderService;

    // DOM SPI (services required e.g. by netconf/restconf)
    // TODO DOMNotificationSubscriptionListenerRegistry domNotificationSubscriptionListenerRegistry;

    // Binding
    @Inject DataBroker dataBroker;
    @Inject BindingNormalizedNodeSerializer codec; // NOT BindingToNormalizedNodeCodec, that's impl, this is API
    // TODO @Inject NotificationService notificationService;
    // TODO @Inject NotificationPublishService notificationPublishService;
    // TODO DataTreeService dataTreeService;
    // TODO @Inject MountPointService mountPointService;
    // TODO @Inject RpcConsumerRegistry rpcConsumerRegistry;
    // TODO @Inject RpcProviderService rpcProviderService;
    // TODO @Inject ActionService actionService;
    // TODO @Inject ActionProviderService actionProviderService;

    @Test
    public void testWiring() throws Exception {
        // Real world ODL SDN application tests use the JUnit @Rule GuiceRule from infrautils.inject.guice.testutils,
        // but for this test who's only purpose it is to test the modules, this "manual" simple Guice use is just fine.
        Injector injector = Guice.createInjector(PRODUCTION, new InMemoryDOMDataBrokerModule(), new MdsalModule());
        injector.injectMembers(this);

        assertEquals(true, Scopes.isSingleton(injector.getBinding(DataBroker.class)));

        // TODO This needs to happen in a Module or Wiring, not here...
        Hack hack = new Hack();
        SchemaContext schemaContext = hack.getSchemaContext();
        // NOK: ((ScanningSchemaServiceProvider) domSchemaService).tryToUpdateSchemaContext();
        ((ScanningSchemaServiceProvider) domSchemaService).notifyListeners(schemaContext);
        dataBroker.newReadWriteTransaction().commit().get();
    }

    private static class Hack extends AbstractSchemaAwareTest {

        @Override
        protected void setupWithSchema(SchemaContext context) {
            // NOOP
        }
    }
}
