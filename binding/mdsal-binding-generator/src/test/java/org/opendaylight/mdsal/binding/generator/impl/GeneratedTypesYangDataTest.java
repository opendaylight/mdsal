/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.ri.BaseYangTypes;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class GeneratedTypesYangDataTest {

    private static final String PACKAGE = "org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222";
    private static final String MODULE_CLASS_NAME = PACKAGE + ".YangDataDemoData";
    private static final String ROOT_CONTAINER_CLASS_NAME = PACKAGE + ".RootContainer";
    private static final String YD_CONTAINER_CLASS_NAME = PACKAGE + ".YangDataContainer";
    private static final String YD_LIST_CLASS_NAME = PACKAGE + ".YangDataList";
    private static final String YD_ANYDATA_CLASS_NAME = PACKAGE + ".YangDataAnydata";
    private static final String YD_ANYXML_CLASS_NAME = PACKAGE + ".YangDataAnyxml";
    private static final String GRP_OF_CONTAINER_CLASS_NAME = PACKAGE + ".GrpContainer";
    private static final String GRP_OF_LIST_CLASS_NAME = PACKAGE + ".GrpList";
    private static final String GRP_OF_LEAF_CLASS_NAME = PACKAGE + ".GrpLeaf";
    private static final String GRP_OF_LEAF_LIST_CLASS_NAME = PACKAGE + ".GrpLeafList";
    private static final String GRP_OF_ANYDATA_CLASS_NAME = PACKAGE + ".GrpAnydata";
    private static final String GRP_OF_ANYXML_CLASS_NAME = PACKAGE + ".GrpAnyxml";
    private static final String YD_GRP_CONTAINER_CLASS_NAME = PACKAGE + ".grp.container.ContainerFromGroup";
    private static final String YD_GRP_LIST_CLASS_NAME = PACKAGE + ".grp.list.ListFromGroup";
    private static final String YD_GRP_ANYDATA_CLASS_NAME = PACKAGE + ".grp.anydata.AnydataFromGroup";
    private static final String YD_GRP_ANYXML_CLASS_NAME = PACKAGE + ".grp.anyxml.AnyxmlFromGroup";

    @Test
    void yangDataGen() {
        final List<GeneratedType> allGenTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResources(GeneratedTypesYangDataTest.class,
                "/ietf-models/ietf-restconf.yang", "/yang-data-demo.yang"));
        assertNotNull(allGenTypes);

        final Map<String, GeneratedType> genTypesMap = allGenTypes.stream()
            // filter out ietf-restconf types generated
            .filter(type -> type.getIdentifier().packageName().startsWith(PACKAGE))
            // map by class name
            .collect(Collectors.toMap(type -> type.getIdentifier().toString(), type -> type));

        final Set<String> expectedGenClassNames = Set.of(
            MODULE_CLASS_NAME, ROOT_CONTAINER_CLASS_NAME,
            YD_CONTAINER_CLASS_NAME, YD_LIST_CLASS_NAME,
            YD_ANYDATA_CLASS_NAME, YD_ANYXML_CLASS_NAME,
            GRP_OF_CONTAINER_CLASS_NAME, GRP_OF_LIST_CLASS_NAME,
            GRP_OF_LEAF_CLASS_NAME, GRP_OF_LEAF_LIST_CLASS_NAME,
            GRP_OF_ANYDATA_CLASS_NAME, GRP_OF_ANYXML_CLASS_NAME,
            YD_GRP_CONTAINER_CLASS_NAME, YD_GRP_LIST_CLASS_NAME,
            YD_GRP_ANYDATA_CLASS_NAME, YD_GRP_ANYXML_CLASS_NAME);
        assertEquals(expectedGenClassNames, genTypesMap.keySet());

        // ensure module class has getters for all root containers incl yang-data defined
        final GeneratedType moduleType = genTypesMap.get(MODULE_CLASS_NAME);
        assertNotNull(moduleType.getMethodDefinitions());
        assertEquals(15, moduleType.getMethodDefinitions().size());

        final GeneratedType rootContainerType = genTypesMap.get(ROOT_CONTAINER_CLASS_NAME);
        assertHasMethod(moduleType, "getRootContainer", rootContainerType);
        assertHasMethod(moduleType, "nonnullRootContainer", rootContainerType);
        final Type yangDataContainerType = genTypesMap.get(YD_CONTAINER_CLASS_NAME);
        assertHasMethod(moduleType, "getYangDataContainer", yangDataContainerType);
        assertHasMethod(moduleType, "nonnullYangDataContainer", yangDataContainerType);
        final Type yangDataListType = Types.listTypeFor(genTypesMap.get(YD_LIST_CLASS_NAME));
        assertHasMethod(moduleType, "getYangDataList", yangDataListType);
        assertHasMethod(moduleType, "nonnullYangDataList", yangDataListType);
        assertHasMethod(moduleType, "getYangDataLeaf", BaseYangTypes.STRING_TYPE);
        assertHasMethod(moduleType, "requireYangDataLeaf", BaseYangTypes.STRING_TYPE);
        final Type yangDataLeafListType = Types.setTypeFor(BaseYangTypes.STRING_TYPE);
        assertHasMethod(moduleType, "getYangDataLeafList", yangDataLeafListType);
        assertHasMethod(moduleType, "requireYangDataLeafList", yangDataLeafListType);
        final Type yangDataAnydataType = genTypesMap.get(YD_ANYDATA_CLASS_NAME);
        assertHasMethod(moduleType, "getYangDataAnydata", yangDataAnydataType);
        assertHasMethod(moduleType, "requireYangDataAnydata", yangDataAnydataType);
        final Type yangDataAnyxmlType = genTypesMap.get(YD_ANYXML_CLASS_NAME);
        assertHasMethod(moduleType, "getYangDataAnyxml", yangDataAnyxmlType);
        assertHasMethod(moduleType, "requireYangDataAnyxml", yangDataAnyxmlType);

        // ensure module interface extends interfaces of groups used within yang-data,
        // it means there will be an API to root elements of these groups from module interface
        assertImplements(moduleType, Type.of(DataRoot.class));
        assertImplements(moduleType, genTypesMap.get(GRP_OF_CONTAINER_CLASS_NAME));
        assertImplements(moduleType, genTypesMap.get(GRP_OF_LIST_CLASS_NAME));
        assertImplements(moduleType, genTypesMap.get(GRP_OF_LEAF_CLASS_NAME));
        assertImplements(moduleType, genTypesMap.get(GRP_OF_LEAF_LIST_CLASS_NAME));
        assertImplements(moduleType, genTypesMap.get(GRP_OF_ANYDATA_CLASS_NAME));
        assertImplements(moduleType, genTypesMap.get(GRP_OF_ANYXML_CLASS_NAME));

        // ensure yang-data at non-top level is ignored (no getters)
        assertNotNull(rootContainerType.getMethodDefinitions());
        assertTrue(rootContainerType.getMethodDefinitions().stream()
            .filter(method -> method.getName().startsWith("get"))
            .findFirst().isEmpty());

    }

    private static void assertHasMethod(final GeneratedType genType, final String methodName,
        final Type returnType) {
        assertTrue(genType.getMethodDefinitions().stream()
                .filter(method -> methodName.equals(method.getName())
                    && returnType.equals(method.getReturnType())).findFirst().isPresent(),
            "no expected method " + methodName);
    }

    private static void assertImplements(final GeneratedType genType, final Type implementedType) {
        assertTrue(genType.getImplements().stream().filter(implementedType::equals).findFirst().isPresent(),
            "no expected implementation of " + implementedType);
    }
}
