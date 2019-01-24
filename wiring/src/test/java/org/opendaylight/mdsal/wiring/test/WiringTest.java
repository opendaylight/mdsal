/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.test;

import static org.junit.Assert.assertEquals;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import javax.inject.Inject;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.wiring.guice.InMemoryModule;
import org.opendaylight.mdsal.wiring.guice.MdsalModule;

/**
 * Tests all the "Wiring classes in mdsal.
 *
 * @author Michael Vorburger.ch
 */
public class WiringTest {

    // Real world ODL SDN applications typically use the JUnit @Rule GuiceRule from infrautils.inject.guice.testutils,
    // but for this test who's only purpose it is to test the modules, this "manual" simple Guice use is just fine.

    @Inject DataBroker dataBroker;

    @Test
    public void testWiring() {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new InMemoryModule(), new MdsalModule());
        injector.injectMembers(this);
        assertEquals(true, Scopes.isSingleton(injector.getBinding(DataBroker.class)));
    }
}
