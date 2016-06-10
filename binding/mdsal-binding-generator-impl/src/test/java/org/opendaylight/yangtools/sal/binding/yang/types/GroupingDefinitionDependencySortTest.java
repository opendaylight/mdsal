/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class GroupingDefinitionDependencySortTest {

    @Rule
    public ExpectedException expException = ExpectedException.none();

    @Test
    public void testSortMethod() {
        GroupingDefinitionDependencySort groupingDefinitionDependencySort = new GroupingDefinitionDependencySort();
        List<GroupingDefinition> unsortedGroupingDefs = new ArrayList<>();

        GroupingDefinition grp1 = mock(GroupingDefinition.class);
        doReturn(SchemaPath.create(false, QName.create("Cont1"), QName.create("Cont2"))).when(grp1).getPath();
        doReturn(QName.create("leaf1")).when(grp1).getQName();
        doReturn(Collections.EMPTY_SET).when(grp1).getUses();
        doReturn(Collections.EMPTY_SET).when(grp1).getGroupings();
        doReturn(Collections.EMPTY_SET).when(grp1).getChildNodes();

        GroupingDefinition grp2 = mock(GroupingDefinition.class);
        doReturn(SchemaPath.create(false, QName.create("Cont1"))).when(grp2).getPath();
        doReturn(QName.create("leaf2")).when(grp2).getQName();
        doReturn(Collections.EMPTY_SET).when(grp2).getUses();
        doReturn(Collections.EMPTY_SET).when(grp2).getGroupings();
        doReturn(Collections.EMPTY_SET).when(grp2).getChildNodes();

        GroupingDefinition grp3 = mock(GroupingDefinition.class);
        doReturn(SchemaPath.create(false, QName.create("Cont1"), QName.create("Cont2"))).when(grp3).getPath();
        doReturn(QName.create("leaf3")).when(grp3).getQName();
        doReturn(Collections.EMPTY_SET).when(grp3).getUses();
        doReturn(Collections.EMPTY_SET).when(grp3).getGroupings();
        doReturn(Collections.EMPTY_SET).when(grp3).getChildNodes();

        GroupingDefinition grp4 = mock(GroupingDefinition.class);
        doReturn(SchemaPath.create(false, QName.create("Cont1"), QName.create("Cont2"), QName.create("List1"))).when(grp4).getPath();
        doReturn(QName.create("leaf4")).when(grp4).getQName();
        doReturn(Collections.EMPTY_SET).when(grp4).getUses();
        doReturn(Collections.EMPTY_SET).when(grp4).getGroupings();
        doReturn(Collections.EMPTY_SET).when(grp4).getChildNodes();

        GroupingDefinition grp5 = mock(GroupingDefinition.class);
        doReturn(SchemaPath.create(false, QName.create("Cont1"))).when(grp5).getPath();
        doReturn(QName.create("leaf5")).when(grp5).getQName();
        doReturn(Collections.EMPTY_SET).when(grp5).getUses();
        doReturn(Collections.EMPTY_SET).when(grp5).getGroupings();
        doReturn(Collections.EMPTY_SET).when(grp5).getChildNodes();

        unsortedGroupingDefs.add(grp1);
        unsortedGroupingDefs.add(grp1);
        unsortedGroupingDefs.add(grp2);
        unsortedGroupingDefs.add(grp3);
        unsortedGroupingDefs.add(grp4);
        unsortedGroupingDefs.add(grp5);

        List<GroupingDefinition> sortedGroupingDefs = groupingDefinitionDependencySort.sort(unsortedGroupingDefs);
        assertNotNull(sortedGroupingDefs);

        expException.expect(IllegalArgumentException.class);
        expException.expectMessage("Set of Type Definitions cannot be NULL!");
        groupingDefinitionDependencySort.sort(null);
    }
}
