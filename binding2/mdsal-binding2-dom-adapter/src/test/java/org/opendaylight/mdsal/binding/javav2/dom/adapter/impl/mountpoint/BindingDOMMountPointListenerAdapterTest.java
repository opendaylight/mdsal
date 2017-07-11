/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.mountpoint;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.binding.javav2.api.MountPointListener;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.test.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.test.BindingTestContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class BindingDOMMountPointListenerAdapterTest {

    private BindingDOMMountPointListenerAdapter bindingDOMMountPointListenerAdapter;
    private BindingToNormalizedNodeCodec codec;

    @Mock private MountPointListener listener;
    @Mock private DOMMountPointService mountPointService;
    @Mock private ListenerRegistration listenerRegistration;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();
        codec = testContext.getCodec();
        doReturn(listenerRegistration).when(mountPointService).registerProvisionListener(any());
        bindingDOMMountPointListenerAdapter =
                new BindingDOMMountPointListenerAdapter<>(listener, codec, mountPointService);
    }

    @Test
    public void basicTest() throws Exception {
        assertEquals(listener, bindingDOMMountPointListenerAdapter.getInstance());
        bindingDOMMountPointListenerAdapter.close();
        verify(listenerRegistration).close();
    }

    @Test
    public void onMountPointCreatedWithExceptionTest() throws Exception {
        reset(listener);
        bindingDOMMountPointListenerAdapter.onMountPointCreated(YangInstanceIdentifier.EMPTY);
        verifyZeroInteractions(listener);
    }

    @Test
    public void onMountPointRemovedWithExceptionTest() throws Exception {
        reset(listener);
        bindingDOMMountPointListenerAdapter.onMountPointRemoved(YangInstanceIdentifier.EMPTY);
        verifyZeroInteractions(listener);
    }
}