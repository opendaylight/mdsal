/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.model.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The Annotation Type interface is designed to hold information about
 * annotation for any type that could be annotated in Java. <br>
 * For sake of simplicity the Annotation Type is not designed to model exact
 * behaviour of annotation mechanism, but just to hold information needed to
 * model annotation over java Type definition.
 * By using in collections, implementations are expected
 * to implement {@link #hashCode()} and {@link #equals(Object)} methods.
 *
 */
@Beta
public interface AnnotationType extends Type, Comparable<AnnotationType> {

    /**
     * Returns the List of Annotations. <br>
     * Each Annotation Type MAY have defined multiple Annotations.
     *
     * @return the List of Annotations.
     */
    List<AnnotationType> getAnnotations();

    /**
     * Returns Parameter Definition assigned for given parameter name. <br>
     * If Annotation does not contain parameter with specified param name, the
     * method MAY return <code>null</code> value.
     *
     * @param paramName
     *            Parameter Name
     * @return Parameter Definition assigned for given parameter name.
     */
    Parameter getParameter(final String paramName);

    /**
     * Returns List of all parameters assigned to Annotation Type.
     *
     * @return List of all parameters assigned to Annotation Type.
     */
    List<Parameter> getParameters();

    /**
     * Returns List of parameter names.
     *
     * @return List of parameter names.
     */
    List<String> getParameterNames();

    /**
     * Returns <code>true</code> if annotation contains parameters.
     *
     * @return <code>true</code> if annotation contains parameters.
     */
    default boolean containsParameters() {
        return !getParameters().isEmpty();
    }

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    int compareTo(AnnotationType o);

    /**
     * Annotation Type parameter interface. For simplicity the Parameter
     * contains values and value types as Strings. Every annotation which
     * contains parameters could contain either single parameter or array of
     * parameters. To model this purposes the by contract if the parameter
     * contains single parameter the {@link #getValues()} method will return
     * empty List and {@link #getSingleValue()} MUST always return non-
     * <code>null</code> parameter. If the Parameter holds List of values the
     * singular {@link #getSingleValue()} parameter MAY return <code>null</code>
     * value.
     */

    @Beta
    interface Parameter {

        /**
         * Returns the Name of the parameter.
         *
         * @return the Name of the parameter.
         */
        String getName();

        /**
         * Returns value in String format if Parameter contains singular value,
         * otherwise should return first value only. Implementation should throw
         * exception if there is no value to return.
         *
         * @return value in String format.
         * @throws NoSuchElementException If such value not found
         */
        String getSingleValue();

        /**
         * Returns List of Parameter assigned values in order in which they were
         * assigned for given parameter name. <br>
         * If there are multiple values assigned for given parameter name the
         * method MUST NOT return empty List.
         * As we consider getSingleValue() as a primary method, default
         * implementation of getValues() is provided here.
         *
         * @return List of Parameter assigned values in order in which they were
         *         assigned for given parameter name.
         */
        default List<String> getValues() {
            return ImmutableList.of(getSingleValue());
        }
    }
}
