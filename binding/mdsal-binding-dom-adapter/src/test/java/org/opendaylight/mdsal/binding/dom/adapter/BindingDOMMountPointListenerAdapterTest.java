/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.MountPointService.MountPointListener;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class BindingDOMMountPointListenerAdapterTest {
    private BindingDOMMountPointListenerAdapter adapter;
    private AdapterContext codec;

    @Mock private MountPointListener listener;
    @Mock private DOMMountPointService mountPointService;
    @Mock private Registration listenerRegistration;

    @Before
    public void setUp() throws Exception {
        final var testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final var testContext = testFactory.getTestContext();
        testContext.start();
        codec = testContext.getCodec();
        doReturn(listenerRegistration).when(mountPointService).registerProvisionListener(any());
        adapter = new BindingDOMMountPointListenerAdapter(listener, codec, mountPointService);
    }

    @Test
    public void basicTest() throws Exception {
        assertSame(listener, adapter.listener);
        adapter.close();
        verify(listenerRegistration).close();
    }

    @Test
    public void onMountPointCreatedWithExceptionTest() throws Exception {
        reset(listener);
        adapter.onMountPointCreated(YangInstanceIdentifier.of());
        verifyNoInteractions(listener);
    }

    @Test
    public void onMountPointRemovedWithExceptionTest() throws Exception {
        reset(listener);
        adapter.onMountPointRemoved(YangInstanceIdentifier.of());
        verifyNoInteractions(listener);
    }
}