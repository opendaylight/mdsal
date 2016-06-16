/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.yang.types;

import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding2.model.api.Restrictions;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class TypeProviderImpl implements TypeProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TypeProviderImpl.class);

    /**
     * Contains the schema data red from YANG files.
     */
    private final SchemaContext schemaContext;

    /**
     * Map<moduleName, Map<moduleDate, Map<typeName, type>>>
     */
    private final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap;

    private final Map<Module, Set<Type>> additionalTypes;
    /**
     * Creates new instance of class <code>TypeProviderImpl</code>.
     *
     * @param schemaContext
     *            contains the schema data red from YANG files
     * @throws IllegalArgumentException
     *             if <code>schemaContext</code> equal null.
     */
    public TypeProviderImpl(final SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
        this.genTypeDefsContextMap = new HashMap<>();
        this.additionalTypes = new HashMap<>();
    }

    @Override
    public Type javaTypeForSchemaDefinitionType(TypeDefinition<?> type, SchemaNode parentNode) {
        return null;
    }

    @Override
    public Type javaTypeForSchemaDefinitionType(TypeDefinition<?> type, SchemaNode parentNode, Restrictions restrictions) {
        return null;
    }

    @Override
    public String getTypeDefaultConstruction(LeafSchemaNode node) {
        return null;
    }

    @Override
    public String getConstructorPropertyName(SchemaNode node) {
        return null;
    }

    @Override
    public String getParamNameFromType(TypeDefinition<?> type) {
        return null;
    }

    /**
     * Converts <code>typeDefinition</code> to concrete JAVA <code>Type</code>.
     *
     * @param typeDefinition
     *            type definition which should be converted to JAVA
     *            <code>Type</code>
     * @return JAVA <code>Type</code> which represents
     *         <code>typeDefinition</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>typeDefinition</code> equal null</li>
     *             <li>if Q name of <code>typeDefinition</code></li>
     *             <li>if name of <code>typeDefinition</code></li>
     *             </ul>
     */
    public Type generatedTypeForExtendedDefinitionType(final TypeDefinition<?> typeDefinition, final SchemaNode parentNode) {
        Preconditions.checkArgument(typeDefinition != null, "Type Definition cannot be NULL!");
        if (typeDefinition.getQName() == null) {
            throw new IllegalArgumentException(
                    "Type Definition cannot have non specified QName (QName cannot be NULL!)");
        }
        Preconditions.checkArgument(typeDefinition.getQName().getLocalName() != null,
                "Type Definitions Local Name cannot be NULL!");

        final TypeDefinition<?> baseTypeDef = baseTypeDefForExtendedType(typeDefinition);
        if (!(baseTypeDef instanceof LeafrefTypeDefinition) && !(baseTypeDef instanceof IdentityrefTypeDefinition)) {
            final Module module = findParentModule(schemaContext, parentNode);

            if (module != null) {
                final Map<Date, Map<String, Type>> modulesByDate = genTypeDefsContextMap.get(module.getName());
                final Map<String, Type> genTOs = modulesByDate.get(module.getRevision());
                if (genTOs != null) {
                    return genTOs.get(typeDefinition.getQName().getLocalName());
                }
            }
        }
        return null;
    }

    /**
     * Gets base type definition for <code>extendTypeDef</code>. The method is
     * recursively called until non <code>ExtendedType</code> type is found.
     *
     * @param extendTypeDef
     *            type definition for which is the base type definition sought
     * @return type definition which is base type for <code>extendTypeDef</code>
     * @throws IllegalArgumentException
     *             if <code>extendTypeDef</code> equal null
     */
    private static TypeDefinition<?> baseTypeDefForExtendedType(final TypeDefinition<?> extendTypeDef) {
        Preconditions.checkArgument(extendTypeDef != null, "Type Definition reference cannot be NULL!");

        TypeDefinition<?> ret = extendTypeDef;
        while (ret.getBaseType() != null) {
            ret = ret.getBaseType();
        }

        return ret;
    }

    public Map<Module, Set<Type>> getAdditionalTypes() {
        return additionalTypes;
    }

}
