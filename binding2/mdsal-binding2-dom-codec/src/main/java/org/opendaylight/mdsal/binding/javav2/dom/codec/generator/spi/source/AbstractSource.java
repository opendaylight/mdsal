/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.source;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StaticBindingProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;

/**
 * Base class for preparing types and constants for writer.
 *
 */
@Beta
abstract class AbstractSource {

    private final Set<StaticBindingProperty> staticConstants = new HashSet<>();

    /**
     * Create new static constant of specific type with value.
     *
     * @param name
     *            - name of constant
     * @param type
     *            - specific type of constant
     * @param value
     *            - value of constant
     * @param <T>
     *            - type of constant
     */
    final <T> void staticConstant(final String name, final Class<T> type, final T value) {
        this.staticConstants.add(new StaticBindingProperty(name, type, value));
    }

    /**
     * Get set of static constants.
     *
     * @return unmodifiable view of set of static constants
     */
    public final Set<StaticBindingProperty> getStaticConstants() {
        return Collections.unmodifiableSet(this.staticConstants);
    }

    /**
     * Prepare common part of invoke of method on object.
     *
     * @param object
     *            - object for invoke method
     * @param methodName
     *            - method name to be invoked
     * @return base part of invoking method on object as String
     */
    private static StringBuilder prepareCommonInvokePart(final CharSequence object, final String methodName) {
        final StringBuilder sb = new StringBuilder();
        if (object != null) {
            sb.append(object);
            sb.append('.');
        }
        return sb.append(methodName).append('(');
    }

    /**
     * Prepare invoking method on object with an argument.
     *
     * @param object
     *            - object for invoke method
     * @param methodName
     *            - method name to be invoked
     * @param arg
     *            - argument of method
     * @return invoking method on object with an argument as String
     */
    static final CharSequence invoke(final CharSequence object, final String methodName, final Object arg) {
        return prepareCommonInvokePart(object, methodName).append(arg).append(')');
    }

    /**
     * Prepare invoking method on object with more arguments.
     *
     * @param object
     *            - object for invoke method
     * @param methodName
     *            - method name to be invoked
     * @param args
     *            - arguments of method
     * @return invoking method on object with more arguments as String
     */
    protected static final CharSequence invoke(final CharSequence object, final String methodName,
            final Object... args) {
        final StringBuilder sb = prepareCommonInvokePart(object, methodName);

        final UnmodifiableIterator<Object> iterator = Iterators.forArray(args);
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(',');
            }
        }
        return sb.append(')');
    }

    /**
     * Assign of value to variable.
     *
     * @param var
     *            - name of variable
     * @param value
     *            - value of variable
     * @return assigned value to variable as char sequence
     */
    static final CharSequence assign(final String var, final CharSequence value) {
        return assign((String) null, var, value);
    }

    /**
     * Assign of value to variable of specific type.
     *
     * @param type
     *            - specific type of value
     * @param var
     *            - name of variable
     * @param value
     *            - value of variable
     * @return assigned value to variable of specific type as char sequence, if
     *         type is null then there is not added type to final string of
     *         assigned value
     */
    static final CharSequence assign(final String type, final String var, final CharSequence value) {
        final StringBuilder sb = new StringBuilder();
        if (type != null) {
            sb.append(type);
            sb.append(' ');
        }
        return sb.append(var).append(" = ").append(value);
    }

    /**
     * Assign of value to variable of specific type.
     *
     * @param type
     *            - specific type of value
     * @param var
     *            - name of variable
     * @param value
     *            - value of variable
     * @return assigned value to variable of specific type as char sequence, if
     *         type is null then there is not added type to final string of
     *         assigned value
     */
    static final CharSequence assign(final Type type, final String var, final CharSequence value) {
        return assign(type.getFullyQualifiedName(), var, value);
    }

    /**
     * Cast value to specific type.
     *
     * @param type
     *            - specific type
     * @param value
     *            - value for cast
     * @return casted value to specifc type as char sequence
     */
    static final CharSequence cast(final Type type, final CharSequence value) {
        return cast(type.getFullyQualifiedName(), value);
    }

    /**
     * Cast value to specific type.
     *
     * @param type
     *            - specific type
     * @param value
     *            - value for cast
     * @return casted value to specifc type as char sequence
     */
    static final CharSequence cast(final String type, final CharSequence value) {
        return "((" + type + ") " + value + ')';
    }

    /**
     * Create loop through iterable object with specific body.
     *
     * @param iterable
     *            - iterable object
     * @param iteratorName
     *            - name of iterator variable of iterable object
     * @param valueType
     *            - type of iterable item
     * @param valueName
     *            - name of variable of iterable item
     * @param body
     *            - specific body for porcess of iterable item
     * @return loop through iterable object with specific body as String
     */
    static final CharSequence forEach(final String iterable, final String iteratorName,
            final String valueType, final String valueName, final CharSequence body) {
        final StringBuilder sb = new StringBuilder();
        sb.append(statement(assign(java.util.Iterator.class.getName(), iteratorName, invoke(iterable, "iterator"))));
        sb.append("while (").append(invoke(iteratorName, "hasNext")).append(") {\n");
        sb.append(statement(assign(valueType, valueName, cast(valueType, invoke(iteratorName, "next")))));
        sb.append(body);
        return sb.append("\n}\n");
    }

    /**
     * Create new Java statement.
     *
     * @param statement
     *            - input for creating new Java statement
     * @return java statement
     */
    static final CharSequence statement(final CharSequence statement) {
        return new StringBuilder(statement).append(";\n");
    }
}
