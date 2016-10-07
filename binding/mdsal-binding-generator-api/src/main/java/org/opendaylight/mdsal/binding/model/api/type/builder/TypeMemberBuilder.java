/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api.type.builder;

import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.Type;

public interface TypeMemberBuilder<T extends TypeMemberBuilder<T>> {

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
    AnnotationTypeBuilder addAnnotation(String packageName, String name);

    /**
     * Returns the name of property.
     *
     * @return the name of property.
     */
    String getName();

    /**
     * Adds return Type into Builder definition for Generated Property. <br>
     * The return Type MUST NOT be <code>null</code>, otherwise the method
     * SHOULD throw {@link IllegalArgumentException}
     *
     * @param returnType
     *            Return Type of property.
     */
    T setReturnType(Type returnType);

    AccessModifier getAccessModifier();

    /**
     * Sets the access modifier of property.
     *
     * @param modifier
     *            Access Modifier value.
     */
    T setAccessModifier(AccessModifier modifier);

    /**
     * Adds String definition of comment into Method Signature definition. <br>
     * The comment String MUST NOT contain anny comment specific chars (i.e.
     * "/**" or "//") just plain String text description.
     *
     * @param comment
     *            Comment String.
     */
    T setComment(String comment);

    /**
     * Sets the flag final for method signature. If this is set the method will
     * be prohibited from overriding. <br>
     * This setting is irrelevant for methods designated to be defined in
     * interface definitions because interface can't have final method.
     *
     * @param isFinal
     *            Is Final
     */
    T setFinal(boolean isFinal);

    T setStatic(boolean isStatic);
}
