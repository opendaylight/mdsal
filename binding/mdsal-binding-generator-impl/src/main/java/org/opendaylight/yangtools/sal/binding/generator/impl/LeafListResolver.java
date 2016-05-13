/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;

import java.util.Map;
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

public class LeafListResolver extends LeafParentResolver {
    private TypeProvider typeProvider;
    private SchemaContext schemaContext;
    private Map<Module, ModuleContext> genCtx;

    public LeafListResolver(SchemaContext schemaContext, TypeProvider typeProvider, Map<Module, ModuleContext> genCtx) {
        this.typeProvider = typeProvider;
        this.schemaContext = schemaContext;
        this.genCtx = genCtx;
    }

    /**
     * Converts <code>node</code> leaf list schema node to getter method of
     * <code>typeBuilder</code>.
     *
     * @param typeBuilder
     *            generated type builder to which is <code>node</code> added as
     *            getter method
     * @param node
     *            leaf list schema node which is added to
     *            <code>typeBuilder</code> as getter method
     * @param module
     * @return boolean value
     *         <ul>
     *         <li>true - if <code>node</code>, <code>typeBuilder</code>,
     *         nodeName equal null or <code>node</code> is added by <i>uses</i></li>
     *         <li>false - other cases</li>
     *         </ul>
     */
    public boolean resolveLeafListSchemaNode(final GeneratedTypeBuilder typeBuilder, final LeafListSchemaNode node, final
    Module module) {
        if (node == null || typeBuilder == null || node.isAddedByUses()) {
            return false;
        }

        final QName nodeName = node.getQName();
        if (nodeName == null) {
            return false;
        }

        final TypeDefinition<?> typeDef = node.getType();
        final Module parentModule = findParentModule(schemaContext, node);

        Type returnType = null;
        if (typeDef.getBaseType() == null) {
            if (typeDef instanceof EnumTypeDefinition) {
                final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typeDef;
                final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, nodeName,
                        typeBuilder, module, genCtx);
                returnType = new ReferencedTypeImpl(enumBuilder.getPackageName(), enumBuilder.getName());
                ((TypeProviderImpl) typeProvider).putReferencedType(node.getPath(), returnType);
            } else if (typeDef instanceof UnionTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, node, parentModule, typeProvider);
                if (genTOBuilder != null) {
                    returnType = createReturnTypeForUnion(genTOBuilder, typeDef, typeBuilder, parentModule, typeProvider);
                }
            } else if (typeDef instanceof BitsTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, node, parentModule, typeProvider);
                returnType = genTOBuilder.toInstance();
            } else {
                final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node, restrictions);
            }
        } else {
            final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
            returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node, restrictions);
        }

        final ParameterizedType listType = Types.listTypeFor(returnType);
        BindingTextUtils.constructGetter(typeBuilder, nodeName.getLocalName(), node.getDescription(), listType, node
                .getStatus());
        return true;
    }

}