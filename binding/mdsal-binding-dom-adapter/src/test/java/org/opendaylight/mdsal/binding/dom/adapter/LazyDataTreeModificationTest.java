/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101.BooleanContainer;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

public class LazyDataTreeModificationTest {
    @Test
    public void basicTest() throws Exception {
        final BindingDOMCodecServices registry = mock(BindingDOMCodecServices.class);
        final AdapterContext codec = new ConstantAdapterContext(registry);
        final DOMDataTreeCandidate domDataTreeCandidate = mock(DOMDataTreeCandidate.class);
        final DOMDataTreeIdentifier domDataTreeIdentifier =
                DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.of());
        final var bindingPath = InstanceIdentifier.create(BooleanContainer.class);
        doReturn(bindingPath).when(registry).fromYangInstanceIdentifier(any());
        final BindingDataObjectCodecTreeNode<?> bindingCodecTreeNode = mock(BindingDataObjectCodecTreeNode.class);
        doReturn(bindingCodecTreeNode).when(registry).getSubtreeCodec(any(InstanceIdentifier.class));
        doReturn(domDataTreeIdentifier).when(domDataTreeCandidate).getRootPath();
        doReturn(mock(DataTreeCandidateNode.class)).when(domDataTreeCandidate).getRootNode();
        doReturn(bindingPath.getPathArguments().iterator().next()).when(bindingCodecTreeNode)
            .deserializePathArgument(null);

        assertNotNull(LazyDataTreeModification.from(codec.currentSerializer(), domDataTreeCandidate, null));
    }
}