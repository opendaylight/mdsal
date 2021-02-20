/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.model.api.DefaultType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class KeyGenerator extends AbstractExplicitGenerator<KeyEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(KeyGenerator.class);
    // FIXME: this should be a well-known constant
    private static final String SUFFIX = "Key";

    private final ListGenerator listGen;

    KeyGenerator(final KeyEffectiveStatement statement, final AbstractCompositeGenerator<?> parent,
            final ListGenerator listGen) {
        super(statement, parent);
        this.listGen = requireNonNull(listGen);
    }

    @Override
    void pushToInference(final SchemaInferenceStack inferenceStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    @NonNull Member createMember() {
        return parent().domain().addSecondary(listGen.getMember(), SUFFIX);
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName());

        builder.addImplementsType(BindingTypes.identifier(DefaultType.of(listGen.typeName())));

        final Set<QName> leafNames = statement().argument();
        for (Generator listChild : listGen) {
            if (listChild instanceof LeafGenerator) {
                final LeafGenerator leafGen = (LeafGenerator) listChild;
                if (leafNames.contains(leafGen.statement().argument())) {
                    // FIXME: add property
                    LOG.info("Should add leaf {}", listChild);
                }
            }
        }

        // serialVersionUID
        addSerialVersionUID(builder);

        return builder.build();
    }
}
