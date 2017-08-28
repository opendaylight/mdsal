/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.source;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.generator.AbstractGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.generator.AbstractStreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerRegistry;

@Beta
public abstract class AbstractTreeNodeSerializerSource extends AbstractSource {

    private static final ClassLoadingStrategy STRATEGY = GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();

    static final String SERIALIZER = "_serializer";
    static final String STREAM = "_stream";
    static final String ITERATOR = "_iterator";
    static final String CURRENT = "_current";
    static final String REGISTRY = "_registry";

    private final AbstractGenerator generator;

    /**
     * Set up generator.
     *
     * @param generator
     *            -parent generator
     */
    AbstractTreeNodeSerializerSource(final AbstractGenerator generator) {
        this.generator = Preconditions.checkNotNull(generator);
    }

    @SuppressWarnings("unchecked")
    Class<? extends Instantiable<?>> loadClass(final Type childType) {
        try {
            return (Class<? extends Instantiable<?>>) STRATEGY.loadClass(childType);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Could not load referenced class ", e);
        }
    }

    /**
     * Returns body of static serialize method.
     *
     * <ul>
     * <li>{@link TreeNodeSerializerRegistry} - registry of serializers
     * <li>{@link TreeNode} - node to be serialized
     * <li>{@link BindingStreamEventWriter} - writer to which events should be
     * serialized
     * </ul>
     *
     * @return valid javassist code describing static serialization body
     */
    public abstract CharSequence getSerializerBody();

    /**
     * Invoking leafNode method of stream with arguments local name and value.
     *
     * @param localName
     *            - argument for invoking leafNode
     * @param value
     *            - argument for invoking leafNode
     * @return invoking leafNode method as String
     */
    static final CharSequence leafNode(final String localName, final CharSequence value) {
        return invoke(STREAM, "leafNode", escape(localName), value);
    }

    /**
     * Invoking startLeafSet method of stream with arguments local name and
     * expected.
     *
     * @param localName
     *            - argument for invoking startLeafSet
     * @param expected
     *            - argument for invoking startLeafSet
     * @return invoking startLeafSet method as String
     */
    static final CharSequence startLeafSet(final String localName, final CharSequence expected) {
        return invoke(STREAM, "startLeafSet", escape(localName), expected);
    }

    /**
     * Invoking startOrderedLeafSet method of stream with arguments localname
     * and expected.
     *
     * @param localName
     *            - argument for invoking startOrderedLeafSet
     * @param expected
     *            - argument for invoking startOrderedLeafSet
     * @return invoking startOrderedLeafSet method as String
     */
    static final CharSequence startOrderedLeafSet(final String localName, final CharSequence expected) {
        return invoke(STREAM, "startOrderedLeafSet", escape(localName), expected);
    }

    /**
     * Bound local name by quotes.
     *
     * @param localName
     *            - to be bounded
     * @return bounded local name
     */
    static final CharSequence escape(final String localName) {
        return '"' + localName + '"';
    }

    /**
     * Invoking leafSetEntryNode method of stream with argument value.
     *
     * @param value
     *            - argument for invoking leafSetEntryNode
     * @return invoking leafSetEntryNode method as String
     */
    static final CharSequence leafSetEntryNode(final CharSequence value) {
        return invoke(STREAM, "leafSetEntryNode", value);
    }

    /**
     * Invoking startContainerNode method of stream with arguments type and
     * expected.
     *
     * @param type
     *            - argument for invoking startContainerNode
     * @param expected
     *            - argument for invoking startContainerNode
     * @return invoking startContainerNode method as String
     */
    public static final CharSequence startContainerNode(final CharSequence type, final CharSequence expected) {
        return invoke(STREAM, "startContainerNode", type, expected);
    }

    /**
     * Invoking startUnkeyedList method of stream with arguments type and
     * expected.
     *
     * @param type
     *            - argument for invoking startUnkeyedList
     * @param expected
     *            - argument for invoking startUnkeyedList
     * @return invoking startUnkeyedList method as String
     */
    static final CharSequence startUnkeyedList(final CharSequence type, final CharSequence expected) {
        return invoke(STREAM, "startUnkeyedList", type, expected);
    }

    /**
     * Invoking startUnkeyedListItem of stream with argument expected.
     *
     * @param expected
     *            - argument for invoking startUnkeyedListItem
     * @return invoking startUnkeyedListItem method as String
     */
    protected static final CharSequence startUnkeyedListItem(final CharSequence expected) {
        return invoke(STREAM, "startUnkeyedListItem", expected);
    }

    /**
     * Invoking startMapNode method of stream with arguments type and expected.
     *
     * @param type
     *            - argument for invoking startMapNode
     * @param expected
     *            - argument for invoking startMapNode
     * @return invoking startMapNode method as String
     */
    static final CharSequence startMapNode(final CharSequence type, final CharSequence expected) {
        return invoke(STREAM, "startMapNode", type, expected);
    }

    /**
     * Invoking startOrderedMapNode method of stream with arguments type and
     * expected.
     *
     * @param type
     *            - argument for invoking startOrderedMapNode
     * @param expected
     *            - argument for invoking startOrderedMapNode
     * @return invoking startOrderedMapNode method as String
     */
    static final CharSequence startOrderedMapNode(final CharSequence type, final CharSequence expected) {
        return invoke(STREAM, "startOrderedMapNode", type, expected);
    }

    /**
     * Invoking startMapEntryNode method of stream with arguments key and
     * expected.
     *
     * @param key
     *            - argument for invoking startMapEntryNode
     * @param expected
     *            - argument for invoking startMapEntryNode
     * @return invoking startMapEntryNode method as String
     */
    protected static final CharSequence startMapEntryNode(final CharSequence key, final CharSequence expected) {
        return invoke(STREAM, "startMapEntryNode", key, expected);
    }

    /**
     * Invoking startAugmentationNode of stream with argument key.
     *
     * @param key
     *            - argument for invoking startAugmentationNode
     * @return invoking startAugmentationNode method as String
     */
    protected static final CharSequence startAugmentationNode(final CharSequence key) {
        return invoke(STREAM, "startAugmentationNode", key);
    }

    /**
     * Invoking startChoiceNode method of stream with arguments localname and
     * expected.
     *
     * @param localName
     *            - argument for invoking startChoiceNode
     * @param expected
     *            - argument for invoking startChoiceNode
     * @return invoking startChoiceNode method as String
     */
    static final CharSequence startChoiceNode(final CharSequence localName, final CharSequence expected) {
        return invoke(STREAM, "startChoiceNode", localName, expected);
    }

    /**
     * Invoking startCaseNode method of stream with arguments localname and
     * expected.
     *
     * @param localName
     *            - argument for invoking startCaseNode
     * @param expected
     *            - argument for invoking startCaseNode
     * @return invoking startCaseNode method as String
     */
    protected static final CharSequence startCaseNode(final CharSequence localName, final CharSequence expected) {
        return invoke(STREAM, "startCase", localName, expected);
    }

    /**
     * Invoking anyxmlNode method of stream with arguments name and
     * value.
     *
     * @param name
     *            - argument for invoking anyxmlNode
     * @param value
     *            - argument for invoking anyxmlNode
     * @return invoking anyxmlNode method as String
     */
    static final CharSequence anyxmlNode(final String name, final String value)
            throws IllegalArgumentException {
        return invoke(STREAM, "anyxmlNode", escape(name), value);
    }

    /**
     * Invoking anydataNode method of stream with arguments name and
     * value.
     *
     * @param name
     *            - argument for invoking anydataNode
     * @param value
     *            - argument for invoking anydataNode
     * @return invoking anydataNode method as String
     */
    static final CharSequence anydataNode(final String name, final String value)
            throws IllegalArgumentException {
        return invoke(STREAM, "anydataNode", escape(name), name);
    }

    /**
     * Invoking endNode method of stream without any arguments.
     *
     * @return invoking andNode method as String
     */
    static final CharSequence endNode() {
        return invoke(STREAM, "endNode");
    }

    /**
     * Prepare loop through iterable object with specific body.
     *
     * @param iterable
     *            - name of iterable object
     * @param valueType
     *            - type of iterate objects
     * @param body
     *            - specific body of loop
     * @return loop through iterable object as String
     */
    static final CharSequence forEach(final String iterable, final Type valueType, final CharSequence body) {
        return forEach(iterable, ITERATOR, valueType.getFullyQualifiedName(), CURRENT, body);
    }

    /**
     * Returns class reference for type.
     *
     * @param type
     *            - type for referencing class
     * @return referenced class of type
     */
    protected static final CharSequence classReference(final Type type) {
        return type.getFullyQualifiedName() + ".class";
    }

    /**
     * After getting class of childType from class loader, prepare invoking of
     * serialize() method from instance of reached class of childType with
     * arguments {@link #REGISTRY}, name and {@link #STREAM}.
     *
     * @param childType
     *            - type of child for getting class from classloader
     * @param name
     *            - argument for invoking serialize method of instance childType
     *            class
     * @return invoking serialize method with specific arguments as String
     */
    final CharSequence staticInvokeEmitter(final Type childType, final String name) {
        final Class<?> cls;
        try {
            cls = STRATEGY.loadClass(childType);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Failed to invoke emitter", e);
        }

        final String className = this.generator.loadSerializerFor(cls) + ".getInstance()";
        return invoke(className, AbstractStreamWriterGenerator.SERIALIZE_METHOD_NAME, REGISTRY, name, STREAM);
    }
}

