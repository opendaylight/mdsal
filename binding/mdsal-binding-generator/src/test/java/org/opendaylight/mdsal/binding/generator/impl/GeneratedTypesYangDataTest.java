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
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class GeneratedTypesYangDataTest {

    private static final String PACKAGE = "org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222";
    private static final String MODULE_CLASS_NAME = PACKAGE + ".YangDataExtDemoData";
    private static final String CONTAINER_FROM_YANG_DATA_CLASSNAME = PACKAGE + ".ContainerFromYangData";
    private static final String CONTAINER_FROM_GROUP_CLASSNAME = PACKAGE + ".grp.ContainerFromGroup";
    private static final String MODULE_ROOT_CONTAINER_CLASSNAME = PACKAGE + ".RootContainer";
    private static final String GROUPPING_CLASSNAME = PACKAGE + ".Grp";

    @Test
    void yangDataGen() {
        final List<GeneratedType> allGenTypes = DefaultBindingGenerator.generateFor(
                YangParserTestUtils.parseYangResources(GeneratedTypesYangDataTest.class,
                        "/ietf-models/ietf-restconf.yang", "/yang-data-ext-demo.yang"));
        assertNotNull(allGenTypes);

        final Map<String, GeneratedType> genTypesMap = allGenTypes.stream()
                // filter out ietf-restconf types generated
                .filter(type -> type.getIdentifier().packageName().startsWith(PACKAGE))
                // map by class name
                .collect(Collectors.toMap(type -> type.getIdentifier().toString(), type -> type));

        final Set<String> expectedGenClassNames = Set.of(
                MODULE_CLASS_NAME,
                CONTAINER_FROM_YANG_DATA_CLASSNAME,
                CONTAINER_FROM_GROUP_CLASSNAME,
                MODULE_ROOT_CONTAINER_CLASSNAME,
                GROUPPING_CLASSNAME);
        assertEquals(expectedGenClassNames, genTypesMap.keySet());

        // ensure module class has getters for all root containers incl yang-data defined
        final GeneratedType moduleType = genTypesMap.get(MODULE_CLASS_NAME);
        assertNotNull(moduleType.getMethodDefinitions());
        assertEquals(6, moduleType.getMethodDefinitions().size());

        assertHasMethod(moduleType, "getContainerFromYangData", CONTAINER_FROM_YANG_DATA_CLASSNAME);
        assertHasMethod(moduleType, "nonnullContainerFromYangData", CONTAINER_FROM_YANG_DATA_CLASSNAME);
        assertHasMethod(moduleType, "getContainerFromGroup", CONTAINER_FROM_GROUP_CLASSNAME);
        assertHasMethod(moduleType, "nonnullContainerFromGroup", CONTAINER_FROM_GROUP_CLASSNAME);
        assertHasMethod(moduleType, "getRootContainer", MODULE_ROOT_CONTAINER_CLASSNAME);
        assertHasMethod(moduleType, "nonnullRootContainer", MODULE_ROOT_CONTAINER_CLASSNAME);

        // ensure yang-data at non-top level is ignored (no getters)
        final GeneratedType rootContainerType = genTypesMap.get(MODULE_ROOT_CONTAINER_CLASSNAME);
        assertNotNull(rootContainerType.getMethodDefinitions());
        assertTrue(rootContainerType.getMethodDefinitions().stream()
                .filter(method -> method.getName().startsWith("get"))
                .findFirst().isEmpty());

    }

    private static void assertHasMethod(final GeneratedType genType, final String methodName,
            final String returnTypeClassName) {
        genType.getMethodDefinitions().stream()
                .filter(method -> methodName.equals(method.getName())
                        && returnTypeClassName.equals(method.getReturnType().getIdentifier().toString()))
                .findFirst().orElseThrow();
    }
}
