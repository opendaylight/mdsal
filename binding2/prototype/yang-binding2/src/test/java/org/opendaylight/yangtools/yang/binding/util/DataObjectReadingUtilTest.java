/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertTrue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.test.mock.Nodes;

public class DataObjectReadingUtilTest {
    @Mock private InstanceIdentifier<? extends TreeNode> pathNull;
    @Mock private Map.Entry<InstanceIdentifier<? extends TreeNode>, TreeNode> entryNull;
    @Mock private TreeNode mockedTreeNode;
    private InstanceIdentifier<? extends TreeNode> path;
    private Map.Entry<InstanceIdentifier<? extends TreeNode>, TreeNode> entry;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        path = InstanceIdentifier.builder(Nodes.class).build();
        ImmutableMap<InstanceIdentifier<? extends TreeNode>, TreeNode> map =
                ImmutableMap.<InstanceIdentifier<? extends TreeNode>, TreeNode>builder()
                .put(path, mockedTreeNode).build();

        ImmutableSet<Entry<InstanceIdentifier<? extends TreeNode>, TreeNode>> entries = map.entrySet();
        UnmodifiableIterator<Entry<InstanceIdentifier<? extends TreeNode>, TreeNode>> it = entries.iterator();
        while(it.hasNext()) {
            entry = it.next();
        }
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testReadDataParentNull() {
        DataObjectReadingUtil.readData(entryNull.getValue(), (InstanceIdentifier<TreeNode>) entryNull.getKey(), pathNull);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testReadDataParentPathNull() {
        DataObjectReadingUtil.readData(entry.getValue(), (InstanceIdentifier<TreeNode>) entryNull.getKey(), pathNull);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReadDataWithThreeParams() {
        assertTrue("Check if contains key",
                DataObjectReadingUtil.readData(entry.getValue(),
                        (InstanceIdentifier<TreeNode>) entry.getKey(), path).containsKey(entry.getKey()));

        assertTrue("Check if contains value",
                DataObjectReadingUtil.readData(entry.getValue(),
                        (InstanceIdentifier<TreeNode>) entry.getKey(), path).containsValue(entry.getValue()));
    }

    @Test(expected = NullPointerException.class)
    public void testReadDataWithTwoParams() {
        DataObjectReadingUtil.readData(mockedTreeNode, TreeNode.class);
    }
}