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
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import javax.inject.Inject;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.mdsal.wiring.guice.BindingModule;
import org.opendaylight.mdsal.wiring.guice.InMemoryDOMDataBrokerModule;
import org.opendaylight.mdsal.wiring.schema.PurelyClassLoadingSchemaWiring;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont.VlanId;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.ContBuilder;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Id;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * Tests all the *Wiring and *Module classes in mdsal, for applications using binding.
 *
 * <p>See the {@link DOMWiringTest} and {@link HybridWiringTest} for alternative wirings.
 *
 * @author Michael Vorburger.ch
 */
public class BindingWiringTest {

    // yangtools
    @Inject SchemaContextProvider schemaContextProvider;
    @Inject SchemaSourceProvider<YangTextSchemaSource> schemaSourceProvider;

    // DOM API
    @Inject DOMDataBroker domDataBroker;
    @Inject DOMSchemaService domSchemaService;
    @Inject DOMYangTextSourceProvider domYangTextSourceProvider;
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
        Injector injector = Guice.createInjector(PRODUCTION, new InMemoryDOMDataBrokerModule(),
                new BindingModule(new PurelyClassLoadingSchemaWiring()));
        injector.injectMembers(this);

        assertEquals(true, Scopes.isSingleton(injector.getBinding(DataBroker.class)));

        testDataBroker(dataBroker);
    }

    static void testDataBroker(DataBroker db) throws Exception {
        ReadWriteTransaction tx = db.newReadWriteTransaction();
        tx.put(CONFIGURATION,
               InstanceIdentifier.create(Cont.class),
               new ContBuilder().setVlanId(new VlanId(new Id(123))).build());
        tx.commit().get();
    }
}
