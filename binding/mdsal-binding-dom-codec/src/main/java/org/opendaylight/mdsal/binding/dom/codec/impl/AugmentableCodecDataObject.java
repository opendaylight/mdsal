/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableClassToInstanceMap;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.util.AugmentationReader;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * A base class for {@link DataObject}s which are also {@link Augmentable}, backed by {@link DataObjectCodecContext}.
 * While this class is public, it not part of API surface and is an implementation detail. The only reason for it being
 * public is that it needs to be accessible by code generated at runtime.
 *
 * @param <T> DataObject type
 */
public abstract class AugmentableCodecDataObject<T extends DataObject & Augmentable<T>>
        extends CodecDataObject<T> implements Augmentable<T>, AugmentationHolder<T> {
    private final @NonNull DataObjectCodecContext<T, ?> context;

    private static final VarHandle CACHED_AUGMENTATIONS;

    static {
        try {
            CACHED_AUGMENTATIONS = MethodHandles.lookup().findVarHandle(AugmentableCodecDataObject.class,
                "cachedAugmentations", ImmutableClassToInstanceMap.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unused")
    private ImmutableClassToInstanceMap<Augmentation<T>> cachedAugmentations;

    protected AugmentableCodecDataObject(final DataObjectCodecContext<T, ?> context,
            final NormalizedNodeContainer<?, ?, ?> data) {
        super(context, data);
        this.context = requireNonNull(context, "Context must not be null");
    }

    @Override
    public final <A extends Augmentation<T>> @Nullable A augmentation(final Class<A> augmentationType) {
        requireNonNull(augmentationType, "Supplied augmentation must not be null.");

        final ImmutableClassToInstanceMap<Augmentation<T>> aug = acquireAugmentations();
        if (aug != null) {
            return aug.getInstance(augmentationType);
        }

        @SuppressWarnings({"unchecked","rawtypes"})
        final Optional<DataContainerCodecContext<?, ?>> optAugCtx = context.possibleStreamChild(
            (Class) augmentationType);
        if (optAugCtx.isPresent()) {
            final DataContainerCodecContext<?, ?> augCtx = optAugCtx.get();
            // Due to binding specification not representing grouping instantiations we can end up having the same
            // augmentation applied to a grouping multiple times. While these augmentations have the same shape, they
            // are still represented by distinct binding classes and therefore we need to make sure the result matches
            // the augmentation the user is requesting -- otherwise a strict receiver would end up with a cryptic
            // ClassCastException.
            if (augmentationType.isAssignableFrom(augCtx.getBindingClass())) {
                final Optional<NormalizedNode<?, ?>> augData = codecData().getChild(augCtx.getDomPathArgument());
                if (augData.isPresent()) {
                    return (A) augCtx.deserialize(augData.get());
                }
            }
        }
        return null;
    }

    @Override
    public final Map<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations() {
        ImmutableClassToInstanceMap<Augmentation<T>> local = acquireAugmentations();
        if (local != null) {
            return local;
        }

        local = ImmutableClassToInstanceMap.copyOf(context.getAllAugmentationsFrom(codecData()));
        final Object witness = CACHED_AUGMENTATIONS.compareAndExchangeRelease(this, null, local);
        return witness == null ? local : (ImmutableClassToInstanceMap<Augmentation<T>>) witness;
    }

    @Override
    final int codecAugmentedHashCode() {
        return 31 * super.codecAugmentedHashCode() + augmentations().hashCode();
    }

    @Override
    final boolean codecAugmentedEquals(final T other) {
        return super.codecAugmentedEquals(other) && augmentations().equals(getAllAugmentations(other));
    }

    @Override
    final ToStringHelper codecAugmentedFillToString(final ToStringHelper helper) {
        return super.codecAugmentedFillToString(helper).add("augmentations", augmentations());
    }

    private ImmutableClassToInstanceMap<Augmentation<T>> acquireAugmentations() {
        return (ImmutableClassToInstanceMap<Augmentation<T>>) CACHED_AUGMENTATIONS.getAcquire(this);
    }

    private static Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentations(
            final Augmentable<?> dataObject) {
        if (dataObject instanceof AugmentationReader) {
            return ((AugmentationReader) dataObject).getAugmentations(dataObject);
        }
        return BindingReflections.getAugmentations(dataObject);
    }
}
