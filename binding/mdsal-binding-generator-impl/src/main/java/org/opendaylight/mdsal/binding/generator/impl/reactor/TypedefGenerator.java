/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.model.util.TypeConstants;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
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
    TypeDefinition<?> extractTypeDefinition() {
        return statement().getTypeDefinition();
    }

    @Override
    GeneratedType createSimple(final TypeBuilderFactory builderFactory, final Type javaType) {
        final ModuleGenerator module = currentModule();
        final String moduleName = module.statement().argument().getLocalName();
        final TypeDefinition<?> typedef = statement().getTypeDefinition();
        final GeneratedTOBuilder builder = newGeneratedTOBuilder(builderFactory);

        builder.addImplementsType(BindingTypes.scalarTypeObject(javaType));

        final GeneratedPropertyBuilder genPropBuilder = builder.addProperty(TypeConstants.VALUE_PROP);
        genPropBuilder.setReturnType(javaType);
        builder.addEqualsIdentity(genPropBuilder);
        builder.addHashIdentity(genPropBuilder);
        builder.addToStringProperty(genPropBuilder);

        builder.setRestrictions(BindingGeneratorUtil.getRestrictions(typedef));

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

    @Override
    GeneratedTOBuilder newGeneratedTOBuilder(final TypeBuilderFactory builderFactory) {
        final GeneratedTOBuilder ret = super.newGeneratedTOBuilder(builderFactory);
        ret.setTypedef(true);
        return ret;
    }
}
