/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.spi;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.model.api.Restrictions;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Service provider interface that defines contract for generated types.
 */
@Beta
public interface TypeProvider {

    /**
     * Resolve of yang Type Definition to its java counter part.
     * If the Type Definition contains one of yang primitive types the method
     * will return java.lang. counterpart. (For example if yang type is int32
     * the java counterpart is java.lang.Integer). In case that Type
     * Definition contains extended type defined via yang typedef statement
     * the method SHOULD return Generated Type or Generated Transfer Object
     * if that Type is correctly referenced to resolved imported yang module.
     * The method will return <code>null</code> value in situations that
     * TypeDefinition can't be resolved (either due missing yang import or
     * incorrectly specified type).
     *
     *
     * @param type Type Definition to resolve from
     * @param parentNode parent node
     * @return Resolved Type
     */
    Type javaTypeForSchemaDefinitionType(TypeDefinition<?> type, SchemaNode parentNode, ModuleContext context);

    /**
     *
     * @param type Type Definition to resolve from
     * @param parentNode parent node
     * @param restrictions restrictions applied to given type definition
     * @return Resolved Type
     */
    Type javaTypeForSchemaDefinitionType(TypeDefinition<?> type, SchemaNode parentNode, Restrictions restrictions,
            ModuleContext context);

    /**
     * Returns string containing code for creation of new type instance.
     *
     * @param node Schema node to resolve from
     * @return String representing default construction
     */
    String getTypeDefaultConstruction(LeafSchemaNode node);

    /**
     *
     * @param node Schema node to resolve from
     * @return String representing constructor property name
     */
    String getConstructorPropertyName(SchemaNode node);

    /**
     *
     * @param type Type Definition to resolve from
     * @return String representing parameter name
     */
    String getParamNameFromType(TypeDefinition<?> type);

}
