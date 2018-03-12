/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api.type.builder;

import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

/**
 * Enum Builder is interface that contains methods to build and instantiate
 * Enumeration definition.
 *
 * @see Enumeration
 */
public interface EnumBuilder extends Type {

    /**
     * The method creates new AnnotationTypeBuilder containing specified package
     * name an annotation name. <br>
     * Neither the package name or annotation name can contain <code>null</code>
     * references. In case that any of parameters contains <code>null</code> the
     * method SHOULD thrown {@link IllegalArgumentException}
     *
     * @param packageName
     *            Package Name of Annotation Type
     * @param name
     *            Name of Annotation Type
     * @return <code>new</code> instance of Annotation Type Builder.
     */
    AnnotationTypeBuilder addAnnotation(final JavaTypeName identifier);

    /**
     * The method creates new AnnotationTypeBuilder containing specified package
     * name an annotation name. <br>
     * Neither the package name or annotation name can contain <code>null</code>
     * references. In case that any of parameters contains <code>null</code> the
     * method SHOULD thrown {@link IllegalArgumentException}
     *
     * @param packageName
     *            Package Name of Annotation Type
     * @param name
     *            Name of Annotation Type
     * @return <code>new</code> instance of Annotation Type Builder.
     */
    default AnnotationTypeBuilder addAnnotation(final String packageName, final String simpleName) {
        return addAnnotation(JavaTypeName.create(packageName, simpleName));
    }

    /**
     *
     * @param definingType
     * @return
     */
    Enumeration toInstance(final Type definingType);

    /**
     * Updates this builder with data from <code>enumTypeDef</code>.
     * Specifically this data represents list of value-name pairs.
     *
     * @param enumTypeDef
     *            enum type definition as source of enum data for
     *            <code>enumBuilder</code>
     */
    void updateEnumPairsFromEnumTypeDef(final EnumTypeDefinition enumTypeDef);

    /**
     * @param description
     */
    void setDescription(final String description);

}
