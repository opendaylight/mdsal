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
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.mdsal.wiring.guice.BindingModule;
import org.opendaylight.mdsal.wiring.guice.DOMModule;
import org.opendaylight.mdsal.wiring.guice.InMemoryDOMDataBrokerModule;
import org.opendaylight.mdsal.wiring.schema.PurelyDynamicSchemaWiring;
import org.opendaylight.mdsal.wiring.schema.YangRegisterer;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * Tests all (and only) all DOM API related *Wiring and *Module classes in mdsal.
 *
 * <p>Note how this test, contrary to the {@link BindingWiringTest}, does
 * not use the {@link BindingModule}, only the {@link DOMModule}.
 *
 * @author Michael Vorburger.ch
 */
public class DOMWiringTest {

    @Inject YangRegisterer yangRegisterer;

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

    @Test
    public void testWiring() throws Exception {
        // Real world ODL SDN application tests use the JUnit @Rule GuiceRule from infrautils.inject.guice.testutils,
        // but for this test who's only purpose it is to test the modules, this "manual" simple Guice use is just fine.
        Injector injector = Guice.createInjector(PRODUCTION, new InMemoryDOMDataBrokerModule(),
                new DOMModule(new PurelyDynamicSchemaWiring()));
        injector.injectMembers(this);

        assertEquals(true, Scopes.isSingleton(injector.getBinding(DOMDataBroker.class)));

        try (Registration registration = yangRegisterer
                .registerYANG(getClass().getResource("/META-INF/yang/test@2017-01-01.yang").toURI())) {
            DOMDataTreeReadWriteTransaction tx = domDataBroker.newReadWriteTransaction();
            YangInstanceIdentifier contYIID = YangInstanceIdentifier.of(Cont.QNAME);
            NodeIdentifier nodeIdentifier = NodeIdentifier.create(Cont.QNAME);
            tx.put(CONFIGURATION, contYIID,
                    ImmutableContainerNodeBuilder.create().withNodeIdentifier(nodeIdentifier).build());
            tx.commit().get();
        }
    }
}
