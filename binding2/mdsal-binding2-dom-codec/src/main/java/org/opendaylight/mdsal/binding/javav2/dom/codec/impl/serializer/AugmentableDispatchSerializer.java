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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.AugmentationReader;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializer;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerImplementation;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerRegistry;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatch serializer, which emits
 * {@link BindingStreamEventWriter#startAugmentationNode(Class)} events for
 * supplied augmentation node.
 */
@Beta
public class AugmentableDispatchSerializer implements TreeNodeSerializerImplementation {

    private static final Logger LOG = LoggerFactory.getLogger(AugmentableDispatchSerializer.class);

    @Override
    public void serialize(final TreeNodeSerializerRegistry reg, final TreeNode obj,
            final BindingStreamEventWriter stream) throws IOException {
        if (obj instanceof Augmentable<?>) {
            final Map<Class<? extends Augmentation<?>>, Augmentation<?>> augmentations;
            if (reg instanceof AugmentationReader) {
                augmentations = ((AugmentationReader) reg).getAugmentations(obj);
            } else if (Proxy.isProxyClass(obj.getClass())) {
                augmentations = getFromProxy(obj);
            } else {
                augmentations = BindingReflections.getAugmentations((Augmentable<?>) obj);
            }
            for (final Entry<Class<? extends Augmentation<?>>, Augmentation<?>> aug : augmentations.entrySet()) {
                emitAugmentation(aug.getKey(), aug.getValue(), stream, reg);
            }
        }
    }

    private Map<Class<? extends Augmentation<?>>, Augmentation<?>> getFromProxy(final TreeNode obj) {
        final InvocationHandler proxy = Proxy.getInvocationHandler(obj);
        if (proxy instanceof AugmentationReader) {
            return ((AugmentationReader) proxy).getAugmentations(obj);
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("rawtypes")
    private void emitAugmentation(final Class type, final Augmentation<?> value, final BindingStreamEventWriter stream,
            final TreeNodeSerializerRegistry registry) throws IOException {
        Preconditions.checkArgument(value instanceof TreeNode);
        @SuppressWarnings("unchecked")
        final TreeNodeSerializer serializer = registry.getSerializer(type);
        if (serializer != null) {
            serializer.serialize((TreeNode) value, stream);
        } else {
            LOG.warn("TreeNodeSerializer is not present for {} in registry {}", type, registry);
        }
    }
}