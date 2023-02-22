/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.Enumeration.Pair;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class GenEnumResolvingTest {
    @Test
    public void testLeafEnumResolving() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            GenEnumResolvingTest.class,
            "/enum-test-models/ietf-interfaces@2012-11-15.yang", "/ietf-models/iana-if-type.yang"));
        assertNotNull(genTypes);

        assertEquals(9, genTypes.size());

        GeneratedType genInterface = null;
        for (final GeneratedType type : genTypes) {
            if (type.getName().equals("Interface")) {
                genInterface = type;
            }
        }
        assertNotNull("Generated Type Interface is not present in list of Generated Types", genInterface);

        Enumeration linkUpDownTrapEnable = null;
        Enumeration operStatus = null;
        final List<Enumeration> enums = genInterface.getEnumerations();
        assertNotNull("Generated Type Interface cannot contain NULL reference to Enumeration types!", enums);
        assertEquals("Generated Type Interface MUST contain 2 Enumeration Types", 2, enums.size());
        for (final Enumeration e : enums) {
            if (e.getName().equals("LinkUpDownTrapEnable")) {
                linkUpDownTrapEnable = e;
            } else if (e.getName().equals("OperStatus")) {
                operStatus = e;
            }
        }

        assertNotNull("Expected Enum LinkUpDownTrapEnable, but was NULL!", linkUpDownTrapEnable);
        assertNotNull("Expected Enum OperStatus, but was NULL!", operStatus);

        assertNotNull("Enum LinkUpDownTrapEnable MUST contain Values definition not NULL reference!",
                linkUpDownTrapEnable.getValues());
        assertNotNull("Enum OperStatus MUST contain Values definition not NULL reference!", operStatus.getValues());
        assertEquals("Enum LinkUpDownTrapEnable MUST contain 2 values!", 2, linkUpDownTrapEnable.getValues().size());
        assertEquals("Enum OperStatus MUST contain 7 values!", 7, operStatus.getValues().size());

        final List<MethodSignature> methods = genInterface.getMethodDefinitions();

        assertNotNull("Generated Interface cannot contain NULL reference for Method Signature Definitions!", methods);

        // FIXME: split this into getter/default/static asserts
        assertEquals(33, methods.size());
        Enumeration ianaIfType = null;
        for (final MethodSignature method : methods) {
            if (method.getName().equals("getType")) {
                if (method.getReturnType() instanceof Enumeration) {
                    ianaIfType = (Enumeration) method.getReturnType();
                }
            }
        }

        assertNotNull("Method getType MUST return Enumeration Type not NULL reference!", ianaIfType);
        assertEquals("Enumeration getType MUST contain 272 values!", 272, ianaIfType.getValues().size());
    }

    @Test
    public void testTypedefEnumResolving() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/ietf-models/iana-if-type.yang"));
        assertNotNull(genTypes);
        assertEquals(2, genTypes.size());

        final GeneratedType type = genTypes.get(1);
        assertThat(type, instanceOf(Enumeration.class));

        final Enumeration enumer = (Enumeration) type;
        assertEquals("Enumeration type MUST contain 272 values!", 272, enumer.getValues().size());
    }

    @Test
    public void testLeafrefEnumResolving() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            GenEnumResolvingTest.class,
            "/enum-test-models/abstract-topology@2013-02-08.yang", "/enum-test-models/ietf-interfaces@2012-11-15.yang",
            "/ietf-models/iana-if-type.yang"));
        assertNotNull(genTypes);
        assertEquals(27, genTypes.size());

        GeneratedType genInterface = null;
        for (final GeneratedType type : genTypes) {
            if (type.getName().equals("Interface") && type.getPackageName().equals(
                "org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208.topology.interfaces")) {
                genInterface = type;
            }
        }
        assertNotNull("Generated Type Interface is not present in list of Generated Types", genInterface);

        Type linkUpDownTrapEnable = null;
        Type operStatus = null;
        final List<MethodSignature> methods = genInterface.getMethodDefinitions();
        assertNotNull("Generated Type Interface cannot contain NULL reference to Enumeration types!", methods);

        // FIXME: split this into getter/default/static asserts
        assertEquals(13, methods.size());
        for (final MethodSignature method : methods) {
            if (method.getName().equals("getLinkUpDownTrapEnable")) {
                linkUpDownTrapEnable = method.getReturnType();
            } else if (method.getName().equals("getOperStatus")) {
                operStatus = method.getReturnType();
            }
        }

        assertNotNull("Expected Referenced Enum LinkUpDownTrapEnable, but was NULL!", linkUpDownTrapEnable);
        assertThat(linkUpDownTrapEnable, instanceOf(Enumeration.class));
        assertEquals(
            "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev121115.interfaces.Interface",
            linkUpDownTrapEnable.getIdentifier().immediatelyEnclosingClass().get().toString());

        assertNotNull("Expected Referenced Enum OperStatus, but was NULL!", operStatus);
        assertThat(operStatus, instanceOf(Enumeration.class));
        assertEquals(
            "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev121115.interfaces.Interface",
            operStatus.getIdentifier().immediatelyEnclosingClass().get().toString());
    }

    @Test
    public void testEnumNamesMapping() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/enum-test-models/enum-names-mapping@2023-02-17.yang"));
        assertNotNull(genTypes);
        assertEquals(4, genTypes.size());

        // ------------------- container test-enums -----------------------
        final List<Enumeration> testEnums = genTypes.get(1).getEnumerations();
        assertEquals(4, testEnums.size());

        final List<Pair> dollarContaining = testEnums.get(0).getValues();
        assertEquals(5, dollarContaining.size());
        assertEquals("$", dollarContaining.get(0).getMappedName());
        assertEquals("$abc", dollarContaining.get(1).getMappedName());
        assertEquals("A$bc", dollarContaining.get(2).getMappedName());
        assertEquals("Ab$c", dollarContaining.get(3).getMappedName());
        assertEquals("Abc$", dollarContaining.get(4).getMappedName());

        final List<Pair> prefixRequired = testEnums.get(1).getValues();
        assertEquals(2, prefixRequired.size());
        assertEquals("_09", prefixRequired.get(0).getMappedName());
        assertEquals("_1337LeetPro", prefixRequired.get(1).getMappedName());

        final List<Pair> invalidIdentifier = testEnums.get(2).getValues();
        assertEquals(1, invalidIdentifier.size());
        assertEquals("$_", invalidIdentifier.get(0).getMappedName());

        final List<Pair> invalidChars = testEnums.get(3).getValues();
        assertEquals(5, invalidChars.size());
        assertEquals("$$2A$", invalidChars.get(0).getMappedName());
        assertEquals("$$2E$", invalidChars.get(1).getMappedName());
        assertEquals("$$2F$", invalidChars.get(2).getMappedName());
        assertEquals("$$3F$", invalidChars.get(3).getMappedName());
        assertEquals("$a$2A$a", invalidChars.get(4).getMappedName());
        // ---------------------------------------------------------------------

        // ------------------- container okay-identifier -----------------------
        final List<Enumeration> okayIdentifier = genTypes.get(2).getEnumerations();
        assertEquals(2, okayIdentifier.size());

        final List<Pair> underscores = okayIdentifier.get(0).getValues();
        assertEquals(1, underscores.size());
        assertEquals("__", underscores.get(0).getMappedName());

        final List<Pair> wordsCapitalCamelCase = okayIdentifier.get(1).getValues();
        assertEquals(2, wordsCapitalCamelCase.size());
        assertEquals("True", wordsCapitalCamelCase.get(0).getMappedName());
        assertEquals("ĽaľahoPapľuhu", wordsCapitalCamelCase.get(1).getMappedName());
        // ---------------------------------------------------------------------

        // ------------------- container conflicting-names -----------------------
        final List<Enumeration> conflictingNames = genTypes.get(3).getEnumerations();
        assertEquals(4, conflictingNames.size());

        final List<Pair> conflict1 = conflictingNames.get(0).getValues();
        assertEquals(3, conflict1.size());
        assertEquals("_09", conflict1.get(0).getMappedName());
        assertEquals("$09", conflict1.get(1).getMappedName());
        assertEquals("$0$2D$9", conflict1.get(2).getMappedName());

        final List<Pair> conflict2 = conflictingNames.get(1).getValues();
        assertEquals(2, conflict2.size());
        assertEquals("aZ", conflict2.get(0).getMappedName());
        assertEquals("$a$2D$z", conflict2.get(1).getMappedName());

        final List<Pair> conflict3 = conflictingNames.get(2).getValues();
        assertEquals(3, conflict3.size());
        assertEquals("$a2$2E$5", conflict3.get(0).getMappedName());
        assertEquals("a25", conflict3.get(1).getMappedName());
        assertEquals("$a2$2D$5", conflict3.get(2).getMappedName());

        final List<Pair> conflict4 = conflictingNames.get(3).getValues();
        assertEquals(2, conflict4.size());
        assertEquals("$ľaľaho$20$papľuhu", conflict4.get(0).getMappedName());
        assertEquals("$ľaľaho$20$$20$papľuhu", conflict4.get(1).getMappedName());
        // ---------------------------------------------------------------------
    }
}
