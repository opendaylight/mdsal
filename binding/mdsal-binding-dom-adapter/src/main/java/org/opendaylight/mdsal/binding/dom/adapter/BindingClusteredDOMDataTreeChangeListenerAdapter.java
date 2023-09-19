/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Adapter wrapping Binding {@link ClusteredDataTreeChangeListener} and exposing
 * it as {@link ClusteredDOMDataTreeChangeListener} and translated DOM events
 * to their Binding equivalent.
 *
 * @author Thomas Pantelis
 *
 * @deprecated due to parent interface deprecation for removal. Same adapter class
 *      {@link BindingDOMDataTreeChangeListenerAdapter} expected to be used for all the cases.
 *      Listener cluster accessibility policy to be defined via registration API.
 */
@Deprecated(forRemoval = true)
final class BindingClusteredDOMDataTreeChangeListenerAdapter<T extends DataObject>
        extends BindingDOMDataTreeChangeListenerAdapter<T> implements ClusteredDOMDataTreeChangeListener {
    BindingClusteredDOMDataTreeChangeListenerAdapter(final AdapterContext codec,
            final ClusteredDataTreeChangeListener<T> listener, final LogicalDatastoreType store,
            final Class<T> augment) {
        super(codec, listener, store, augment);
    }
}
