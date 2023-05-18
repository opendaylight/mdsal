/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;

public class RegistrationTreeNodeTest {
    @Test
    public void basicTest() throws Exception {
        final NodeIdentifier pathArgument = new NodeIdentifier(QName.create("", "pathArgument"));
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

        final NodeWithValue<?> nodeWithValue = new NodeWithValue<>(QName.create("", "testNode"), new Object());
        assertEquals(List.of(), registrationTreeNode.getInexactChildren(nodeWithValue));
        assertEquals(List.of(), registrationTreeNode.getInexactChildren(pathArgument));

        final NodeIdentifier nodeWithoutValue = new NodeIdentifier(QName.create("", "testNode"));
        assertNotNull(registrationTreeNode.ensureChild(nodeWithoutValue));
        assertFalse(registrationTreeNode.getInexactChildren(nodeWithValue).isEmpty());

        assertNotNull(registrationTreeNode.toString());
        assertTrue(registrationTreeNode.toString().contains(pathArgument.toString()));
    }
}