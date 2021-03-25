/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal554Test {
    @Test
    public void builderTemplateGenerateListenerMethodsTest() {
        final List<Type> genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal554.yang"));
        assertEquals(4, genTypes.size());

        final List<MethodSignature> methods = ((GeneratedType) genTypes.get(3)).getMethodDefinitions();
        assertEquals(3, methods.size());
        assertEquals(methods.get(0).getName(), "onDeprecatedNotification");
        assertEquals(methods.get(0).isDefault(), false);
        assertEquals(methods.get(0).getAnnotations().size(), 1);
        assertEquals(methods.get(0).getAnnotations().get(0).getIdentifier(), JavaTypeName.create(Deprecated.class));
        assertEquals(methods.get(0).getAnnotations().get(0).getParameters().size(), 0);

        assertEquals(methods.get(1).getName(), "onObsoleteNotification");
        assertEquals(methods.get(1).isDefault(), true);
        assertEquals(methods.get(1).getAnnotations().size(), 1);
        assertEquals(methods.get(1).getAnnotations().get(0).getIdentifier(), JavaTypeName.create(Deprecated.class));
        assertEquals(methods.get(1).getAnnotations().get(0).getParameters().size(), 1);
        assertEquals(methods.get(1).getAnnotations().get(0).getParameters().get(0).getName(), "forRemoval");
        assertEquals(methods.get(1).getAnnotations().get(0).getParameters().get(0).getValue(), "true");

        assertEquals(methods.get(2).getName(), "onTestNotification");
        assertEquals(methods.get(2).isDefault(), false);
        assertEquals(methods.get(2).getAnnotations().size(), 0);
    }
}
