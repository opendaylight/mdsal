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

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.ri.BaseYangTypes;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class GeneratedTypesYangDataTest {


    private static final String PACKAGE = "org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.";
    private static final String PACKAGE2 = "org.opendaylight.yang.gen.v1.urn.test.yang.data.collision.rev220222.";
    private static final String MODULE_CLASS_NAME = PACKAGE + "YangDataDemoData";
    private static final String ROOT_CONTAINER_CLASS_NAME = PACKAGE + "RootContainer";

    @Test
    void yangDataGen() {
        final List<GeneratedType> allGenTypes = DefaultBindingGenerator.generateFor(
                YangParserTestUtils.parseYangResources(GeneratedTypesYangDataTest.class,
                        "/ietf-models/ietf-restconf.yang", "/yang-data-demo.yang"));
        assertNotNull(allGenTypes);
        assertEquals(39, allGenTypes.size());

        final Map<String, GeneratedType> genTypesMap = allGenTypes.stream()
                // omit restconf artifacts
                .filter(genType -> genType.getIdentifier().toString().startsWith(PACKAGE))
                .collect(ImmutableMap.toImmutableMap(type -> type.getIdentifier().toString(), type -> type));
        assertEquals(28, genTypesMap.size());

        // ensure generated yang-data classes contain getters for inner structure types

        // yang-data > container
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithContainer"),
                assertGenType(genTypesMap, PACKAGE + "ContainerFromYangData"),
                List.of("getContainerFromYangData", "nonnullContainerFromYangData"));
        // yang-data > list
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithList"),
                Types.listTypeFor(assertGenType(genTypesMap, PACKAGE + "ListFromYangData")),
                List.of("getListFromYangData", "nonnullListFromYangData"));
        // yang-data > leaf
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithLeaf"),
                BaseYangTypes.STRING_TYPE,
                List.of("getLeafFromYangData", "requireLeafFromYangData"));
        // yang-data > leaf-list
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithLeafList"),
                Types.setTypeFor(BaseYangTypes.STRING_TYPE),
                List.of("getLeafListFromYangData", "requireLeafListFromYangData"));
        // yang-data > anydata
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithAnydata"),
                assertGenType(genTypesMap, PACKAGE + "AnydataFromYangData"),
                List.of("getAnydataFromYangData", "requireAnydataFromYangData"));
        // yang-data > anyxml
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithAnyxml"),
                assertGenType(genTypesMap, PACKAGE + "AnyxmlFromYangData"),
                List.of("getAnyxmlFromYangData", "requireAnyxmlFromYangData"));

        // ensure generated yang-data classes extending inner group so group content is reachable

        // yang-data > uses > group > container
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithContainerFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForContainer"),
                assertGenType(genTypesMap, PACKAGE + "grp._for.container.ContainerFromGroup"),
                List.of("getContainerFromGroup", "nonnullContainerFromGroup"));
        // yang-data > uses > group > list
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithListFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForList"),
                Types.listTypeFor(assertGenType(genTypesMap, PACKAGE + "grp._for.list.ListFromGroup")),
                List.of("getListFromGroup", "nonnullListFromGroup")
        );
        // yang-data > uses > group > leaf
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithLeafFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForLeaf"),
                BaseYangTypes.UINT32_TYPE,
                List.of("getLeafFromGroup", "requireLeafFromGroup"));
        // yang-data > uses > group > leaf-list
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithLeafListFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForLeafList"),
                Types.setTypeFor(BaseYangTypes.UINT32_TYPE),
                List.of("getLeafListFromGroup", "requireLeafListFromGroup"));
        // yang-data > uses > group > anydata
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithAnydataFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForAnydata"),
                assertGenType(genTypesMap, PACKAGE + "grp._for.anydata.AnydataFromGroup"),
                List.of("getAnydataFromGroup", "requireAnydataFromGroup"));
        // yang-data > uses > group > anyxml
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithAnyxmlFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForAnyxml"),
                assertGenType(genTypesMap, PACKAGE + "grp._for.anyxml.AnyxmlFromGroup"),
                List.of("getAnyxmlFromGroup", "requireAnyxmlFromGroup"));


        // ensure module class has only getter for root container
        final GeneratedType moduleType = assertGenType(genTypesMap, MODULE_CLASS_NAME);
        assertNotNull(moduleType.getMethodDefinitions());
        assertEquals(List.of("getRootContainer"),
                moduleType.getMethodDefinitions().stream().map(MethodSignature::getName)
                        .filter(methodName -> methodName.startsWith("get")).toList());

        // ensure yang-data at non-top level is ignored (no getters in parent container)
        GeneratedType rootContainerType = assertGenType(genTypesMap, ROOT_CONTAINER_CLASS_NAME);
        assertNotNull(rootContainerType.getMethodDefinitions());
        assertTrue(rootContainerType.getMethodDefinitions().stream()
                .filter(method -> method.getName().startsWith("get"))
                .findFirst().isEmpty());
    }

    @Test
    public void nameCollisions() {
        final List<GeneratedType> allGenTypes = DefaultBindingGenerator.generateFor(
                YangParserTestUtils.parseYangResources(GeneratedTypesYangDataTest.class,
                        "/ietf-models/ietf-restconf.yang", "/yang-data-collision.yang"));
        assertNotNull(allGenTypes);
        final Set<String> genTypeNames =
                allGenTypes.stream().map(type -> type.getIdentifier().toString()).collect(Collectors.toSet());

        // name collision with inner container
        assertTrue(genTypeNames.contains(PACKAGE2 + "Collision1"));
        // TODO fix once it's clarified either either yang-data child should be allocated at same package

        // name collision with top level container
        assertTrue(genTypeNames.contains(PACKAGE2 + "Collision2"));
        assertTrue(genTypeNames.contains(PACKAGE2 + "Collision2$YD"));

        // name collision
        assertTrue(genTypeNames.contains(PACKAGE2 + "Collision3$G"));
        assertTrue(genTypeNames.contains(PACKAGE2 + "Collision3$YD"));

    }

    private static GeneratedType assertGenType(final Map<String, GeneratedType> genTypesMap, final String className) {
        assertTrue(genTypesMap.containsKey(className), "no type generated: " + className);
        return genTypesMap.get(className);
    }

    private static void assertYangDataGenType(final GeneratedType yangDataType, final Type contentType,
            List<String> getterMethods) {
        assertImplements(yangDataType, BindingTypes.DATA_ROOT);
        assertImplements(yangDataType, BindingTypes.DATA_OBJECT);
        for (final String methodName : getterMethods) {
            assertHasMethod(yangDataType, methodName, contentType);
        }
    }

    private static void assertYangDataGenType(final GeneratedType yangDataType, final GeneratedType groupType,
            final Type contentType, final List<String> getterMethods) {
        assertImplements(yangDataType, BindingTypes.DATA_ROOT);
        assertImplements(yangDataType, BindingTypes.DATA_OBJECT);
        assertImplements(yangDataType, groupType);
        for (final String methodName : getterMethods) {
            assertHasMethod(groupType, methodName, contentType);
        }
    }

    private static void assertHasMethod(final GeneratedType genType, final String methodName,
            final Type returnType) {
        assertTrue(genType.getMethodDefinitions().stream()
                        .filter(method -> methodName.equals(method.getName())
                                && returnType.equals(method.getReturnType())).findFirst().isPresent(),
                "no expected method " + methodName + " returning " + returnType);
    }

    private static void assertImplements(final GeneratedType genType, final Type implementedType) {
        assertTrue(genType.getImplements().stream().filter(implementedType::equals).findFirst().isPresent(),
                "no expected implementation of " + implementedType);
    }
}
