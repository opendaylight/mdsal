/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Unit tests for BindingDOMDataTreeChangeServiceAdapter.
 *
 * @author Thomas Pantelis
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class BindingDOMDataTreeChangeServiceAdapterTest {
    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final YangInstanceIdentifier TOP_YIID = YangInstanceIdentifier.of(Top.QNAME);

    @Mock
    private DOMDataTreeChangeService mockDOMService;

    @Mock
    private BindingDOMCodecServices services;

    @SuppressWarnings("rawtypes")
    @Mock
    private ListenerRegistration mockDOMReg;

    @Before
    public void setUp() {
        doReturn(TOP_YIID).when(services).toYangInstanceIdentifier(TOP_PATH);
    }

    @Test
    public void testRegisterDataTreeChangeListener() {
        final AdapterContext codec = new ConstantAdapterContext(services);

        final DataTreeChangeService service = new BindingDOMDataTreeChangeServiceAdapter(codec, mockDOMService);

        doReturn(mockDOMReg).when(mockDOMService).registerDataTreeChangeListener(
                domDataTreeIdentifier(TOP_YIID),
                any(DOMDataTreeChangeListener.class));
        final DataTreeIdentifier<Top> treeId = DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION, TOP_PATH);
        final TestClusteredDataTreeChangeListener mockClusteredListener = new TestClusteredDataTreeChangeListener();
        service.registerDataTreeChangeListener(treeId , mockClusteredListener);

        verify(mockDOMService).registerDataTreeChangeListener(domDataTreeIdentifier(TOP_YIID),
                isA(ClusteredDOMDataTreeChangeListener.class));

        reset(mockDOMService);
        doReturn(mockDOMReg).when(mockDOMService).registerDataTreeChangeListener(
                domDataTreeIdentifier(TOP_YIID), any(DOMDataTreeChangeListener.class));
        final TestDataTreeChangeListener mockNonClusteredListener = new TestDataTreeChangeListener();
        service.registerDataTreeChangeListener(treeId , mockNonClusteredListener);

        verify(mockDOMService).registerDataTreeChangeListener(domDataTreeIdentifier(TOP_YIID),
                not(isA(ClusteredDOMDataTreeChangeListener.class)));
    }

    static DOMDataTreeIdentifier domDataTreeIdentifier(final YangInstanceIdentifier yangID) {
        return argThat(arg -> arg.getDatastoreType() == LogicalDatastoreType.CONFIGURATION
                && yangID.equals(arg.getRootIdentifier()));
    }

    private static class TestClusteredDataTreeChangeListener implements ClusteredDataTreeChangeListener<Top> {
        @Override
        public void onDataTreeChanged(final List<DataTreeModification<Top>> changes) {
            // No-op
        }

        @Override
        public void onInitialData() {
            // No-op
        }
    }

    private static class TestDataTreeChangeListener implements DataTreeChangeListener<Top> {
        @Override
        public void onDataTreeChanged(final List<DataTreeModification<Top>> changes) {
            // No-op
        }

        @Override
        public void onInitialData() {
            // No-op
        }
    }
}
