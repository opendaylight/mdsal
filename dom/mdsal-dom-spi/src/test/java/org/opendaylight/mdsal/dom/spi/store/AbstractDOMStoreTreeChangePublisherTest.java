/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

public class AbstractDOMStoreTreeChangePublisherTest extends AbstractDOMStoreTreeChangePublisher {
    private static boolean removeInvoked = false;
    private static boolean notifyInvoked = false;

    @Test
    public void basicTest() throws Exception {
        final DataTreeCandidate dataTreeCandidate = mock(DataTreeCandidate.class);
        final DataTreeCandidateNode dataTreeCandidateNode = mock(DataTreeCandidateNode.class, "dataTreeCandidateNode");
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.builder()
                .node(QName.create("", "node1")).node(QName.create("", "node2")).build();

        doReturn(dataTreeCandidateNode).when(dataTreeCandidate).getRootNode();
        doReturn(ModificationType.WRITE).when(dataTreeCandidateNode).modificationType();
        doReturn(yangInstanceIdentifier).when(dataTreeCandidate).getRootPath();
        doReturn(ImmutableList.of(dataTreeCandidateNode)).when(dataTreeCandidateNode).childNodes();
        doReturn(yangInstanceIdentifier.getLastPathArgument()).when(dataTreeCandidateNode).name();

        final DOMDataTreeChangeListener domDataTreeChangeListener = mock(DOMDataTreeChangeListener.class);

        final var abstractDOMDataTreeChangeListenerRegistration =
                registerTreeChangeListener(yangInstanceIdentifier, domDataTreeChangeListener);

        assertFalse(removeInvoked);
        assertFalse(notifyInvoked);

        processCandidateTree(dataTreeCandidate);
        doReturn(ModificationType.UNMODIFIED).when(dataTreeCandidateNode).modificationType();
        processCandidateTree(dataTreeCandidate);

        abstractDOMDataTreeChangeListenerRegistration.close();

        assertTrue(removeInvoked);
        assertTrue(notifyInvoked);

        assertTrue(abstractDOMDataTreeChangeListenerRegistration.isClosed());
    }

    @Override
    protected void notifyListener(final AbstractDOMDataTreeChangeListenerRegistration<?> registration,
            final List<DataTreeCandidate> changes) {
        notifyInvoked = true;
    }

    @Override
    protected void registrationRemoved(final AbstractDOMDataTreeChangeListenerRegistration<?> registration) {
        removeInvoked = true;
    }
}
