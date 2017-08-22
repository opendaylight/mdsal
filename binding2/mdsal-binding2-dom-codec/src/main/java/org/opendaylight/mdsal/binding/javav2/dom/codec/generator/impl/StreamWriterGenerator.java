/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.api.TreeNodeSerializerGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.generator.AbstractStreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.source.AbstractAugmentableDataNodeContainerEmitterSource;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.source.AbstractDataNodeContainerSerializerSource;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.source.AbstractTreeNodeSerializerSource;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer.AugmentableDispatchSerializer;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer.ChoiceDispatchSerializer;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.binding.javav2.spec.base.IdentifiableItem;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerImplementation;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;

/**
 * Concrete implementation of {@link AbstractStreamWriterGenerator} which in
 * runtime generates classes implementing
 * {@link TreeNodeSerializerImplementation} interface and are used to serialize
 * Binding {@link TreeNode}.
 *
 * Actual implementation of codecs is done via static methods, which allows for
 * static wiring of codecs. Choice codec and Augmentable codecs are static
 * properties of parent codec and stateless implementations are used (
 * {@link ChoiceDispatchSerializer}, {@link AugmentableDispatchSerializer},
 * which uses registry to dispatch to concrete item codec.
 *
 */
@Beta
public class StreamWriterGenerator extends AbstractStreamWriterGenerator {

    private static final String UNKNOWN_SIZE = BindingStreamEventWriter.class.getName() + ".UNKNOWN_SIZE";

    private StreamWriterGenerator(final JavassistUtils utils, final Void ignore) {
        super(utils);
    }

    /**
     * Create a new instance backed by a specific {@link JavassistUtils}
     * instance.
     *
     * @param utils
     *            JavassistUtils instance to use
     * @return A new generator
     */
    public static TreeNodeSerializerGenerator create(final JavassistUtils utils) {
        return new StreamWriterGenerator(utils, null);
    }

    @Override
    protected AbstractTreeNodeSerializerSource generateContainerSerializer(final GeneratedType type,
            final ContainerSchemaNode node) {

        return new AbstractAugmentableDataNodeContainerEmitterSource(this, type, node) {
            @Override
            public CharSequence emitStartEvent() {
                return startContainerNode(classReference(type), UNKNOWN_SIZE);
            }
        };
    }

    @Override
    protected AbstractTreeNodeSerializerSource generateNotificationSerializer(final GeneratedType type,
            final NotificationDefinition node) {

        return new AbstractAugmentableDataNodeContainerEmitterSource(this, type, node) {
            @Override
            public CharSequence emitStartEvent() {
                return startContainerNode(classReference(type), UNKNOWN_SIZE);
            }
        };
    }

    @Override
    protected AbstractTreeNodeSerializerSource generateCaseSerializer(final GeneratedType type,
            final ChoiceCaseNode node) {
        return new AbstractAugmentableDataNodeContainerEmitterSource(this, type, node) {
            @Override
            public CharSequence emitStartEvent() {
                return startCaseNode(classReference(type), UNKNOWN_SIZE);
            }
        };
    }

    @Override
    protected AbstractTreeNodeSerializerSource generateUnkeyedListEntrySerializer(final GeneratedType type,
            final ListSchemaNode node) {
        return new AbstractAugmentableDataNodeContainerEmitterSource(this, type, node) {

            @Override
            public CharSequence emitStartEvent() {
                return startUnkeyedListItem(UNKNOWN_SIZE);
            }
        };
    }

    @Override
    protected AbstractTreeNodeSerializerSource generateSerializer(final GeneratedType type,
            final AugmentationSchema schema) {
        return new AbstractDataNodeContainerSerializerSource(this, type, schema) {

            @Override
            public CharSequence emitStartEvent() {
                return startAugmentationNode(classReference(type));
            }
        };
    }

    @Override
    protected AbstractTreeNodeSerializerSource generateMapEntrySerializer(final GeneratedType type,
            final ListSchemaNode node) {
        return new AbstractAugmentableDataNodeContainerEmitterSource(this, type, node) {
            @Override
            public CharSequence emitStartEvent() {
                StringBuilder sb = new StringBuilder()
                        .append('(')
                        .append(IdentifiableItem.class.getName())
                        .append(") ")
                        .append(invoke(INPUT, "treeIdentifier"));
                return startMapEntryNode(sb.toString(), UNKNOWN_SIZE);
            }
        };
    }
}
