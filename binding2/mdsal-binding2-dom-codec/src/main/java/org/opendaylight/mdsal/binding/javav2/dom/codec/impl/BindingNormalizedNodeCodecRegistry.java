/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.factory.BindingNormalizedNodeWriterFactory;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.factory.BindingTreeCodecFactory;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.serializer.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.api.TreeNodeSerializerGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.NotificationCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.BindingCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.NodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.TreeNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.runtime.context.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.javav2.runtime.context.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializer;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerImplementation;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerRegistry;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializing and deserializing Binding and DOM data
 */
@Beta
public class BindingNormalizedNodeCodecRegistry implements TreeNodeSerializerRegistry, BindingTreeCodecFactory,
        BindingNormalizedNodeWriterFactory, BindingNormalizedNodeSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(BindingNormalizedNodeCodecRegistry.class);

    private final TreeNodeSerializerGenerator generator;
    private final LoadingCache<Class<? extends TreeNode>, TreeNodeSerializer> serializers;
    private volatile BindingCodecContext codecContext;

    /**
     * Prepare generator for generating serializers and create loader for serializers.
     *
     * @param generator
     *            - serializer generator
     */
    public BindingNormalizedNodeCodecRegistry(final TreeNodeSerializerGenerator generator) {
        this.generator = Preconditions.checkNotNull(generator);
        this.serializers = CacheBuilder.newBuilder().weakKeys().build(new GeneratorLoader());
    }

    @Override
    public TreeNodeSerializer getSerializer(final Class<? extends TreeNode> type) {
        return serializers.getUnchecked(type);
    }

    /**
     * Get binding tree codec context.
     *
     * @return codec context
     */
    public BindingTreeCodec getCodecContext() {
        return codecContext;
    }

    /**
     * Create codec context based on runtime context and notify generator that runtime context has been
     * updated.
     *
     * @param context
     *            - runtime context
     */
    public void onBindingRuntimeContextUpdated(final BindingRuntimeContext context) {
        codecContext = create(context);
        generator.onBindingRuntimeContextUpdated(context);
    }

    @Nullable
    @Override
    public YangInstanceIdentifier toYangInstanceIdentifier(@Nonnull final InstanceIdentifier<?> binding) {
        return codecContext.getInstanceIdentifierCodec().serialize(binding);
    }

    @Nullable
    @Override
    public InstanceIdentifier<?> fromYangInstanceIdentifier(@Nonnull final YangInstanceIdentifier dom) {
        return codecContext.getInstanceIdentifierCodec().deserialize(dom);
    }

    @Nullable
    @Override
    public <T extends TreeNode> Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>
            toNormalizedNode(final InstanceIdentifier<T> path, final T data) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        // We create Binding Stream Writer which translates from Binding to Normalized Nodes
        final Entry<YangInstanceIdentifier, BindingStreamEventWriter> writeCtx =
                codecContext.newWriter(path, domWriter);

        // We get serializer which reads binding data and uses Binding To Normalized Node writer to write
        // result
        try {
            getSerializer(path.getTargetType()).serialize(data, writeCtx.getValue());
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing path {} data {}", path, data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return new SimpleEntry<>(writeCtx.getKey(), result.getResult());
    }

    @Nonnull
    @SuppressWarnings("rawtypes")
    @Override
    public ContainerNode toNormalizedNodeNotification(@Nonnull final Notification data) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        @SuppressWarnings("unchecked")
        final Class<? extends TreeNode> type = (Class) data.getClass();
        @SuppressWarnings("unchecked")
        final BindingStreamEventWriter writer = newNotificationWriter((Class) type, domWriter);
        try {
            getSerializer(type).serialize((TreeNode) data, writer);
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing data {}", data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return (ContainerNode) result.getResult();

    }

    @Nonnull
    @SuppressWarnings("unchecked")
    @Override
    public ContainerNode toNormalizedNodeOperationData(@Nonnull final TreeNode data) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        @SuppressWarnings("rawtypes")
        final Class<? extends TreeNode> type = data.getClass();
        final Class<? extends Instantiable<?>> instData = (Class<? extends Instantiable<?>>) data.getClass();
        final BindingStreamEventWriter writer = newOperationWriter(instData, domWriter);
        try {
            getSerializer(type).serialize(data, writer);
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing data {}", data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return (ContainerNode) result.getResult();
    }

    private static boolean isBindingRepresentable(final NormalizedNode<?, ?> data) {
        if (data instanceof ChoiceNode) {
            return false;
        }
        if (data instanceof LeafNode<?>) {
            return false;
        }
        if (data instanceof LeafSetNode) {
            return false;
        }
        if (data instanceof LeafSetEntryNode<?>) {
            return false;
        }
        if (data instanceof MapNode) {
            return false;
        }
        if (data instanceof UnkeyedListNode) {
            return false;
        }

        return true;
    }

    @Nullable
    @Override
    public Entry<InstanceIdentifier<?>, TreeNode> fromNormalizedNode(@Nonnull final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        if (!isBindingRepresentable(data)) {
            return null;
        }

        final List<TreeArgument<?>> builder = new ArrayList<>();
        final NodeCodecContext<?> codec = codecContext.getCodecContextNode(path, builder);
        if (codec == null) {
            if (data != null) {
                LOG.warn("Path {} does not have a binding equivalent, should have been caught earlier ({})", path,
                        data.getClass());
            }
            return null;
        }

        final TreeNode lazyObj = codec.deserialize(data);
        final InstanceIdentifier<?> bindingPath = InstanceIdentifier.create(builder);
        return new SimpleEntry<>(bindingPath, lazyObj);
    }

    @Nullable
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Notification fromNormalizedNodeNotification(@Nonnull final SchemaPath path,
            @Nonnull final ContainerNode data) {
        final NotificationCodecContext<?> codec = codecContext.getNotificationContext(path);
        return codec.deserialize(data);
    }

    @Nullable
    @Override
    public TreeNode fromNormalizedNodeOperationData(@Nonnull final SchemaPath path, @Nonnull final ContainerNode data) {
        final OperationInputCodec<?> codec = codecContext.getOperationInputCodec(path);
        return codec.deserialize(data);
    }

    @Nonnull
    @Override
    public Entry<YangInstanceIdentifier, BindingStreamEventWriter>
            newWriterAndIdentifier(@Nonnull final InstanceIdentifier<?> path,
                    @Nonnull final NormalizedNodeStreamWriter domWriter) {
        return codecContext.newWriter(path, domWriter);
    }

    @Nonnull
    @Override
    public BindingStreamEventWriter newWriter(@Nonnull final InstanceIdentifier<?> path,
            @Nonnull final NormalizedNodeStreamWriter domWriter) {
        return codecContext.newWriterWithoutIdentifier(path, domWriter);
    }

    @Nonnull
    @Override
    public BindingStreamEventWriter newNotificationWriter(@Nonnull final Class<? extends Notification<?>> notification,
            @Nonnull final NormalizedNodeStreamWriter domWriter) {
        return codecContext.newNotificationWriter(notification, domWriter);
    }

    @Nonnull
    @Override
    public BindingStreamEventWriter newOperationWriter(
            @Nonnull final Class<? extends Instantiable<?>> operationInputOrOutput,
            @Nonnull final NormalizedNodeStreamWriter domWriter) {
        return codecContext.newOperationWriter(operationInputOrOutput, domWriter);
    }

    /**
     * Deserialize function based on tree node codec context resolved by binding path.
     *
     * @param path
     *            - binding identifier
     * @return function deserializer of codec context of binding path
     */
    public <T extends TreeNode> Function<Optional<NormalizedNode<?, ?>>, Optional<T>>
            deserializeFunction(final InstanceIdentifier<T> path) {
        final TreeNodeCodecContext<?, ?> ctx =
                (TreeNodeCodecContext<?, ?>) codecContext.getCodecContextNode(path, null);
        return new DeserializeFunction<>(ctx);
    }

    @Override
    public BindingCodecContext create(final BindingRuntimeContext context) {
        return new BindingCodecContext(context, this);
    }

    @Override
    public BindingCodecContext create(final SchemaContext context, final Class<?>... bindingClasses) {
        final ModuleInfoBackedContext strategy = ModuleInfoBackedContext.create();
        for (final Class<?> bindingCls : bindingClasses) {
            try {
                strategy.registerModuleInfo(BindingReflections.getModuleInfo(bindingCls));
            } catch (final Exception e) {
                throw new IllegalStateException(
                        "Could not create BindingRuntimeContext from class " + bindingCls.getName(), e);
            }
        }
        final BindingRuntimeContext runtimeCtx = BindingRuntimeContext.create(strategy, context);
        return create(runtimeCtx);
    }

    private static final class DeserializeFunction<T> implements Function<Optional<NormalizedNode<?, ?>>, Optional<T>> {
        private final TreeNodeCodecContext<?, ?> ctx;

        DeserializeFunction(final TreeNodeCodecContext<?, ?> ctx) {
            this.ctx = ctx;
        }

        @Nullable
        @SuppressWarnings("unchecked")
        @Override
        public Optional<T> apply(@Nullable final Optional<NormalizedNode<?, ?>> input) {
            if (input.isPresent()) {
                return Optional.of((T) ctx.deserialize(input.get()));
            }
            return Optional.absent();
        }
    }

    private final class GeneratorLoader extends CacheLoader<Class<? extends TreeNode>, TreeNodeSerializer> {
        @Override
        public TreeNodeSerializer load(@Nonnull final Class<? extends TreeNode> key) throws Exception {
            final TreeNodeSerializerImplementation prototype = generator.getSerializer(key);
            return new TreeNodeSerializerProxy(prototype);
        }
    }

    private final class TreeNodeSerializerProxy
            implements TreeNodeSerializer, Delegator<TreeNodeSerializerImplementation> {
        private final TreeNodeSerializerImplementation delegate;

        TreeNodeSerializerProxy(final TreeNodeSerializerImplementation delegate) {
            this.delegate = delegate;
        }

        @Override
        public TreeNodeSerializerImplementation getDelegate() {
            return delegate;
        }

        @Override
        public void serialize(final TreeNode obj, final BindingStreamEventWriter stream) throws IOException {
            delegate.serialize(BindingNormalizedNodeCodecRegistry.this, obj, stream);
        }
    }

}
