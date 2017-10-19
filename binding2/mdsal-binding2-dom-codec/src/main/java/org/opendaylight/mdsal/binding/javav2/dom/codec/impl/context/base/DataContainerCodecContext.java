/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.MissingSchemaException;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.NonCachingCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.cache.CachingNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer.BindingToNormalizedStreamWriter;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

@Beta
public abstract class DataContainerCodecContext<D extends TreeNode, T> extends NodeCodecContext<D> {

    private final DataContainerCodecPrototype<T> prototype;
    private volatile TreeNodeSerializer eventStreamSerializer;

    protected DataContainerCodecContext(final DataContainerCodecPrototype<T> prototype) {
        this.prototype = prototype;
    }

    @Nonnull
    @Override
    public final T getSchema() {
        return prototype.getSchema();
    }

    protected final QNameModule namespace() {
        return prototype.getNamespace();
    }

    protected final CodecContextFactory factory() {
        return prototype.getFactory();
    }

    @Override
    public YangInstanceIdentifier.PathArgument getDomPathArgument() {
        return prototype.getYangArg();
    }

    /**
     * Returns nested node context using supplied YANG Instance Identifier.
     *
     * @param arg Yang Instance Identifier Argument
     * @return Context of child
     * @throws IllegalArgumentException If supplied argument does not represent valid child.
     */
    @Nonnull
    @Override
    public abstract NodeCodecContext<?> yangPathArgumentChild(YangInstanceIdentifier.PathArgument arg);

    /**
     * Returns nested node context using supplied Binding Instance Identifier
     * and adds YANG instance identifiers to supplied list.
     *
     * @param arg Binding Instance Identifier Argument
     * @return Context of child or null if supplied {@code arg} does not represent valid child.
     * @throws IllegalArgumentException If supplied argument does not represent valid child.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public DataContainerCodecContext<?,?> bindingPathArgumentChild(@Nonnull final TreeArgument<?> arg,
            final List<PathArgument> builder) {
        final DataContainerCodecContext<?,?> child = streamChild((Class<? extends TreeNode>) arg.getType());
        if (builder != null) {
            child.addYangPathArgument(arg,builder);
        }
        return child;
    }

    /**
     * Returns de-serialized Binding Path Argument from YANG instance identifier.
     *
     * @param domArg input path argument
     * @return returns binding path argument
     */
    @SuppressWarnings("rawtypes")
    protected TreeArgument getBindingPathArgument(final YangInstanceIdentifier.PathArgument domArg) {
        return bindingArg();
    }

    @SuppressWarnings("rawtypes")
    protected final TreeArgument bindingArg() {
        return prototype.getBindingArg();
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    @Override
    public final Class<D> getBindingClass() {
        return Class.class.cast(prototype.getBindingClass());
    }

    /**
     * Returns child context as if it was walked by
     * {@link BindingStreamEventWriter}. This means that to enter case, one
     * must issue getChild(ChoiceClass).getChild(CaseClass).
     *
     * @param childClass input child class
     * @return Context of child node or null, if supplied class is not subtree child
     * @throws IllegalArgumentException
     *             If supplied child class is not valid in specified context.
     */
    @Nonnull
    @Override
    public abstract <C extends TreeNode> DataContainerCodecContext<C,?> streamChild(@Nonnull Class<C> childClass)
        throws IllegalArgumentException;

    /**
     * Returns child context as if it was walked by {@link BindingStreamEventWriter}. This means that to enter
     * case, one must issue getChild(ChoiceClass).getChild(CaseClass).
     *
     * @param childClass input child class
     * @return Context of child or Optional absent is supplied class is not applicable in context.
     */
    @Override
    public abstract <C extends TreeNode> Optional<DataContainerCodecContext<C, ?>> possibleStreamChild(@Nonnull
        Class<C> childClass);

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + prototype.getBindingClass() + "]";
    }

    @Nonnull
    @Override
    public BindingNormalizedNodeCachingCodec<D> createCachingCodec(
            @Nonnull final ImmutableCollection<Class<? extends TreeNode>> cacheSpecifier) {
        if (cacheSpecifier.isEmpty()) {
            return new NonCachingCodec<D>(this);
        }
        return new CachingNormalizedNodeCodec<D>(this, ImmutableSet.copyOf(cacheSpecifier));
    }

    public BindingStreamEventWriter createWriter(final NormalizedNodeStreamWriter domWriter) {
        return BindingToNormalizedStreamWriter.create(this, domWriter);
    }

    @Nonnull
    protected final <V> V childNonNull(@Nullable final V nullable, final YangInstanceIdentifier.PathArgument child,
            final String message, final Object... args) {
        if (nullable != null) {
            return nullable;
        }
        MissingSchemaException.checkModulePresent(factory().getRuntimeContext().getSchemaContext(), child);
        throw IncorrectNestingException.create(message, args);
    }

    @Nonnull
    protected final <V> V childNonNull(@Nullable final V nullable, final QName child, final String message,
            final Object... args) {
        if (nullable != null) {
            return nullable;
        }
        MissingSchemaException.checkModulePresent(factory().getRuntimeContext().getSchemaContext(), child);
        throw IncorrectNestingException.create(message, args);
    }

    @Nonnull
    protected final <V> V childNonNull(@Nullable final V nullable, final Class<?> childClass, final String message,
            final Object... args) {
        if (nullable != null) {
            return nullable;
        }
        MissingSchemaForClassException.check(factory().getRuntimeContext(), childClass);
        MissingClassInLoadingStrategyException.check(factory().getRuntimeContext().getStrategy(), childClass);
        throw IncorrectNestingException.create(message, args);
    }

    public TreeNodeSerializer eventStreamSerializer() {
        if (eventStreamSerializer == null) {
            eventStreamSerializer = factory().getEventStreamSerializer(getBindingClass());
        }
        return eventStreamSerializer;
    }

    @Nonnull
    @Override
    public NormalizedNode<?, ?> serialize(@Nonnull final D data) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        writeAsNormalizedNode(data, domWriter);
        return result.getResult();
    }

    @Override
    public void writeAsNormalizedNode(final D data, final NormalizedNodeStreamWriter writer) {
        try {
            eventStreamSerializer().serialize(data, createWriter(writer));
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to serialize Binding DTO",e);
        }
    }

}