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
import java.lang.reflect.Field;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;

public class AbstractDOMStoreTreeChangePublisherTest extends AbstractDOMStoreTreeChangePublisher {

    private static boolean removeInvoked = false;
    private static boolean notifyInvoked = false;

    @Test
    public void basicTest() throws Exception {
        final DataTreeCandidate dataTreeCandidate = mock(DataTreeCandidate.class);
        final DataTreeCandidateNode dataTreeCandidateNode = mock(DataTreeCandidateNode.class, "dataTreeCandidateNode");
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.builder()
                .node(QName.create("node1")).node(QName.create("node2")).build();

        doReturn(dataTreeCandidateNode).when(dataTreeCandidate).getRootNode();
        doReturn(ModificationType.WRITE).when(dataTreeCandidateNode).getModificationType();
        doReturn(yangInstanceIdentifier).when(dataTreeCandidate).getRootPath();
        doReturn(ImmutableList.of(dataTreeCandidateNode)).when(dataTreeCandidateNode).getChildNodes();
        doReturn(yangInstanceIdentifier.getLastPathArgument()).when(dataTreeCandidateNode).getIdentifier();

        final DOMDataTreeChangeListener domDataTreeChangeListener = mock(DOMDataTreeChangeListener.class);

        final AbstractDOMDataTreeChangeListenerRegistration abstractDOMDataTreeChangeListenerRegistration =
                this.registerTreeChangeListener(yangInstanceIdentifier, domDataTreeChangeListener);

        assertFalse(removeInvoked);
        assertFalse(notifyInvoked);

        this.processCandidateTree(dataTreeCandidate);
        doReturn(ModificationType.UNMODIFIED).when(dataTreeCandidateNode).getModificationType();
        this.processCandidateTree(dataTreeCandidate);

        abstractDOMDataTreeChangeListenerRegistration.close();

        assertTrue(removeInvoked);
        assertTrue(notifyInvoked);

        final Field closedField = AbstractRegistration.class.getDeclaredField("closed");
        closedField.setAccessible(true);

        final int closed = (int) closedField.get(abstractDOMDataTreeChangeListenerRegistration);
        Assert.assertEquals(1, closed);
    }

    @Override
    protected void notifyListeners(@Nonnull Collection<AbstractDOMDataTreeChangeListenerRegistration<?>> registrations,
                                   @Nonnull YangInstanceIdentifier path, @Nonnull DataTreeCandidateNode node) {
        notifyInvoked = true;
    }

    @Override
    protected void registrationRemoved(@Nonnull AbstractDOMDataTreeChangeListenerRegistration<?> registration) {
        removeInvoked = true;
    }
}