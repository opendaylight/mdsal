/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.spi.shard;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.concurrent.NotThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;

/**
 * Specification of subshard producer context that's used for building modification factories that span into the
 * subshards.
 */
@Beta
@NonNullByDefault
@NotThreadSafe
public final class SubshardProducerSpecification {
    private final Collection<DOMDataTreeIdentifier> prefixes = new ArrayList<>(1);
    private final ChildShardContext shard;

    public SubshardProducerSpecification(final ChildShardContext subshard) {
        this.shard = requireNonNull(subshard);
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
