/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.BaseYangTypes;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.model.util.TypeConstants;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code typedef} statement.
 */
public final class TypedefGenerator extends AbstractTypeObjectGenerator<TypedefEffectiveStatement> {
    private static final ImmutableMap<QName, Type> SIMPLE_TYPES = ImmutableMap.<QName, Type>builder()
        .put(TypeDefinitions.BINARY, BaseYangTypes.BINARY_TYPE)
        .put(TypeDefinitions.BOOLEAN, BaseYangTypes.BOOLEAN_TYPE)
        .put(TypeDefinitions.DECIMAL64, BaseYangTypes.DECIMAL64_TYPE)
        .put(TypeDefinitions.EMPTY, BaseYangTypes.EMPTY_TYPE)
        .put(TypeDefinitions.INSTANCE_IDENTIFIER, BaseYangTypes.INSTANCE_IDENTIFIER)
        .put(TypeDefinitions.INT8, BaseYangTypes.INT8_TYPE)
        .put(TypeDefinitions.INT16, BaseYangTypes.INT16_TYPE)
        .put(TypeDefinitions.INT32, BaseYangTypes.INT32_TYPE)
        .put(TypeDefinitions.INT64, BaseYangTypes.INT64_TYPE)
        .put(TypeDefinitions.STRING, BaseYangTypes.STRING_TYPE)
        .put(TypeDefinitions.UINT8, BaseYangTypes.UINT8_TYPE)
        .put(TypeDefinitions.UINT16, BaseYangTypes.UINT16_TYPE)
        .put(TypeDefinitions.UINT32, BaseYangTypes.UINT32_TYPE)
        .put(TypeDefinitions.UINT64, BaseYangTypes.UINT64_TYPE)
        .build();

    // FIXME: typedef/leaf/leaf-list: bits, enumeration, union
    // FIXME: leaf/leaf-list: identityref, leafref

    /**
     * List of all generators for types directly derived from this typedef. We populate this list during initial type
     * linking. It allows us to easily cascade inferences made by this typedef down the type derivation tree.
     */
    private List<AbstractTypeObjectGenerator<?>> derivedGenerators = null;

    TypedefGenerator(final TypedefEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.TYPEDEF;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        throw new UnsupportedOperationException("Cannot push " + statement() + " to data tree");
    }

    void addDerivedGenerator(final AbstractTypeObjectGenerator<?> derivedGenerator) {
        if (derivedGenerators == null) {
            derivedGenerators = new ArrayList<>(4);
        }
        derivedGenerators.add(requireNonNull(derivedGenerator));
    }

    @Override
    void bindDerivedGenerators(final TypeReference reference) {
        // Trigger any derived resolvers ...
        if (derivedGenerators != null) {
            for (AbstractTypeObjectGenerator<?> derived : derivedGenerators) {
                derived.bindTypeDefinition(reference);
            }
        }
        // ... and make sure nobody can come in late
        derivedGenerators = List.of();
    }

    @Override
    GeneratedTransferObject createRootType(final TypeBuilderFactory builderFactory) {
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName());
        builder.setTypedef(true);



        // FIXME: real type?
        final Type javaType = null;


//        newType.setSchemaPath(typedef.getPath());
//        newType.setModuleName(moduleName);
//        addCodegenInformation(newType, typedef);

        final TypeDefinition<?> typedef = statement().getTypeDefinition();
        builder.setRestrictions(BindingGeneratorUtil.getRestrictions(typedef));
        final GeneratedPropertyBuilder genPropBuilder = builder.addProperty(TypeConstants.VALUE_PROP);
        genPropBuilder.setReturnType(javaType);

        builder.addEqualsIdentity(genPropBuilder);
        builder.addHashIdentity(genPropBuilder);
        builder.addToStringProperty(genPropBuilder);
        builder.addImplementsType(BindingTypes.scalarTypeObject(javaType));
        annotateDeprecatedIfNecessary(statement().getTypeDefinition(), builder);

        if (javaType instanceof ConcreteType && "String".equals(javaType.getName()) && typedef.getBaseType() != null) {
//            addStringRegExAsConstant(genTOBuilder, resolveRegExpressionsFromTypedef(typedef));
        }
//        addUnitsToGenTO(genTOBuilder, typedef.getUnits().orElse(null));
        makeSerializable(builder);
        return builder.build();
    }

    @Override
    GeneratedTransferObject createDerivedType(final TypeBuilderFactory builderFactory,
            final GeneratedTransferObject baseType) {
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName());
        builder.setTypedef(true);

        builder.setExtendsType(baseType);

        // FIXME: anything else?

        return builder.build();
    }
}
