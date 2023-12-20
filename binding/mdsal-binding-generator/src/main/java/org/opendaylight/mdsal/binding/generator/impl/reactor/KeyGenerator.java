/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultKeyRuntimeType;
import org.opendaylight.mdsal.binding.model.api.DataObjectField;
import org.opendaylight.mdsal.binding.model.api.KeyArchetype;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.KeyRuntimeType;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class KeyGenerator extends AbstractExplicitGenerator<KeyEffectiveStatement, KeyRuntimeType> {
    private final ListGenerator listGen;

    KeyGenerator(final KeyEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent,
            final ListGenerator listGen) {
        super(statement, parent);
        this.listGen = requireNonNull(listGen);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.KEY;
    }

    @Override
    void pushToInference(final SchemaInferenceStack inferenceStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    Member createMember(final CollisionDomain domain) {
        return domain.addSecondary(this, listGen.getMember(), Naming.KEY_SUFFIX);
    }

    @Override
    KeyArchetype createTypeImpl() {
//        final var builder = builderFactory.newGeneratedTOBuilder(typeName());
//
//        builder.addImplementsType(BindingTypes.key(Type.of(listGen.typeName())));

        final var leafNames = statement().argument();
        final var builder = ImmutableList.<DataObjectField<?>>builderWithExpectedSize(leafNames.size());
        for (var listChild : listGen) {
            if (listChild instanceof LeafGenerator leafGen) {
                final var qname = leafGen.statement().argument();
                if (leafNames.contains(qname)) {
                    builder.add(leafGen.generateDataObjectField());

//                    final var prop = builder
//                        .addProperty(Naming.getPropertyName(qname.getLocalName()))
//                        .setReturnType(leafGen.methodReturnType(builderFactory))
//                        .setReadOnly(true);
//
////                    addComment(propBuilder, leaf);
//
//                    builder.addEqualsIdentity(prop);
//                    builder.addHashIdentity(prop);
//                    builder.addToStringProperty(prop);
                }
            }
        }

//        // serialVersionUID
//        addSerialVersionUID(builder);

        return new KeyArchetype(typeName(), statement(), listGen.typeName(), builder.build());
    }

    @Override
    KeyRuntimeType createExternalRuntimeType(final Type type) {
        if (type instanceof KeyArchetype archetype) {
            return new DefaultKeyRuntimeType(archetype);
        }
        throw new VerifyException("Unexpected type %s" + type);
    }

    @Override
    KeyRuntimeType createInternalRuntimeType(final AugmentResolver resolver, final KeyEffectiveStatement statement,
            final Type type) {
        // The only reference to this runtime type is from ListGenerator which is always referencing the external type
        throw new UnsupportedOperationException("Should never be called");
    }

    @Override
    DataObjectField<?> generateDataObjectField() {
        // Keys are explicitly handled by their corresponding list
        return null;
    }
}
