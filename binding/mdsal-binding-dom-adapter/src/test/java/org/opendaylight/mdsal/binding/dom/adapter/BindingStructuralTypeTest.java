/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class BindingStructuralTypeTest {
    @Mock
    private NormalizedNode normalizedNode;
    @Mock
    private DataTreeCandidateNode dataTreeCandidateNode;

    @Test
    public void basicTest() {
        doReturn(normalizedNode).when(dataTreeCandidateNode).dataBefore();
        assertEquals(BindingStructuralType.UNKNOWN, BindingStructuralType.from(dataTreeCandidateNode));
    }
}