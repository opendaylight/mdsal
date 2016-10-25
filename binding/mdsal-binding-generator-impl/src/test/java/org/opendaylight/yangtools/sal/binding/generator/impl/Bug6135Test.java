/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.util.type.EnumerationTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class Bug6135Test {

    @Test
    public void bug6135Test() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSource(new YangStatementSourceImpl("/bug-6135/foo.yang", false));

        final EffectiveSchemaContext context = reactor.buildEffective();
        assertNotNull(context);

        final List<Type> generateTypes = new BindingGeneratorImpl(false).generateTypes(context);
        assertFalse(generateTypes.isEmpty());

        GeneratedType genInterface = null;
        for (final Type type : generateTypes) {
            if (!(type instanceof Enumeration)) {
                    genInterface = (GeneratedType) type;
            }
        }
        assertNotNull("Generated Type is not present in list of Generated Types", genInterface);
        final List<Enumeration> enums = genInterface.getEnumerations();
        assertEquals(3, enums.size());
    }
}