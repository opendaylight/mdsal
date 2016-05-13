/*
 *
 *  * Copyright (c) 2016. Cisco Systems, Inc. and others.  All rights reserved.
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;

import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

public class IdentityResolver extends LeafParentResolver {
    private SchemaContext schemaContext;
    private Map<Module, ModuleContext> genCtx;

    public IdentityResolver(SchemaContext schemaContext, Map<Module, ModuleContext> genCtx) {
        this.schemaContext = schemaContext;
        this.genCtx = genCtx;
    }

    /**
     * Converts all <b>identities</b> of the module to the list of
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained set of all identity objects to
     *            iterate over them
     * @param context
     *            schema context only used as input parameter for method
     *
     */
    protected void allIdentitiesToGenTypes(final Module module, final SchemaContext context, final boolean verboseClassComments) {
        final Set<IdentitySchemaNode> schemaIdentities = module.getIdentities();
        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());

        if (schemaIdentities != null && !schemaIdentities.isEmpty()) {
            for (final IdentitySchemaNode identity : schemaIdentities) {
                identityToGenType(module, basePackageName, identity, context, verboseClassComments);
            }
        }
    }


    /**
     * Converts the <b>identity</b> object to GeneratedType. Firstly it is
     * created transport object builder. If identity contains base identity then
     * reference to base identity is added to superior identity as its extend.
     * If identity doesn't contain base identity then only reference to abstract
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string contains the module package name
     * @param identity
     *            IdentitySchemaNode which contains data about identity
     * @param context
     *            SchemaContext which is used to get package and name
     *            information about base of identity
     *
     */
    private void identityToGenType(final Module module, final String basePackageName,
        final IdentitySchemaNode identity, final SchemaContext context, final boolean verboseClassComments) {
        if (identity == null) {
            return;
        }
        final String packageName = packageNameForGeneratedType(basePackageName, identity.getPath());
        final String genTypeName = BindingMapping.getClassName(identity.getQName());
        final GeneratedTOBuilderImpl newType = new GeneratedTOBuilderImpl(packageName, genTypeName);
        final IdentitySchemaNode baseIdentity = identity.getBaseIdentity();
        if (baseIdentity == null) {
            final GeneratedTOBuilderImpl gto = new GeneratedTOBuilderImpl(BaseIdentity.class.getPackage().getName(),
                    BaseIdentity.class.getSimpleName());
            newType.setExtendsType(gto.toInstance());
        } else {
            final Module baseIdentityParentModule = SchemaContextUtil.findParentModule(context, baseIdentity);
            final String returnTypePkgName = BindingMapping.getRootPackageName(baseIdentityParentModule
                    .getQNameModule());
            final String returnTypeName = BindingMapping.getClassName(baseIdentity.getQName());
            final GeneratedTransferObject gto = new GeneratedTOBuilderImpl(returnTypePkgName, returnTypeName)
                    .toInstance();
            newType.setExtendsType(gto);
        }
        newType.setAbstract(true);
        newType.addComment(identity.getDescription());
        final String parentName = BindingTextUtils.getParentName(schemaContext, identity);
        newType.setDescription(BindingTextUtils.createDescription(identity, newType.getFullyQualifiedName(),
                verboseClassComments, parentName));
        newType.setReference(identity.getReference());
        newType.setModuleName(module.getName());
        newType.setSchemaPath(identity.getPath().getPathFromRoot());

        final QName qname = identity.getQName();
        BindingTextUtils.qnameConstant(newType, BindingMapping.QNAME_STATIC_FIELD_NAME, qname);

        genCtx.get(module).addIdentityType(identity.getQName(), newType);
    }
}