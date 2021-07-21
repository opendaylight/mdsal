/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.TypeMember;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal502Test {

    private static final JavaTypeName DATA =
        JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal502.norev", "Mdsal502Data");
    private static final JavaTypeName MY_GRP =
        JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal502.norev", "MyGrp");
    private static final JavaTypeName MY_DOT_GRP =
        JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal502.norev", "¤my﹎grp");
    private static final JavaTypeName MY_DASH_GRP =
        JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal502.norev", "¤my﹍grp");
    private static final JavaTypeName MY_UNDER_GRP =
        JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal502.norev", "¤My_grp");
    private static final JavaTypeName MY_CAP_GRP =
        JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal502.norev", "¤MyGrp");
    final List<GeneratedType> types = DefaultBindingGenerator
        .generateFor(YangParserTestUtils.parseYangResource("/mdsal502.yang"));


    @Test
    public void testNamingConflict() {
        assertEquals(6, types.size());
        assertEquals(Set.of(DATA, MY_GRP, MY_DOT_GRP, MY_DASH_GRP, MY_UNDER_GRP, MY_CAP_GRP),
            types.stream().map(GeneratedType::getIdentifier).collect(Collectors.toUnmodifiableSet()));
    }

    @Test
    public void testRequireMethods() {
        final var typesMethodNames = types.stream()
            .map(GeneratedType::getMethodDefinitions)
            .flatMap(Collection::stream)
            .map(TypeMember::getName)
            .filter(name -> name.contains("require"))
            .collect(Collectors.toUnmodifiableSet());

        assertEquals(Set.of("require¤My﹎grp", "require¤My﹍grp", "require¤My_grp", "require¤MyGrp"), typesMethodNames);
    }
}
