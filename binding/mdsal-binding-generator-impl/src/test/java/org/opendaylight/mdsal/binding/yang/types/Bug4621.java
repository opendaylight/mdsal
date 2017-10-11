/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug4621 {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void bug4621test() {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/bug-4621/foo.yang");
        final Module moduleValid = schemaContext.findModuleByNamespace(URI.create("foo")).iterator().next();
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);

        expectedEx.expect(IllegalArgumentException.class);

        final QName listNode = QName.create(moduleValid.getQNameModule(), "neighbor");
        final QName leafrefNode = QName.create(moduleValid.getQNameModule(), "neighbor2-id");
        DataSchemaNode leafrefRel = ((ListSchemaNode) moduleValid.getDataChildByName(listNode))
                .getDataChildByName(leafrefNode);
        LeafSchemaNode leafRel = (LeafSchemaNode) leafrefRel;
        TypeDefinition<?> leafTypeRel = leafRel.getType();
        assertNotNull(typeProvider.javaTypeForSchemaDefinitionType(leafTypeRel, leafRel));
    }
}