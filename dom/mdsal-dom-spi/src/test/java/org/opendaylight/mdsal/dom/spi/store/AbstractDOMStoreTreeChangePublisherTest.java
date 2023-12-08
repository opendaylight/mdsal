/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

class AbstractDOMStoreTreeChangePublisherTest {
    private static final class TestPublisher extends AbstractDOMStoreTreeChangePublisher {
        boolean removeInvoked;
        boolean notifyInvoked;

        @Override
        protected void notifyListener(final Reg registration, final List<DataTreeCandidate> changes) {
            notifyInvoked = true;
        }

        @Override
        protected void registrationRemoved(final Reg registration) {
            removeInvoked = true;
        }
    }

    private final TestPublisher publisher = new TestPublisher();

    @Test
    void basicTest() {
        final var dataTreeCandidate = mock(DataTreeCandidate.class);
        final var dataTreeCandidateNode = mock(DataTreeCandidateNode.class, "dataTreeCandidateNode");
        final var yangInstanceIdentifier = YangInstanceIdentifier.of(
                QName.create("", "node1"), QName.create("", "node2"));

        doReturn(dataTreeCandidateNode).when(dataTreeCandidate).getRootNode();
        doReturn(ModificationType.WRITE).when(dataTreeCandidateNode).modificationType();
        doReturn(yangInstanceIdentifier).when(dataTreeCandidate).getRootPath();
        doReturn(ImmutableList.of(dataTreeCandidateNode)).when(dataTreeCandidateNode).childNodes();
        doReturn(yangInstanceIdentifier.getLastPathArgument()).when(dataTreeCandidateNode).name();

        final var listener = mock(DOMDataTreeChangeListener.class);
        try (var reg = publisher.registerTreeChangeListener(yangInstanceIdentifier, listener)) {
            assertFalse(publisher.removeInvoked);
            assertFalse(publisher.notifyInvoked);

            publisher.processCandidateTree(dataTreeCandidate);
            doReturn(ModificationType.UNMODIFIED).when(dataTreeCandidateNode).modificationType();
            publisher.processCandidateTree(dataTreeCandidate);
        }

        assertTrue(publisher.removeInvoked);
        assertTrue(publisher.notifyInvoked);
    }
}
