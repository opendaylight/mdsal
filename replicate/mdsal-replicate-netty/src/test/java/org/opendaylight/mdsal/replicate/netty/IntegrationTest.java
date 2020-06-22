/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.Uninterruptibles;
import java.net.Inet4Address;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.eos.dom.simple.SimpleDOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.dom.impl.DOMClusterSingletonServiceProviderImpl;
import org.opendaylight.yangtools.concepts.Registration;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IntegrationTest extends AbstractDataBrokerTest {
    private AbstractBootstrapSupport support;
    private DOMClusterSingletonServiceProviderImpl css;

    @Before
    public void before() {
        support = AbstractBootstrapSupport.create();
        css = new DOMClusterSingletonServiceProviderImpl(new SimpleDOMEntityOwnershipService());
        css.initializeProvider();
    }

    @After
    public void after() throws InterruptedException {
        support.close();
        css.close();
    }

    @Test
    public void testDatastoreReplication() {
        final DOMTransactionChain sinkChain = mock(DOMTransactionChain.class);
        final DOMDataBroker sinkBroker = mock(DOMDataBroker.class);
        doReturn(sinkChain).when(sinkBroker).createMergingTransactionChain(any());

        final Registration source = NettyReplication.createSource(support, getDomBroker(), css, true, 4000);
        final Registration sink = NettyReplication.createSink(support, sinkBroker, css, true,
            Inet4Address.getLoopbackAddress(), 4000, Duration.ZERO);

        Uninterruptibles.sleepUninterruptibly(15, TimeUnit.SECONDS);
        sink.close();
        source.close();
    }
}
