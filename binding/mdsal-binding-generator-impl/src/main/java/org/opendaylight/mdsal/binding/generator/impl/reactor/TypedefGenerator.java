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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.util.BaseYangTypes;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.model.util.TypeConstants;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.AbstractEnumerationBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code typedef} statement.
 */
final class TypedefGenerator extends AbstractTypeObjectGenerator<TypedefEffectiveStatement> {
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
        // No-op
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
    ClassPlacement classPlacementImpl(final TypedefGenerator baseGen) {
        return ClassPlacement.TOP_LEVEL;
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

            statement().findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class)
                .ifPresent(builder::setReference);

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
        } else if (TypeDefinitions.UNION.equals(typeName)) {
            builder.addImplementsType(TYPE_OBJECT);
            builder.setIsUnion(true);

//            builder.setSchemaPath(typedef.getPath());
            builder.setModuleName(moduleName);
//            addCodegenInformation(builder, typedef);

            // Pattern string is the key, XSD regex is the value. The reason for this choice is that the pattern carries
            // also negation information and hence guarantees uniqueness.
            final Map<String, String> expressions = new HashMap<>();
            for (TypeDefinition<?> unionType : ((UnionTypeDefinition) typedef).getTypes()) {
                final String unionTypeName = unionType.getQName().getLocalName();

                // If we have a base type we should follow the type definition backwards, except for identityrefs, as
                // those do not follow type encapsulation -- we use the general case for that.
//                if (unionType.getBaseType() != null  && !(unionType instanceof IdentityrefTypeDefinition)) {
//                    resolveExtendedSubtypeAsUnion(builder, unionType, expressions, parentNode);
//                } else if (unionType instanceof UnionTypeDefinition) {
//                    generatedTOBuilders.addAll(resolveUnionSubtypeAsUnion(builder,
//                        (UnionTypeDefinition) unionType, parentNode));
//                } else if (unionType instanceof EnumTypeDefinition) {
//                    final Enumeration enumeration = addInnerEnumerationToTypeBuilder((EnumTypeDefinition) unionType,
//                        unionTypeName, builder);
//                    updateUnionTypeAsProperty(builder, enumeration, unionTypeName);
//                } else {
//                    final Type javaType = javaTypeForSchemaDefinitionType(unionType, parentNode);
//                    updateUnionTypeAsProperty(builder, javaType, unionTypeName);
//                }
            }
            addStringRegExAsConstant(builder, expressions);

//            final GeneratedTOBuilder resultTOBuilder = builders.remove(0);
//            builders.forEach(resultTOBuilder::addEnclosingTransferObject);
//            return resultTOBuilder;

//            final GeneratedTOBuilder genTOBuilder = provideGeneratedTOBuilderForUnionTypeDef(
//                JavaTypeName.create(basePackageName, BindingMapping.getClassName(typedef.getQName())),
//                (UnionTypeDefinition) baseTypedef, typedef);
//            genTOBuilder.setIsUnion(true);
//            returnType = genTOBuilder.build();

            // Define a corresponding union builder. Typedefs are always anchored at a Java package root,
            // so we are placing the builder alongside the union.
//            final GeneratedTOBuilder unionBuilder = newGeneratedTOBuilder(
//                JavaTypeName.create(genTOBuilder.getPackageName(), genTOBuilder.getName() + "Builder"));
//            unionBuilder.setIsUnionBuilder(true);
//            final MethodSignatureBuilder method = unionBuilder.addMethod("getDefaultInstance");
//            method.setReturnType(returnType);
//            method.addParameter(Types.STRING, "defaultValue");
//            method.setAccessModifier(AccessModifier.PUBLIC);
//            method.setStatic(true);

        } else {
            throw new IllegalStateException("Unhandled type " + typeName);
        }

//        builder.setSchemaPath(typedef.getPath());
        builder.setModuleName(moduleName);
//        addCodegenInformation(builder, typedef);

        annotateDeprecatedIfNecessary(typedef, builder);

        if (javaType instanceof ConcreteType && "String".equals(javaType.getName()) && typedef.getBaseType() != null) {
            addStringRegExAsConstant(builder, resolveRegExpressions(typedef));
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

        final TypeDefinition<?> typedef = statement().getTypeDefinition();
        addStringRegExAsConstant(builder, resolveRegExpressions(typedef));

        // FIXME: anything else?

        return builder.build();
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // typedefs are a separate concept
    }

    @Override
    Type methodReturnType(final TypeBuilderFactory builderFactory, final TypedefGenerator baseGen,
            final TypeReference refType) {
        // we should never reach here
        throw new UnsupportedOperationException();
    }
}
