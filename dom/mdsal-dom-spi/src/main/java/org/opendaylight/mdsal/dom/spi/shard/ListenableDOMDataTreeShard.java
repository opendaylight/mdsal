/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;

/**
 * A {@link DOMDataTreeShard} which allows registration of listeners, allowing realization of the DOMDataTreeService's
 * registerListener contract. Note that producer/consumer as well as the logical data store type are taken care of
 * by the caller, hence implementations of this interface only need to take care of communicating with their subshards.
 */
@Beta
public interface ListenableDOMDataTreeShard extends DOMDataTreeShard, DOMDataTreeListenerRegistry {

}
