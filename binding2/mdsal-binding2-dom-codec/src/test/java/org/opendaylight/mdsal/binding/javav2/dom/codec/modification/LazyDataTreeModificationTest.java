/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.modification;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;

public class LazyDataTreeModificationTest {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void basicTest() throws Exception {
        final BindingNormalizedNodeCodecRegistry registry = mock(BindingNormalizedNodeCodecRegistry.class);
        final BindingToNormalizedNodeCodec codec =
                new BindingToNormalizedNodeCodec(
                        (GeneratedClassLoadingStrategy) GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
                        registry);
        final DOMDataTreeCandidate domDataTreeCandidate = mock(DOMDataTreeCandidate.class);
        final DOMDataTreeIdentifier domDataTreeIdentifier =
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY);
        doReturn(InstanceIdentifier.create(TreeNode.class)).when(registry).fromYangInstanceIdentifier(any());
        final BindingTreeCodec bindingCodecTree = mock(BindingTreeCodec.class);
        final BindingTreeNodeCodec bindingCodecTreeNode = mock(BindingTreeNodeCodec.class);
        doReturn(bindingCodecTreeNode).when(bindingCodecTree).getSubtreeCodec(any(InstanceIdentifier.class));
        final PathArgument identifier =
                new YangInstanceIdentifier.NodeIdentifier(QName.create("test", "2017-06-13", "test-l"));

        doReturn(mock(TreeArgument.class)).when(bindingCodecTreeNode).deserializePathArgument(identifier);
        doReturn(bindingCodecTree).when(registry).getCodecContext();
        doReturn(domDataTreeIdentifier).when(domDataTreeCandidate).getRootPath();
        final DataTreeCandidateNode dataTreeCandidate = mock(DataTreeCandidateNode.class);
        doReturn(dataTreeCandidate).when(domDataTreeCandidate).getRootNode();
        doReturn(identifier).when(dataTreeCandidate).getIdentifier();
        assertNotNull(LazyDataTreeModification.create(codec, domDataTreeCandidate));
    }
}
