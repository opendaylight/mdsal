package org.opendaylight.mdsal.binding.spi;

import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

public abstract class ForwardingDataObjectModification<T extends DataObject> extends ForwardingObject implements
        DataObjectModification<T> {

    @Override
    protected abstract DataObjectModification<T> delegate();

    @Override
    public PathArgument getIdentifier() {
        return delegate().getIdentifier();
    }

    @Override
    public Class<T> getDataType() {
        return delegate().getDataType();
    }

    @Override
    public DataObjectModification.ModificationType getModificationType() {
        return delegate().getModificationType();
    }

    @Override
    public T getDataBefore() {
        return delegate().getDataBefore();
    }

    @Override
    public T getDataAfter() {
        return delegate().getDataAfter();
    }

    @Override
    public Collection<DataObjectModification<? extends DataObject>> getModifiedChildren() {
        return delegate().getModifiedChildren();
    }

    @Override
    public <C extends ChildOf<? super T>> DataObjectModification<C> getModifiedChildContainer(final Class<C> child) {
        return delegate().getModifiedChildContainer(child);
    }

    @Override
    public <C extends Augmentation<T> & DataObject> DataObjectModification<C> getModifiedAugmentation(
            final Class<C> augmentation) {
        return delegate().getModifiedAugmentation(augmentation);
    }

    @Override
    public <C extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<C>> DataObjectModification<C> getModifiedChildListItem(
            final Class<C> listItem, final K listKey) {
        return delegate().getModifiedChildListItem(listItem, listKey);
    }

    @Override
    public DataObjectModification<? extends DataObject> getModifiedChild(final PathArgument childArgument) {
        return delegate().getModifiedChild(childArgument);
    }
}
