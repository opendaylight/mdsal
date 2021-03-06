/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class AugmentRelativeXPathTest extends AbstractTypesTest {

    public AugmentRelativeXPathTest() {
        super(AugmentRelativeXPathTest.class.getResource("/augment-relative-xpath-models"));
    }

    @Test
    public void testAugmentationWithRelativeXPath() {

        final EffectiveModelContext context = YangParserTestUtils.parseYangFiles(testModels);

        assertNotNull("context is null", context);
        final List<Type> genTypes = DefaultBindingGenerator.generateFor(context);

        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());

        GeneratedTransferObject gtInterfaceKey = null;
        GeneratedType gtInterface = null;
        GeneratedType gtTunnel = null;
        GeneratedTransferObject gtTunnelKey = null;

        for (final Type type : genTypes) {
            if (!type.getPackageName().contains("augment._abstract.topology")) {
                continue;
            }

            if (type.getName().equals("InterfaceKey")) {
                gtInterfaceKey = (GeneratedTransferObject) type;
            } else if (type.getName().equals("Interface")) {
                gtInterface = (GeneratedType) type;
            } else if (type.getName().equals("Tunnel")) {
                gtTunnel = (GeneratedType) type;
            } else if (type.getName().equals("TunnelKey")) {
                gtTunnelKey = (GeneratedTransferObject) type;
            }
        }

        // 'Interface
        assertNotNull("Interface is null", gtInterface);
        final List<MethodSignature> gtInterfaceMethods = gtInterface.getMethodDefinitions();
        assertNotNull("Interface methods are null", gtInterfaceMethods);
        MethodSignature getIfcKeyMethod = null;
        for (final MethodSignature method : gtInterfaceMethods) {
            if (BindingMapping.IDENTIFIABLE_KEY_NAME.equals(method.getName())) {
                getIfcKeyMethod = method;
                break;
            }
        }
        assertNotNull("getKey method is null", getIfcKeyMethod);
        assertNotNull("getKey method return type is null", getIfcKeyMethod.getReturnType());
        assertTrue("getKey method return type name must be InterfaceKey", getIfcKeyMethod.getReturnType().getName()
                .equals("InterfaceKey"));

        // 'InterfaceKey'
        assertNotNull("InterfaceKey is null", gtInterfaceKey);
        final List<GeneratedProperty> properties = gtInterfaceKey.getProperties();
        assertNotNull("InterfaceKey properties are null", properties);
        GeneratedProperty gtInterfaceId = null;
        for (final GeneratedProperty property : properties) {
            if (property.getName().equals("interfaceId")) {
                gtInterfaceId = property;
                break;
            }
        }
        assertNotNull("interfaceId is null", gtInterfaceId);
        assertNotNull("interfaceId return type is null", gtInterfaceId.getReturnType());
        assertTrue("interfaceId return type name must be String",
                gtInterfaceId.getReturnType().getName().equals("String"));

        // 'Tunnel'
        assertNotNull("Tunnel is null", gtTunnel);
        final List<MethodSignature> tunnelMethods = gtTunnel.getMethodDefinitions();
        assertNotNull("Tunnel methods are null", tunnelMethods);
        MethodSignature getTunnelKeyMethod = null;
        for (MethodSignature method : tunnelMethods) {
            if (BindingMapping.IDENTIFIABLE_KEY_NAME.equals(method.getName())) {
                getTunnelKeyMethod = method;
                break;
            }
        }
        assertNotNull("getKey method is null", getTunnelKeyMethod);
        assertNotNull("getKey method return type", getTunnelKeyMethod.getReturnType());
        assertTrue("getKey method return type name must be TunnelKey", getTunnelKeyMethod.getReturnType().getName()
                .equals("TunnelKey"));

        // 'TunnelKey'
        assertNotNull("TunnelKey is null", gtTunnelKey);
        final List<GeneratedProperty> tunnelKeyProperties = gtTunnelKey.getProperties();
        assertNotNull("TunnelKey properties are null", tunnelKeyProperties);

        GeneratedProperty gtTunnelId = null;
        for (final GeneratedProperty property : tunnelKeyProperties) {
            if (property.getName().equals("tunnelId")) {
                gtTunnelId = property;
            }
        }
        assertNotNull("tunnelId is null", gtTunnelId);
        assertNotNull("tunnelId return type is null", gtTunnelId.getReturnType());
        assertTrue("tunnelId return type name must be Uri", gtTunnelId.getReturnType().getName().equals("Uri"));
    }

}
