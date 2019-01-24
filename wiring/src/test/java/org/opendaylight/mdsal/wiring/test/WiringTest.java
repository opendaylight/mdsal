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
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.wiring.guice.InMemoryDOMDataBrokerModule;
import org.opendaylight.mdsal.wiring.guice.MdsalModule;

/**
 * Tests all the "Wiring classes in mdsal.
 *
 * @author Michael Vorburger.ch
 */
public class WiringTest {

    @Inject DataBroker dataBroker;
    @Inject DOMDataBroker domDataBroker;
    @Inject BindingNormalizedNodeSerializer codec; // NOT BindingToNormalizedNodeCodec, that's impl, this is API

    @Test
    public void testWiring() throws InterruptedException, ExecutionException {
        // Real world ODL SDN application tests use the JUnit @Rule GuiceRule from infrautils.inject.guice.testutils,
        // but for this test who's only purpose it is to test the modules, this "manual" simple Guice use is just fine.
        Injector injector = Guice.createInjector(PRODUCTION, new InMemoryDOMDataBrokerModule(), new MdsalModule());
        injector.injectMembers(this);

        assertEquals(true, Scopes.isSingleton(injector.getBinding(DataBroker.class)));
        // TODO dataBroker.newReadWriteTransaction().commit().get();
    }
}
