/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class GroupingDefinitionDependencySortTest {
    @Test
    public void testSortMethod() {

        final List<GroupingDefinition> unsortedGroupingDefs = new ArrayList<>();

        GroupingDefinition grp1 = mock(GroupingDefinition.class);
        doReturn(SchemaPath.create(false, QName.create("", "Cont1"), QName.create("", "Cont2"))).when(grp1).getPath();
        doReturn(QName.create("", "leaf1")).when(grp1).getQName();
        doReturn(Collections.emptySet()).when(grp1).getUses();
        doReturn(Collections.emptySet()).when(grp1).getGroupings();
        doReturn(Collections.emptySet()).when(grp1).getChildNodes();
        doReturn(Collections.emptySet()).when(grp1).getActions();
        doReturn(Collections.emptySet()).when(grp1).getNotifications();

        GroupingDefinition grp2 = mock(GroupingDefinition.class);
        doReturn(SchemaPath.create(false, QName.create("", "Cont1"))).when(grp2).getPath();
        doReturn(QName.create("", "leaf2")).when(grp2).getQName();
        doReturn(Collections.emptySet()).when(grp2).getUses();
        doReturn(Collections.emptySet()).when(grp2).getGroupings();
        doReturn(Collections.emptySet()).when(grp2).getChildNodes();
        doReturn(Collections.emptySet()).when(grp2).getActions();
        doReturn(Collections.emptySet()).when(grp2).getNotifications();

        GroupingDefinition grp3 = mock(GroupingDefinition.class);
        doReturn(SchemaPath.create(false, QName.create("", "Cont1"), QName.create("", "Cont2"))).when(grp3).getPath();
        doReturn(QName.create("", "leaf3")).when(grp3).getQName();
        doReturn(Collections.emptySet()).when(grp3).getUses();
        doReturn(Collections.emptySet()).when(grp3).getGroupings();
        doReturn(Collections.emptySet()).when(grp3).getChildNodes();
        doReturn(Collections.emptySet()).when(grp3).getActions();
        doReturn(Collections.emptySet()).when(grp3).getNotifications();

        GroupingDefinition grp4 = mock(GroupingDefinition.class);
        doReturn(SchemaPath.create(false, QName.create("", "Cont1"), QName.create("", "Cont2"),
            QName.create("", "List1"))).when(grp4).getPath();
        doReturn(QName.create("", "leaf4")).when(grp4).getQName();
        doReturn(Collections.emptySet()).when(grp4).getUses();
        doReturn(Collections.emptySet()).when(grp4).getGroupings();
        doReturn(Collections.emptySet()).when(grp4).getChildNodes();
        doReturn(Collections.emptySet()).when(grp4).getActions();
        doReturn(Collections.emptySet()).when(grp4).getNotifications();

        GroupingDefinition grp5 = mock(GroupingDefinition.class);
        doReturn(SchemaPath.create(false, QName.create("", "Cont1"))).when(grp5).getPath();
        doReturn(QName.create("", "leaf5")).when(grp5).getQName();
        doReturn(Collections.emptySet()).when(grp5).getUses();
        doReturn(Collections.emptySet()).when(grp5).getGroupings();
        doReturn(Collections.emptySet()).when(grp5).getChildNodes();
        doReturn(Collections.emptySet()).when(grp5).getActions();
        doReturn(Collections.emptySet()).when(grp5).getNotifications();

        unsortedGroupingDefs.add(grp1);
        unsortedGroupingDefs.add(grp1);
        unsortedGroupingDefs.add(grp2);
        unsortedGroupingDefs.add(grp3);
        unsortedGroupingDefs.add(grp4);
        unsortedGroupingDefs.add(grp5);

        List<GroupingDefinition> sortedGroupingDefs = GroupingDefinitionDependencySort.sort(unsortedGroupingDefs);
        assertNotNull(sortedGroupingDefs);
    }

    @Test
    public void testNullSort() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> GroupingDefinitionDependencySort.sort(null));
        assertEquals("Set of Type Definitions cannot be NULL!", ex.getMessage());
    }

    @Test
    public void groupingSortIncludesActions() {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResource("/mdsal448.yang");
        final Collection<? extends GroupingDefinition> groupings = context.findModule("mdsal448").get().getGroupings();
        assertEquals(2, groupings.size());

        final List<GroupingDefinition> ordered = sortGroupings(Iterables.get(groupings, 0),
            Iterables.get(groupings, 1));
        assertEquals(2, ordered.size());
        // "the-grouping" needs to be first
        assertEquals("the-grouping", ordered.get(0).getQName().getLocalName());
        assertEquals("action-grouping", ordered.get(1).getQName().getLocalName());

        // Sort needs to be stable
        final List<GroupingDefinition> reverse = sortGroupings(Iterables.get(groupings, 1),
            Iterables.get(groupings, 0));
        assertEquals(ordered, reverse);

        final List<GeneratedType> types = new DefaultBindingGenerator().generateTypes(context, context.getModules());
        assertNotNull(types);
        assertEquals(9, types.size());
    }

    private static List<GroupingDefinition> sortGroupings(final GroupingDefinition... groupings) {
        return GroupingDefinitionDependencySort.sort(Arrays.asList(groupings));
    }
}
