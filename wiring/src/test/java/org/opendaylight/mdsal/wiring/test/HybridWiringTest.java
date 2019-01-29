/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.test;

import static com.google.inject.Stage.PRODUCTION;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javax.inject.Inject;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.wiring.guice.BindingModule;
import org.opendaylight.mdsal.wiring.guice.InMemoryDOMDataBrokerModule;
import org.opendaylight.mdsal.wiring.schema.HybridSchemaWiring;
import org.opendaylight.mdsal.wiring.schema.YangRegisterer;

/**
 * Tests the *Wiring and *Module classes in mdsal for "hybrid" applications
 * which requires both binding (generated code and {@link DataBroker}) and dynamic YANG via DOM
 * ({@link YangRegisterer} and {@link DOMDataBroker}) together.
 *
 * <p>See {@link BindingWiringTest} and {@link DOMWiringTest} for alternative wirings.
 *
 * @author Michael Vorburger.ch
 */
public class HybridWiringTest {

    @Inject YangRegisterer yangRegisterer;
    @Inject DOMDataBroker domDataBroker;
    @Inject DataBroker dataBroker;

    @Test
    public void testWiring() throws Exception {
        Injector injector = Guice.createInjector(PRODUCTION, new InMemoryDOMDataBrokerModule(),
                new BindingModule(new HybridSchemaWiring()));
        injector.injectMembers(this);

        BindingWiringTest.testDataBroker(dataBroker);
        DOMWiringTest.testDOMDataBroker(yangRegisterer, domDataBroker);
    }
}
