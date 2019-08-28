/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.mdsal.binding.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;

public class BindingDOMDataTreeCommitCohortAdapterTest {

    @Test
    public void canCommitTest() throws Exception {
        final DataTreeCommitCohort<?> cohort = mock(DataTreeCommitCohort.class);
        final BindingNormalizedNodeCodecRegistry registry = mock(BindingNormalizedNodeCodecRegistry.class);
        final BindingToNormalizedNodeCodec codec =
                new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(), registry);

        final BindingDOMDataTreeCommitCohortAdapter<?> adapter =
                new BindingDOMDataTreeCommitCohortAdapter<>(codec, cohort);
        assertNotNull(adapter);

        final DOMDataTreeCandidate domDataTreeCandidate = mock(DOMDataTreeCandidate.class);
        final DOMDataTreeIdentifier domDataTreeIdentifier =
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.empty());
        doReturn(InstanceIdentifier.create(DataObject.class)).when(registry).fromYangInstanceIdentifier(any());
        final BindingCodecTree bindingCodecTree = mock(BindingCodecTree.class);
        final BindingDataObjectCodecTreeNode<?> bindingCodecTreeNode = mock(BindingDataObjectCodecTreeNode.class);
        doReturn(bindingCodecTreeNode).when(bindingCodecTree).getSubtreeCodec(any(InstanceIdentifier.class));
        doReturn(bindingCodecTree).when(registry).getCodecContext();
        doReturn(domDataTreeIdentifier).when(domDataTreeCandidate).getRootPath();
        doReturn(mock(DataTreeCandidateNode.class)).when(domDataTreeCandidate).getRootNode();
        assertNotNull(LazyDataTreeModification.create(codec, domDataTreeCandidate));

        final Object txId = new Object();

        doReturn(PostCanCommitStep.NOOP_SUCCESSFUL_FUTURE).when(cohort).canCommit(any(), any());
        adapter.canCommit(txId, null, Arrays.asList(domDataTreeCandidate, domDataTreeCandidate));
        ArgumentCaptor<Collection> modifications = ArgumentCaptor.forClass(Collection.class);
        verify(cohort).canCommit(eq(txId), modifications.capture());
        assertEquals(2, modifications.getValue().size());
    }
}
