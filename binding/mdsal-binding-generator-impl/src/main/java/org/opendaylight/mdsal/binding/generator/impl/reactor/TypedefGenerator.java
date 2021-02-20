/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.TYPE_OBJECT;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.BaseYangTypes;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.model.util.TypeConstants;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.AbstractEnumerationBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
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
    GeneratedType createRootType(final TypeBuilderFactory builderFactory) {
        final ModuleGenerator module = currentModule();
        final String moduleName = module.statement().argument().getLocalName();
        final TypeDefinition<?> typedef = statement().getTypeDefinition();
        final QName typeName = type.argument();

        if (TypeDefinitions.ENUMERATION.equals(typeName)) {
            // enums are automatically Serializable
            final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typedef;
            // TODO units for typedef enum
            //  returnType = provideTypeForEnum(enumTypeDef, typedefName, typedef);
            final AbstractEnumerationBuilder builder = builderFactory.newEnumerationBuilder(typeName());
            //  addEnumDescription(enumBuilder, enumTypeDef);
            enumTypeDef.getReference().ifPresent(builder::setReference);
            builder.setModuleName(moduleName);
            builder.updateEnumPairsFromEnumTypeDef(enumTypeDef);
            return builder.toInstance(null);
        }

        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName());
        builder.setTypedef(true);

        final Type javaType = SIMPLE_TYPES.get(typeName);
        if (javaType != null) {
            builder.addImplementsType(BindingTypes.scalarTypeObject(javaType));

            final GeneratedPropertyBuilder genPropBuilder = builder.addProperty(TypeConstants.VALUE_PROP);
            genPropBuilder.setReturnType(javaType);
            builder.addEqualsIdentity(genPropBuilder);
            builder.addHashIdentity(genPropBuilder);
            builder.addToStringProperty(genPropBuilder);

            builder.setRestrictions(BindingGeneratorUtil.getRestrictions(typedef));

            makeSerializable(builder);
        } else if (TypeDefinitions.BITS.equals(typeName)) {
            builder.addImplementsType(TYPE_OBJECT);
            builder.setBaseType(typedef);

            for (Bit bit : ((BitsTypeDefinition) typedef).getBits()) {
                final String name = bit.getName();
                GeneratedPropertyBuilder genPropertyBuilder = builder.addProperty(BindingMapping.getPropertyName(name));
                genPropertyBuilder.setReadOnly(true);
                genPropertyBuilder.setReturnType(BaseYangTypes.BOOLEAN_TYPE);

                builder.addEqualsIdentity(genPropertyBuilder);
                builder.addHashIdentity(genPropertyBuilder);
                builder.addToStringProperty(genPropertyBuilder);
            }

            makeSerializable(builder);
        } else if (TypeDefinitions.UNION.equals(typeName)) {
//            final GeneratedTOBuilder genTOBuilder = provideGeneratedTOBuilderForUnionTypeDef(
//                JavaTypeName.create(basePackageName, BindingMapping.getClassName(typedef.getQName())),
//                (UnionTypeDefinition) baseTypedef, typedef);
//            genTOBuilder.setIsUnion(true);
//            returnType = genTOBuilder.build();
//
//            // Define a corresponding union builder. Typedefs are always anchored at a Java package root,
//            // so we are placing the builder alongside the union.
//            final GeneratedTOBuilder unionBuilder = newGeneratedTOBuilder(
//                JavaTypeName.create(genTOBuilder.getPackageName(), genTOBuilder.getName() + "Builder"));
//            unionBuilder.setIsUnionBuilder(true);
//            final MethodSignatureBuilder method = unionBuilder.addMethod("getDefaultInstance");
//            method.setReturnType(returnType);
//            method.addParameter(Types.STRING, "defaultValue");
//            method.setAccessModifier(AccessModifier.PUBLIC);
//            method.setStatic(true);

            makeSerializable(builder);

            // FIXME: generate union
            throw new UnsupportedOperationException("union");
        } else {
            throw new IllegalStateException("Unhandled type " + typeName);
        }

//        builder.setSchemaPath(typedef.getPath());
        builder.setModuleName(moduleName);
//        addCodegenInformation(builder, typedef);

        annotateDeprecatedIfNecessary(typedef, builder);

        if (javaType instanceof ConcreteType && "String".equals(javaType.getName()) && typedef.getBaseType() != null) {
//            addStringRegExAsConstant(genTOBuilder, resolveRegExpressionsFromTypedef(typedef));
        }
//        addUnitsToGenTO(genTOBuilder, typedef.getUnits().orElse(null));

        return builder.build();
    }

    @Override
    GeneratedType createDerivedType(final TypeBuilderFactory builderFactory, final GeneratedType baseType) {
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName());
        builder.setTypedef(true);

//        builder.setExtendsType(baseType);

        // FIXME: anything else?

        return builder.build();
    }
}
