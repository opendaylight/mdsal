/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

/**
 * ClusteredDOMDataTreeChangeListener is a marker interface to enable data tree change notifications on all
 * instances in a cluster where this listener is registered.
 *
 * <p>
 * Applications should implement ClusteredDOMDataTreeChangeListener instead of {@link DOMDataTreeChangeListener},
 * if they want to listen for data tree change notifications on any node of a clustered data store.
 * {@link DOMDataTreeChangeListener} enables notifications only at the leader of the data store.
 *
 * @deprecated due to design change. The expected listener notification mode to be defined directly using
 * {@link DOMDataTreeChangeListener#clusterMode()} method.
 *
 * @author Thomas Pantelis
 */

@Deprecated(since = "12.0.0", forRemoval = true)
public interface ClusteredDOMDataTreeChangeListener extends DOMDataTreeChangeListener {
}
