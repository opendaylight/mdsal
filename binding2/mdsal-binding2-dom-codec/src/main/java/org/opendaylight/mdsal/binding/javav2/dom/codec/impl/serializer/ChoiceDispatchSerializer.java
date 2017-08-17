/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.io.IOException;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializer;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerImplementation;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatch serializer, which emit DOM data from Binding v2 via stream writer.
 */
@Beta
public class ChoiceDispatchSerializer implements TreeNodeSerializerImplementation {

    private static final Logger LOG = LoggerFactory.getLogger(ChoiceDispatchSerializer.class);

    @SuppressWarnings("rawtypes")
    private final Class choiceClass;

    @SuppressWarnings("rawtypes")
    private ChoiceDispatchSerializer(final Class choiceClass) {
        this.choiceClass = Preconditions.checkNotNull(choiceClass);
    }

    /**
     * Prepare instance of serializer from choice class.
     *
     * @param choiceClass
     *            - class choice
     * @return instance of serializer
     */
    public static final ChoiceDispatchSerializer from(final Class<? extends Instantiable<?>> choiceClass) {
        return new ChoiceDispatchSerializer(choiceClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(final TreeNodeSerializerRegistry reg, final TreeNode obj,
            final BindingStreamEventWriter stream) throws IOException {
        @SuppressWarnings("rawtypes")
        final Class cazeClass = ((Instantiable) obj).implementedInterface();
        stream.startChoiceNode(new Item<>(choiceClass), BindingStreamEventWriter.UNKNOWN_SIZE);
        final TreeNodeSerializer caseSerializer = reg.getSerializer(cazeClass);
        if (caseSerializer != null) {
            caseSerializer.serialize(obj, stream);
        } else {
            LOG.warn("No serializer for case {} is available in registry {}", cazeClass, reg);
        }
        stream.endNode();
    }
}