/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedListBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class LazyBindingListTest extends AbstractBindingCodecTest {

    private TopLevelList expected;
    private KeyedInstanceIdentifier<TopLevelList, TopLevelListKey> expectedPath;
    private YangInstanceIdentifier path;
    private MapEntryNode data;

    @Before
    public void createData() {
        final List<NestedList> nested = new ArrayList<>();
        for (int i = 0; i < 2 * LazyBindingList.LAZY_CUTOFF; ++i) {
            nested.add(new NestedListBuilder().setName(String.valueOf(i)).build());
        }
        expected = new TopLevelListBuilder()
                .setName("test")
                .setNestedList(nested)
                .build();
        expectedPath = InstanceIdentifier.create(Top.class).child(TopLevelList.class, expected.key());

        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = codecContext.toNormalizedNode(
            expectedPath, expected);
        path = entry.getKey();
        data = (MapEntryNode) entry.getValue();
    }

    @Test
    public void testList() {
        Entry<InstanceIdentifier<?>, DataObject> entry = codecContext.fromNormalizedNode(path, data);
        assertEquals(expectedPath, entry.getKey());
        final TopLevelList value = (TopLevelList) entry.getValue();

        final List<NestedList> list = value.getNestedList();
        assertThat(list,  instanceOf(LazyBindingList.class));

        // Equality does all the right things to check happy paths
        assertEquals(expected.getNestedList(), list);
        assertEquals(expected.getNestedList().hashCode(), list.hashCode());

        // Test throws, just for completeness' sake
        assertThrows(UnsupportedOperationException.class, () -> list.add(null));
        assertThrows(UnsupportedOperationException.class, () -> list.addAll(null));
        assertThrows(UnsupportedOperationException.class, () -> list.addAll(0, null));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(null));
        assertThrows(UnsupportedOperationException.class, () -> list.removeAll(null));
        assertThrows(UnsupportedOperationException.class, () -> list.replaceAll(null));
        assertThrows(UnsupportedOperationException.class, () -> list.retainAll(null));
        assertThrows(UnsupportedOperationException.class, () -> list.sort(null));
        assertThrows(UnsupportedOperationException.class, () -> list.clear());
    }
}
