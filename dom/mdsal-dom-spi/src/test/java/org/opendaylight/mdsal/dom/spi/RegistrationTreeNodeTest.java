/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class RegistrationTreeNodeTest {

    @Test
    public void basicTest() throws Exception {
        final PathArgument pathArgument = mock(PathArgument.class);
        final RegistrationTreeNode<Object> registrationTreeNodeParent = new RegistrationTreeNode<>(null, pathArgument);
        final RegistrationTreeNode<Object> registrationTreeNode =
                new RegistrationTreeNode<>(registrationTreeNodeParent, pathArgument);

        assertEquals(pathArgument, registrationTreeNode.getIdentifier());

        final Object registration = new Object();
        assertFalse(registrationTreeNode.getRegistrations().contains(registration));
        registrationTreeNode.addRegistration(registration);
        assertTrue(registrationTreeNode.getRegistrations().contains(registration));
        registrationTreeNode.removeRegistration(registration);
        assertFalse(registrationTreeNode.getRegistrations().contains(registration));
        registrationTreeNode.removeRegistration(registration);

        assertNotNull(registrationTreeNode.ensureChild(pathArgument));
        assertNotNull(registrationTreeNode.getExactChild(pathArgument));

        final NodeWithValue nodeWithValue = new NodeWithValue<>(QName.create("testNode"), new Object());
        assertEquals(Collections.EMPTY_LIST, registrationTreeNode.getInexactChildren(nodeWithValue));
        assertEquals(Collections.EMPTY_LIST, registrationTreeNode.getInexactChildren(pathArgument));

        final NodeIdentifier nodeWithoutValue = new NodeIdentifier(QName.create("testNode"));
        assertNotNull(registrationTreeNode.ensureChild(nodeWithoutValue));
        assertFalse(registrationTreeNode.getInexactChildren(nodeWithValue).isEmpty());

        doReturn("TestPathArgument").when(pathArgument).toString();
        assertNotNull(registrationTreeNode.toString());
        assertTrue(registrationTreeNode.toString().contains(pathArgument.toString()));
    }
}