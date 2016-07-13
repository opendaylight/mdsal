/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelListChangedBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

public class LazySerializedDOMNotificationTest {

    @Test
    public void basicTest() throws Exception {
        BindingNormalizedNodeSerializer codec = mock(BindingNormalizedNodeSerializer.class);
        final DOMNotification lazySerializedDOMNotification =
                LazySerializedDOMNotification.create(codec, new TwoLevelListChangedBuilder().build());
        ContainerNode containerNode = mock(ContainerNode.class);
        doReturn(containerNode).when(codec).toNormalizedNodeNotification(any());
        assertEquals(containerNode, lazySerializedDOMNotification.getBody());
    }
}