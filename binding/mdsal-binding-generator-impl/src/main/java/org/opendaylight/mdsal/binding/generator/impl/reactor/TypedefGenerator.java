/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generator corresponding to a {@code typedef} statement.
 */
public final class TypedefGenerator extends Generator<TypedefEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(TypedefGenerator.class);

    // The generator corresponding to our type's base type. It produces the superclass of our encapsulated type
    private TypedefGenerator baseGenerator;

    TypedefGenerator(final TypedefEffectiveStatement statement) {
        super(statement);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.TYPEDEF;
    }

    void linkBaseGenerator(final GeneratorContext ctx) {
        final TypeDefinition<?> def = statement().getTypeDefinition();
        final TypeDefinition<?> baseType = def.getBaseType();
        if (baseType == null) {
            // Nothing to search for, we are done
            LOG.info("No base type in {}", def);
            return;
        }

        final TypedefGenerator found = ctx.resolveTypedef(baseType.getQName());
        verify(def == baseType, "Expected %s, found unexpected %s", baseType, found);
        baseGenerator = found;
    }
}
