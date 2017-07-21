/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;

/**
 * Indicates that a shard is writable via the provided {@link #createProducer(Collection) createProducer} method.
 */
@Beta
public interface WriteableDOMDataTreeShard extends DOMDataTreeShard {

    /**
     * Create a producer that has the ability to write into the provided subtrees.
     *
     * @param paths Subtrees that the caller wants to write into.
     * @return Producer.
     */
    DOMDataTreeShardProducer createProducer(Collection<DOMDataTreeIdentifier> paths);
}
