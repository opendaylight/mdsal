/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.binding.api.DataObjectWritten;
import org.opendaylight.mdsal.binding.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101.BooleanContainer;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

@ExtendWith(MockitoExtension.class)
class BindingDOMDataTreeCommitCohortAdapterTest {
    @Mock
    private DataTreeCommitCohort<?> cohort;
    @Mock
    private BindingDOMCodecServices registry;
    @Mock
    private DOMDataTreeCandidate domDataTreeCandidate;
    @Mock
    private BindingDataObjectCodecTreeNode<?> bindingCodecTreeNode;
    @Mock
    private DataTreeCandidateNode rootNode;

    @Test
    void canCommitTest() {
        final var adapterContext = new ConstantAdapterContext(registry);
        final var adapter = new BindingDOMDataTreeCommitCohortAdapter<>(adapterContext, cohort, null);

        final var domDataTreeIdentifier =
                DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.of());
        final var bindingPath = InstanceIdentifier.create(BooleanContainer.class);
        doReturn(bindingPath).when(registry).fromYangInstanceIdentifier(any());
        doReturn(bindingCodecTreeNode).when(registry).getSubtreeCodec(any(InstanceIdentifier.class));
        doReturn(domDataTreeIdentifier).when(domDataTreeCandidate).getRootPath();
        doReturn(ModificationType.WRITE).when(rootNode).modificationType();
        doReturn(rootNode).when(domDataTreeCandidate).getRootNode();
        doReturn(bindingPath.getPathArguments().iterator().next()).when(bindingCodecTreeNode)
            .deserializePathArgument(null);

        final var mod = LazyDataTreeModification.from(adapterContext.currentSerializer(), domDataTreeCandidate, null);
        assertNotNull(mod);
        assertInstanceOf(DataObjectWritten.class, mod.getRootNode());

        final var txId = new Object();

        doReturn(PostCanCommitStep.NOOP_SUCCESSFUL_FUTURE).when(cohort).canCommit(any(), any());
        adapter.canCommit(txId, null, List.of(domDataTreeCandidate, domDataTreeCandidate));
        final var modifications = ArgumentCaptor.forClass(Collection.class);
        verify(cohort).canCommit(eq(txId), modifications.capture());
        assertEquals(2, modifications.getValue().size());
    }
}
