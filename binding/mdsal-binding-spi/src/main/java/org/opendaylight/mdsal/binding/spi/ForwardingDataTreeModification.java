package org.opendaylight.mdsal.binding.spi;

import com.google.common.collect.ForwardingObject;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.yangtools.yang.binding.DataObject;

public abstract class ForwardingDataTreeModification<T extends DataObject> extends ForwardingObject implements
        DataTreeModification<T> {

    @Override
    protected abstract DataTreeModification<T> delegate();

    @Override
    public DataTreeIdentifier<T> getRootPath() {
        return delegate().getRootPath();
    }

    @Override
    public DataObjectModification<T> getRootNode() {
        return delegate().getRootNode();
    }

}
