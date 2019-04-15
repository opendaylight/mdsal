/**
 * A base class for {@link DataObject}s which are also {@link Augmentable}, backed by {@link DataObjectCodecContext}.
 * While this class is public, it not part of API surface and is an implementation detail. The only reason for it being
 * public is that it needs to be accessible by code generated at runtime.
 *
 * @param <T> DataObject type
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.mdsal.binding.dom.codec.util.AugmentationReader;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * A base class for {@link OpaqueObject}s backed by {@link ForeignOpaqueData}. While this class is public, it not part
 * of API surface and is an implementation detail. The only reason for it being public is that it needs to be accessible
 * by code generated at runtime.
 *
 * @param <T> DataObject type
 */
public abstract class AbstractAugmentableDataObject<T extends DataObject & Augmentable<T>>
        extends AbstractDataObject<T> implements Augmentable<T> {
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<AbstractDataObject, ImmutableMap> CACHED_AUGMENTATIONS_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(AbstractDataObject.class, ImmutableMap.class, "cachedAugmentations");
    private volatile ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>> cachedAugmentations = null;

    public AbstractAugmentableDataObject(final DataObjectCodecContext<T, ?> ctx,
            final NormalizedNodeContainer<?, ?, ?> data) {
        super(ctx, data);
    }

    @Override
    final int codecAugmentedHashCode() {
        return 31 * super.codecAugmentedHashCode() + augmentationsImpl().hashCode();
    }

    @Override
    final boolean codecAugmentedEquals(final T other) {
        return super.codecAugmentedEquals(other) && augmentationsImpl().equals(getAllAugmentations(other));
    }

    @Override
    final ToStringHelper codecAugmentedFillToString(final ToStringHelper helper) {
        return super.codecAugmentedFillToString(helper).add("augmentations", augmentationsImpl());
    }

    private Map<Class<? extends Augmentation<T>>, Augmentation<T>> augmentationsImpl() {
        ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>> local = cachedAugmentations;
        if (local != null) {
            return local;
        }

        local = ImmutableMap.copyOf(context.getAllAugmentationsFrom(data));
        return CACHED_AUGMENTATIONS_UPDATER.compareAndSet(this, null, local) ? local : cachedAugmentations;
    }

    private static Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentations(
            final Augmentable<?> dataObject) {
        if (dataObject instanceof AugmentationReader) {
            return ((AugmentationReader) dataObject).getAugmentations(dataObject);
        }
        return BindingReflections.getAugmentations(dataObject);
    }
}
