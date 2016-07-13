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
import static org.mockito.Mockito.mock;

import com.google.common.base.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;

public class BindingStructuralTypeTest {

    @Test
    public void basicTest() throws Exception {
        final NormalizedNode normalizedNode = mock(NormalizedNode.class);
        final Optional optional = Optional.of(normalizedNode);
        final DataTreeCandidateNode dataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(optional).when(dataTreeCandidateNode).getDataAfter();
        doReturn(optional).when(dataTreeCandidateNode).getDataBefore();
        assertEquals(BindingStructuralType.UNKNOWN, BindingStructuralType.from(dataTreeCandidateNode));
    }
}