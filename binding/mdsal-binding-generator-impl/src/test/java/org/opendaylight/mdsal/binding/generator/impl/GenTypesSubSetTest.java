/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class GenTypesSubSetTest {

    @Test
    public void genTypesFromSubsetOfTwoModulesTest() {
        final SchemaContext context = YangParserTestUtils.parseYangResources(GenTypesSubSetTest.class,
                "/leafref-test-models/abstract-topology@2013-02-08.yang", "/ietf/ietf-interfaces.yang",
                "/ietf/ietf-inet-types.yang", "/ietf/ietf-yang-types.yang");
        final Set<Module> toGenModules = new HashSet<>();
        for (final Module module : context.getModules()) {
            if (module.getName().equals("abstract-topology")) {
                toGenModules.add(module);
            } else if (module.getName().equals("ietf-interfaces")) {
                toGenModules.add(module);
            }
        }

        assertEquals("Set of to Generate Modules must contain 2 modules", 2, toGenModules.size());
        assertNotNull("Schema Context is null", context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context, toGenModules);
        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());
        assertEquals("Expected Generated Types from provided sub set of " + "modules should be 23!", 23,
                genTypes.size());
    }

    @Test
    public void genTypesFromSubsetOfThreeModulesTest() {
        final SchemaContext context = YangParserTestUtils.parseYangResources(GenTypesSubSetTest.class,
                "/leafref-test-models/abstract-topology@2013-02-08.yang", "/ietf/ietf-interfaces.yang",
                "/ietf/ietf-inet-types.yang", "/ietf/ietf-yang-types.yang", "/ietf/iana-if-type.yang");
        assertNotNull("Schema Context is null", context);

        final Set<Module> toGenModules = new HashSet<>();
        for (final Module module : context.getModules()) {
            if (module.getName().equals("abstract-topology")) {
                toGenModules.add(module);
            } else if (module.getName().equals("ietf-interfaces")) {
                toGenModules.add(module);
            } else if (module.getName().equals("iana-if-type")) {
                toGenModules.add(module);
            }
        }
        assertEquals("Set of to Generate Modules must contain 3 modules", 3, toGenModules.size());

        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context, toGenModules);
        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());
        assertEquals("Expected Generated Types", 24, genTypes.size());
    }
}
