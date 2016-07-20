/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderModel.createTestContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class YangSchemaUtilsTest {

    private static final AugmentationSchema AUGMENTATION_SCHEMA = mock(AugmentationSchema.class);
    private static final UnknownSchemaNode UNKNOWN_SCHEMA_NODE = mock(UnknownSchemaNode.class);
    private static final QName Q_NAME =
            QName.create(URI.create("testUri"), new Date(System.currentTimeMillis()), "foo_augment");

    @Before
    public void setUp() throws Exception {
        doReturn(ImmutableList.of(UNKNOWN_SCHEMA_NODE)).when(AUGMENTATION_SCHEMA).getUnknownSchemaNodes();
        doReturn(QName.create(YangSchemaUtils.AUGMENT_IDENTIFIER)).when(UNKNOWN_SCHEMA_NODE).getNodeType();
        doReturn(Q_NAME).when(UNKNOWN_SCHEMA_NODE).getQName();
    }

    @Test
    public void getAugmentationQName() throws Exception {
        assertEquals(Q_NAME, YangSchemaUtils.getAugmentationQName(AUGMENTATION_SCHEMA));
        final DataSchemaNode dataSchemaNode = mock(DataSchemaNode.class);
        doReturn(Q_NAME).when(UNKNOWN_SCHEMA_NODE).getNodeType();
        doReturn(ImmutableList.of(dataSchemaNode)).when(AUGMENTATION_SCHEMA).getChildNodes();
        doReturn(false).when(dataSchemaNode).isAugmenting();
        doReturn(Q_NAME).when(dataSchemaNode).getQName();
        doReturn(ImmutableList.of(dataSchemaNode)).when(AUGMENTATION_SCHEMA).getChildNodes();
        assertEquals(Q_NAME, YangSchemaUtils.getAugmentationQName(AUGMENTATION_SCHEMA));
    }

    @Test
    public void getAugmentationIdentifier() throws Exception {
        assertEquals(Q_NAME, YangSchemaUtils.getAugmentationIdentifier(AUGMENTATION_SCHEMA));
        doReturn(Q_NAME).when(UNKNOWN_SCHEMA_NODE).getNodeType();
        assertNull(YangSchemaUtils.getAugmentationIdentifier(AUGMENTATION_SCHEMA));
    }

    @Test
    public void findTypeDefinition() throws Exception {
        SchemaContext context = createTestContext();
        assertNotNull(context);
        final QName qName = QName.create(context.getModules().iterator().next().getNamespace(),
                context.getModules().iterator().next().getRevision(), context.getModules().iterator().next().getName());
        assertNull(YangSchemaUtils.findTypeDefinition(context, SchemaPath.create(ImmutableList.of(qName), false)));
        final List qNames = new ArrayList();
        context.getTypeDefinitions().forEach(typeDefinition -> qNames.add(typeDefinition.getQName()));
        assertNull(YangSchemaUtils.findTypeDefinition(context, SchemaPath.create(qNames, false)));

        context = mock(SchemaContext.class);
        final Module container = mock(Module.class);
        doReturn(null).when(context).findModuleByNamespaceAndRevision(any(), any());
        assertNull(YangSchemaUtils.findTypeDefinition(context, SchemaPath.create(qNames, false)));
        doReturn(container).when(context).findModuleByNamespaceAndRevision(any(), any());

        final DataSchemaNode node = mock(DataSchemaNode.class);
        doReturn(node).when(container).getDataChildByName((QName) any());
        final TypeDefinition typeDefinition = mock(TypeDefinition.class);
        doReturn(Q_NAME).when(typeDefinition).getQName();
        doReturn(ImmutableSet.of(typeDefinition)).when(container).getTypeDefinitions();
        assertEquals(typeDefinition,
                YangSchemaUtils.findTypeDefinition(context, SchemaPath.create(ImmutableList.of(Q_NAME), false)));

        final GroupingDefinition grouping = mock(GroupingDefinition.class);
        doReturn(Q_NAME).when(grouping).getQName();
        doReturn(ImmutableSet.of(grouping)).when(container).getGroupings();
        doReturn(ImmutableSet.of(typeDefinition)).when(grouping).getTypeDefinitions();
        assertEquals(typeDefinition,
                YangSchemaUtils.findTypeDefinition(context, SchemaPath.create(ImmutableList.of(Q_NAME, Q_NAME), false)));

        final DataNodeContainer dataNode =
                mock(DataNodeContainer.class, withSettings().extraInterfaces(DataSchemaNode.class));
        doReturn(dataNode).when(container).getDataChildByName((QName) any());
        doReturn(ImmutableSet.of(typeDefinition)).when(dataNode).getTypeDefinitions();
        assertEquals(typeDefinition,
                YangSchemaUtils.findTypeDefinition(context, SchemaPath.create(ImmutableList.of(Q_NAME, Q_NAME), false)));

        final ChoiceSchemaNode choiceNode =
                mock(ChoiceSchemaNode.class, withSettings().extraInterfaces(DataSchemaNode.class));
        doReturn(choiceNode).when(container).getDataChildByName((QName) any());
        final ChoiceCaseNode caseNode = mock(ChoiceCaseNode.class);
        doReturn(caseNode).when(choiceNode).getCaseNodeByName((QName) any());
        doReturn(ImmutableSet.of(typeDefinition)).when(caseNode).getTypeDefinitions();
        assertEquals(typeDefinition,
                YangSchemaUtils.findTypeDefinition(context,
                        SchemaPath.create(ImmutableList.of(Q_NAME, Q_NAME, Q_NAME), false)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void privateConstructTest() throws Throwable {
        final Constructor constructor = YangSchemaUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (Exception e) {
            throw e.getCause();
        }
    }
}