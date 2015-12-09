/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class Bug4621 {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void bug4621test() throws ReactorException, URISyntaxException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        File file = new File(getClass().getResource("/bug-4621/foo.yang").toURI());
        reactor.addSource(new YangStatementSourceImpl(file.getPath(), true));

        final SchemaContext schemaContext = reactor.buildEffective();
        final Module moduleValid = schemaContext.findModuleByNamespace(new URI("foo")).iterator().next();
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);

        expectedEx.expect(IllegalArgumentException.class);

        DataSchemaNode leafrefRel = ((ListSchemaNode) moduleValid.getDataChildByName("neighbor")).getDataChildByName
                ("neighbor2-id");
        LeafSchemaNode leafRel = (LeafSchemaNode) leafrefRel;
        TypeDefinition<?> leafTypeRel = leafRel.getType();
        Type leafrefRelResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafTypeRel, leafRel);
    }
}