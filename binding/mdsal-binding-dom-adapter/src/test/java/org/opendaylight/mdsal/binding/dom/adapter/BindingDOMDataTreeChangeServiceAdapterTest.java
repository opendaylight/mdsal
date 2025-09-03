/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker.DataTreeChangeExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Unit tests for BindingDOMDataTreeChangeServiceAdapter.
 *
 * @author Thomas Pantelis
 */
@ExtendWith(MockitoExtension.class)
class BindingDOMDataTreeChangeServiceAdapterTest {
    private static final DataObjectIdentifier<Top> TOP_PATH = DataObjectIdentifier.builder(Top.class).build();
    private static final YangInstanceIdentifier TOP_YIID = YangInstanceIdentifier.of(Top.QNAME);

    @Mock
    private DataTreeChangeExtension mockDOMService;

    @Mock
    private BindingDOMCodecServices services;

    @Mock
    private Registration mockDOMReg;

    @BeforeEach
    public void setUp() {
        doReturn(TOP_YIID).when(services).toYangInstanceIdentifier(TOP_PATH);
    }

    @Test
    void testRegisterDataTreeChangeListener() {
        final var codec = new ConstantAdapterContext(services);

        final var service = new BindingDOMDataTreeChangeServiceAdapter(codec, mockDOMService);

       doReturn(mockDOMReg).when(mockDOMService).registerTreeChangeListener(domDataTreeIdentifier(TOP_YIID), any());
        service.registerTreeChangeListener(LogicalDatastoreType.CONFIGURATION, TOP_PATH,
            new TestDataTreeChangeListener());
    }

    private static @NonNull DOMDataTreeIdentifier domDataTreeIdentifier(final YangInstanceIdentifier yangID) {
        return argThat(arg -> arg.datastore() == LogicalDatastoreType.CONFIGURATION && yangID.equals(arg.path()));
    }

    private static final class TestDataTreeChangeListener implements DataTreeChangeListener<Top> {
        @Override
        public void onDataTreeChanged(final List<DataTreeModification<Top>> changes) {
            // No-op
        }
    }
}
