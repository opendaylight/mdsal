/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.annotations.RoutingContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import org.opendaylight.yangtools.yang.model.util.type.CompatUtils;

public class LeafResolver extends LeafParentResolver {
    private TypeProvider typeProvider;
    private SchemaContext schemaContext;
    private Map<Module, ModuleContext> genCtx;
    private static final Splitter COLON_SPLITTER = Splitter.on(':');

    public LeafResolver(SchemaContext schemaContext, TypeProvider typeProvider, Map<Module, ModuleContext> genCtx) {
        this.typeProvider = typeProvider;
        this.schemaContext = schemaContext;
        this.genCtx = genCtx;
    }

    /**
     * Converts <code>leaf</code> to the getter method which is added to
     * <code>typeBuilder</code>.
     *
     * @param typeBuilder
     *            generated type builder to which is added getter method as
     *            <code>leaf</code> mapping
     * @param leaf
     *            leaf schema node which is mapped as getter method which is
     *            added to <code>typeBuilder</code>
     * @param module
     *            Module in which type was defined
     * @return boolean value
     *         <ul>
     *         <li>false - if <code>leaf</code> or <code>typeBuilder</code> are
     *         null</li>
     *         <li>true - in other cases</li>
     *         </ul>
     */
    public Type resolveLeafSchemaNodeAsMethod(final GeneratedTypeBuilder typeBuilder, final LeafSchemaNode leaf, final
    Module module) {
        if (leaf == null || typeBuilder == null || leaf.isAddedByUses()) {
            return null;
        }

        final String leafName = leaf.getQName().getLocalName();
        if (leafName == null) {
            return null;
        }

        final Module parentModule = findParentModule(schemaContext, leaf);
        Type returnType = null;

        final TypeDefinition<?> typeDef = CompatUtils.compatLeafType(leaf);
        if (isInnerType(leaf, typeDef)) {
            if (typeDef instanceof EnumTypeDefinition) {
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf);
                final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typeDef;
                final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, leaf.getQName(),
                        typeBuilder, module, genCtx);

                if (enumBuilder != null) {
                    returnType = enumBuilder.toInstance(typeBuilder);
                }
                ((TypeProviderImpl) typeProvider).putReferencedType(leaf.getPath(), returnType);
            } else if (typeDef instanceof UnionTypeDefinition) {
                GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leaf, parentModule, typeProvider);
                if (genTOBuilder != null) {
                    returnType = createReturnTypeForUnion(genTOBuilder, typeDef, typeBuilder, parentModule, typeProvider);
                }
            } else if (typeDef instanceof BitsTypeDefinition) {
                GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leaf, parentModule, typeProvider);
                if (genTOBuilder != null) {
                    returnType = genTOBuilder.toInstance();
                }
            } else {
                // It is constrained version of already declared type (inner declared type exists,
                // onlyfor special cases (Enum, Union, Bits), which were already checked.
                // In order to get proper class we need to look up closest derived type
                // and apply restrictions from leaf type
                final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                returnType = typeProvider.javaTypeForSchemaDefinitionType(getBaseOrDeclaredType(typeDef), leaf,
                        restrictions);
            }
        } else {
            final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
            returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, restrictions);
        }

        if (returnType == null) {
            return null;
        }

        String leafDesc = leaf.getDescription();
        if (leafDesc == null) {
            leafDesc = "";
        }

        final MethodSignatureBuilder getter = BindingTextUtils.constructGetter(typeBuilder, leafName, leafDesc,
                returnType, leaf.getStatus());
        processContextRefExtension(leaf, getter, parentModule);
        return returnType;
    }

    private static boolean isInnerType(final LeafSchemaNode leaf, final TypeDefinition<?> type) {
        // Deal with old parser, clearing out references to typedefs
        if (type instanceof ExtendedType) {
            return false;
        }

        // New parser with encapsulated type
        if (leaf.getPath().equals(type.getPath())) {
            return true;
        }

        // Embedded type definition with new parser. Also takes care of the old parser with bits
        if (leaf.getPath().equals(type.getPath().getParent())) {
            return true;
        }

        // Old parser uses broken Union type, which does not change its schema path
        if (type instanceof UnionType) {
            return true;
        }

        return false;
    }

    private static TypeDefinition<?> getBaseOrDeclaredType(final TypeDefinition<?> typeDef) {
        if (typeDef instanceof ExtendedType) {
            // Legacy behaviour returning ExtendedType is enough
            return typeDef;
        }
        // Returns DerivedType in case of new parser.
        final TypeDefinition<?> baseType = typeDef.getBaseType();
        return (baseType != null && baseType.getBaseType() != null) ? baseType : typeDef;
    }

    private void processContextRefExtension(final LeafSchemaNode leaf, final MethodSignatureBuilder getter,
        final Module module) {
        for (final UnknownSchemaNode node : leaf.getUnknownSchemaNodes()) {
            final QName nodeType = node.getNodeType();
            if ("context-reference".equals(nodeType.getLocalName())) {
                final String nodeParam = node.getNodeParameter();
                IdentitySchemaNode identity = null;
                String basePackageName = null;
                final Iterable<String> splittedElement = COLON_SPLITTER.split(nodeParam);
                final Iterator<String> iterator = splittedElement.iterator();
                final int length = Iterables.size(splittedElement);
                if (length == 1) {
                    identity = findIdentityByName(module.getIdentities(), iterator.next());
                    basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
                } else if (length == 2) {
                    final String prefix = iterator.next();
                    final Module dependentModule = findModuleFromImports(module.getImports(), prefix);
                    if (dependentModule == null) {
                        throw new IllegalArgumentException("Failed to process context-reference: unknown prefix "
                                + prefix);
                    }
                    identity = findIdentityByName(dependentModule.getIdentities(), iterator.next());
                    basePackageName = BindingMapping.getRootPackageName(dependentModule.getQNameModule());
                } else {
                    throw new IllegalArgumentException("Failed to process context-reference: unknown identity "
                            + nodeParam);
                }
                if (identity == null) {
                    throw new IllegalArgumentException("Failed to process context-reference: unknown identity "
                            + nodeParam);
                }

                final Class<RoutingContext> clazz = RoutingContext.class;
                final AnnotationTypeBuilder rc = getter.addAnnotation(clazz.getPackage().getName(),
                        clazz.getSimpleName());
                final String packageName = packageNameForGeneratedType(basePackageName, identity.getPath());
                final String genTypeName = BindingMapping.getClassName(identity.getQName().getLocalName());
                rc.addParameter("value", packageName + "." + genTypeName + ".class");
            }
        }
    }

    private static IdentitySchemaNode findIdentityByName(final Set<IdentitySchemaNode> identities, final String name) {
        for (final IdentitySchemaNode id : identities) {
            if (id.getQName().getLocalName().equals(name)) {
                return id;
            }
        }
        return null;
    }

    private Module findModuleFromImports(final Set<ModuleImport> imports, final String prefix) {
        for (final ModuleImport imp : imports) {
            if (imp.getPrefix().equals(prefix)) {
                return schemaContext.findModuleByName(imp.getModuleName(), imp.getRevision());
            }
        }
        return null;
    }
}