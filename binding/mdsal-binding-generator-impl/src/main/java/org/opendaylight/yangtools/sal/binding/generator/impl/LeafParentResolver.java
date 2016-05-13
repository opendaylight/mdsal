/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.encodeAngleBrackets;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

public class LeafParentResolver {
    /**
     * Adds enumeration builder created from <code>enumTypeDef</code> to
     * <code>typeBuilder</code>.
     *
     * Each <code>enumTypeDef</code> item is added to builder with its name and
     * value.
     *
     * @param enumTypeDef
     *            EnumTypeDefinition contains enum data
     * @param enumName
     *            string contains name which will be assigned to enumeration
     *            builder
     * @param typeBuilder
     *            GeneratedTypeBuilder to which will be enum builder assigned
     * @param module
     *            Module in which type should be generated
     * @return enumeration builder which contains data from
     *         <code>enumTypeDef</code>
     */
    protected EnumBuilder resolveInnerEnumFromTypeDefinition(final EnumTypeDefinition enumTypeDef, final QName enumName,
        final GeneratedTypeBuilder typeBuilder, final Module module, final Map<Module, ModuleContext> genCtx) {
        if ((enumTypeDef != null) && (typeBuilder != null) && (enumTypeDef.getQName() != null)
                && (enumTypeDef.getQName().getLocalName() != null)) {
            final String enumerationName = BindingMapping.getClassName(enumName);
            final EnumBuilder enumBuilder = typeBuilder.addEnumeration(enumerationName);
            final String enumTypedefDescription = encodeAngleBrackets(enumTypeDef.getDescription());
            enumBuilder.setDescription(enumTypedefDescription);
            enumBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDef);
            ModuleContext ctx = genCtx.get(module);
            ctx.addInnerTypedefType(enumTypeDef.getPath(), enumBuilder);
            return enumBuilder;
        }
        return null;
    }

    protected Type createReturnTypeForUnion(final GeneratedTOBuilder genTOBuilder, final TypeDefinition<?> typeDef,
        final GeneratedTypeBuilder typeBuilder, final Module parentModule, final TypeProvider typeProvider) {
        final GeneratedTOBuilderImpl returnType = new GeneratedTOBuilderImpl(genTOBuilder.getPackageName(),
                genTOBuilder.getName());
        final String typedefDescription = encodeAngleBrackets(typeDef.getDescription());

        returnType.setDescription(typedefDescription);
        returnType.setReference(typeDef.getReference());
        returnType.setSchemaPath(typeDef.getPath().getPathFromRoot());
        returnType.setModuleName(parentModule.getName());

        genTOBuilder.setTypedef(true);
        genTOBuilder.setIsUnion(true);
        TypeProviderImpl.addUnitsToGenTO(genTOBuilder, typeDef.getUnits());
        final GeneratedTOBuilder unionBuilder = createUnionBuilder(genTOBuilder,typeBuilder);
        final MethodSignatureBuilder method = unionBuilder.addMethod("getDefaultInstance");
        method.setReturnType(returnType);
        method.addParameter(Types.STRING, "defaultValue");
        method.setAccessModifier(AccessModifier.PUBLIC);
        method.setStatic(true);

        final Set<Type> types = ((TypeProviderImpl) typeProvider).getAdditionalTypes().get(parentModule);
        if (types == null) {
            ((TypeProviderImpl) typeProvider).getAdditionalTypes().put(parentModule,
                    Sets.<Type> newHashSet(unionBuilder.toInstance()));
        } else {
            types.add(unionBuilder.toInstance());
        }
        return returnType.toInstance();
    }

    private static GeneratedTOBuilder createUnionBuilder(final GeneratedTOBuilder genTOBuilder, final GeneratedTypeBuilder typeBuilder) {
        final String outerCls = Types.getOuterClassName(genTOBuilder);
        final StringBuilder name;
        if (outerCls != null) {
            name = new StringBuilder(outerCls);
        } else {
            name = new StringBuilder();
        }
        name.append(genTOBuilder.getName());
        name.append("Builder");
        final GeneratedTOBuilderImpl unionBuilder = new GeneratedTOBuilderImpl(typeBuilder.getPackageName(),name.toString());
        unionBuilder.setIsUnionBuilder(true);
        return unionBuilder;
    }

    /**
     * Builds generated TO builders for <code>typeDef</code> of type
     * {@link org.opendaylight.yangtools.yang.model.util.UnionType UnionType} or
     * {@link org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition
     * BitsTypeDefinition} which are also added to <code>typeBuilder</code> as
     * enclosing transfer object.
     *
     * If more then one generated TO builder is created for enclosing then all
     * of the generated TO builders are added to <code>typeBuilder</code> as
     * enclosing transfer objects.
     *
     * @param typeDef
     *            type definition which can be of type <code>UnionType</code> or
     *            <code>BitsTypeDefinition</code>
     * @param typeBuilder
     *            generated type builder to which is added generated TO created
     *            from <code>typeDef</code>
     * @param leaf
     *            string with name for generated TO builder
     * @return generated TO builder for <code>typeDef</code>
     */
    protected GeneratedTOBuilder addTOToTypeBuilder(final TypeDefinition<?> typeDef,
        final GeneratedTypeBuilder typeBuilder, final DataSchemaNode leaf, final Module parentModule, final
        TypeProvider typeProvider) {
        final String classNameFromLeaf = BindingMapping.getClassName(leaf.getQName());
        final List<GeneratedTOBuilder> genTOBuilders = new ArrayList<>();
        final String packageName = typeBuilder.getFullyQualifiedName();
        if (typeDef instanceof UnionTypeDefinition) {
            final List<GeneratedTOBuilder> types = ((TypeProviderImpl) typeProvider)
                    .provideGeneratedTOBuildersForUnionTypeDef(packageName, ((UnionTypeDefinition) typeDef),
                            classNameFromLeaf, leaf);
            genTOBuilders.addAll(types);
            if (types.isEmpty()) {
                throw new IllegalStateException("No GeneratedTOBuilder objects generated from union " + typeDef);
            }
            GeneratedTOBuilder resultTOBuilder = types.remove(0);
            for (final GeneratedTOBuilder genTOBuilder : types) {
                resultTOBuilder.addEnclosingTransferObject(genTOBuilder);
            }

            final GeneratedPropertyBuilder genPropBuilder = resultTOBuilder.addProperty("value");
            genPropBuilder.setReturnType(Types.CHAR_ARRAY);
            resultTOBuilder.addEqualsIdentity(genPropBuilder);
            resultTOBuilder.addHashIdentity(genPropBuilder);
            resultTOBuilder.addToStringProperty(genPropBuilder);

        } else if (typeDef instanceof BitsTypeDefinition) {
            genTOBuilders.add((((TypeProviderImpl) typeProvider)).provideGeneratedTOBuilderForBitsTypeDefinition(
                    packageName, typeDef, classNameFromLeaf, parentModule.getName()));
        }
        if (!genTOBuilders.isEmpty()) {
            for (final GeneratedTOBuilder genTOBuilder : genTOBuilders) {
                typeBuilder.addEnclosingTransferObject(genTOBuilder);
            }
            return genTOBuilders.get(0);
        }
        return null;
    }
}
