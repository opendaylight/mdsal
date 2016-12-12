/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.shard.DOMDataTreeShardProducer;

public final class SubshardProducerSpecification {
    private final Collection<DOMDataTreeIdentifier> prefixes = new ArrayList<>(1);
    private final ChildShardContext shard;

    public SubshardProducerSpecification(final ChildShardContext subshard) {
        this.shard = Preconditions.checkNotNull(subshard);
    }

    public void addPrefix(final DOMDataTreeIdentifier prefix) {
        prefixes.add(prefix);
    }

    public DOMDataTreeShardProducer createProducer() {
        return shard.getShard().createProducer(prefixes);
    }

    public DOMDataTreeIdentifier getPrefix() {
        return shard.getPrefix();
    }
}
