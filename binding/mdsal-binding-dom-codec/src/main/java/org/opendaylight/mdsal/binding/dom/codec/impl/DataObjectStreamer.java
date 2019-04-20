/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableClassToInstanceMap;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.dom.codec.impl.NodeCodecContext.CodecContextFactory;
import org.opendaylight.mdsal.binding.dom.codec.util.AugmentationReader;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base superclass for all concrete streamers, that is objects which are able to turn a concrete DataObject into a
 * stream of events.
 *
 * @param <T> DataObject type
 */
@Beta
public abstract class DataObjectStreamer<T extends DataObject> {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectStreamer.class);

    /**
     * Writes stream events representing object to supplied stream.
     *
     * @param registry Streamer registry to use
     * @param obj Source of stream events
     * @param stream Stream to which events should be written.
     * @throws IOException when the stream throws an IOException
     */
    public abstract void streamDataObject(CodecContextFactory registry, T obj,
            DataObjectEventStreamWriter stream) throws IOException;

    protected static final void streamAnyxml(final DataObjectEventStreamWriter writer, final String localName,
            final Object value) throws IOException {
        if (value != null) {
            writer.anyxmlNode(localName, value);
        }
    }

    protected static final void streamAugmentations(final CodecContextFactory registry,
            final DataObjectEventStreamWriter writer, final Augmentable<?> obj) throws IOException {
        final Map<Class<? extends Augmentation<?>>, Augmentation<?>> augmentations;
        if (registry instanceof AugmentationReader) {
            augmentations = ((AugmentationReader) registry).getAugmentations(obj);
        } else if (Proxy.isProxyClass(obj.getClass())) {
            augmentations = getFromProxy(obj);
        } else {
            augmentations = BindingReflections.getAugmentations(obj);
        }
        for (final Entry<Class<? extends Augmentation<?>>, Augmentation<?>> aug : augmentations.entrySet()) {
            emitAugmentation(aug.getKey(), aug.getValue(), writer, registry);
        }
    }

    private static Map<Class<? extends Augmentation<?>>, Augmentation<?>> getFromProxy(final Augmentable<?> obj) {
        final InvocationHandler proxy = Proxy.getInvocationHandler(obj);
        if (proxy instanceof AugmentationReader) {
            return ((AugmentationReader) proxy).getAugmentations(obj);
        }
        return ImmutableClassToInstanceMap.of();
    }

    protected static final <C extends DataObject> void streamChoice(final Class<C> choiceClass,
            final CodecContextFactory registry, final DataObjectEventStreamWriter writer, final C value)
                    throws IOException {
        if (value != null) {
            final Class<? extends DataObject> caseClass = value.implementedInterface();
            writer.startChoiceNode(choiceClass, BindingStreamEventWriter.UNKNOWN_SIZE);
            final DataObjectStreamer caseStreamer = registry.getDataObjectStreamer(caseClass);
            if (caseStreamer != null) {
                // FIXME: integrate cache
                caseStreamer.streamDataObject(registry, value, writer);
            } else {
                LOG.warn("No serializer for case {} is available in registry {}", caseClass, registry);
            }

            writer.endNode();
        }
    }

    protected static final <C extends DataObject> void streamContainer(final DataObjectStreamer<C> childStreamer,
            final CodecContextFactory registry, final DataObjectEventStreamWriter writer, final C value)
                    throws IOException {
        if (value != null) {
            // FIXME: integrate cache
            childStreamer.streamDataObject(registry, value, writer);
        }
    }

    protected static final void streamLeaf(final DataObjectEventStreamWriter writer, final String localName,
            final Object value) throws IOException {
        if (value != null) {
            writer.leafNode(localName, value);
        }
    }

    protected static final void streamLeafList(final DataObjectEventStreamWriter writer, final String localName,
            final List<?> value) throws IOException {
        if (value != null) {
            writer.startLeafSet(localName, value.size());
            for (Object entry : value) {
                writer.leafSetEntryNode(entry);
            }
        }
    }

    protected static final void streamOrderedLeafList(final DataObjectEventStreamWriter writer,
            final String localName, final List<?> value) throws IOException {
        if (value != null) {
            writer.startOrderedLeafSet(localName, value.size());
            for (Object entry : value) {
                writer.leafSetEntryNode(entry);
            }
        }
    }

    protected static final <E extends DataObject> void streamList(final Class<E> childClass,
            final DataObjectStreamer<E> childStreamer, final CodecContextFactory registry,
            final DataObjectEventStreamWriter writer, final List<? extends E> value) throws IOException {
        if (value != null) {
            writer.startUnkeyedList(childClass, value.size());
            commonStreamList(registry, writer, childStreamer, value);
        }
    }

    protected static final <E extends DataObject & Identifiable<?>> void streamMap(final Class<E> childClass,
            final DataObjectStreamer<E> childStreamer, final CodecContextFactory registry,
            final DataObjectEventStreamWriter writer, final List<? extends E> value) throws IOException {
        if (value != null) {
            writer.startMapNode(childClass, value.size());
            commonStreamList(registry, writer, childStreamer, value);
        }
    }

    protected static final <E extends DataObject & Identifiable<?>> void streamOrderedMap(final Class<E> childClass,
            final DataObjectStreamer<E> childStreamer, final CodecContextFactory registry,
            final DataObjectEventStreamWriter writer, final List<? extends E> value) throws IOException {
        if (value != null) {
            writer.startOrderedMapNode(childClass, value.size());
            commonStreamList(registry, writer, childStreamer, value);
        }
    }

    private static <E extends DataObject> void commonStreamList(final CodecContextFactory registry,
            final DataObjectEventStreamWriter writer, final DataObjectStreamer<E> childStreamer,
            final Collection<? extends E> value) throws IOException {
        for (E entry : value) {
            // FIXME: integrate cache
            childStreamer.streamDataObject(registry, entry, writer);
        }
        writer.endNode();
    }

    @SuppressWarnings("rawtypes")
    private static void emitAugmentation(final Class type, final Augmentation<?> value,
            final DataObjectEventStreamWriter writer, final CodecContextFactory registry) throws IOException {
        /*
         * Binding Specification allowed to insert augmentation with null for
         * value, which effectively could be used to remove augmentation
         * from builder / DTO.
         */
        if (value != null) {
            checkArgument(value instanceof DataObject);
            @SuppressWarnings("unchecked")
            final DataObjectStreamer serializer = registry.getDataObjectStreamer(type);
            if (serializer != null) {
                serializer.streamDataObject(registry, (DataObject) value, writer);
            } else {
                LOG.warn("DataObjectSerializer is not present for {} in registry {}", type, registry);
            }
        }
    }
}
