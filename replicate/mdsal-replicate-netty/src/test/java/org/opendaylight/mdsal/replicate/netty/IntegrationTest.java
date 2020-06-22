/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.eos.dom.simple.SimpleDOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.dom.impl.DOMClusterSingletonServiceProviderImpl;
import org.opendaylight.yangtools.concepts.Registration;

public class IntegrationTest extends AbstractDataBrokerTest {
    private AbstractBootstrapSupport support;
    private SimpleDOMEntityOwnershipService eos;
    private DOMClusterSingletonServiceProviderImpl css;

    @Before
    public void before() {
        support = AbstractBootstrapSupport.create();
        eos = new SimpleDOMEntityOwnershipService();
        css = new DOMClusterSingletonServiceProviderImpl(eos);
    }

    @After
    public void after() throws InterruptedException {
        support.close();
        css.close();
    }

    @Test
    public void testDatastoreReplication() {
        final Registration source = NettyReplication.createSource(support, getDomBroker(), css, true, 4000);



    }
}
