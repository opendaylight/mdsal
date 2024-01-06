/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

@ExtendWith(MockitoExtension.class)
class BindingStructuralTypeTest {
    @Mock
    private AnydataNode<?> normalizedNode;
    @Mock
    private DataTreeCandidateNode dataTreeCandidateNode;

    @Test
    void basicTest() {
        doReturn(normalizedNode).when(dataTreeCandidateNode).dataBefore();
        assertEquals(BindingStructuralType.NOT_ADDRESSABLE, BindingStructuralType.from(dataTreeCandidateNode));
    }
}