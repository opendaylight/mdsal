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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;

/**
 * Definition of a subshard containing the prefix and the subshard.
 */
@Beta
@NonNullByDefault
public final class ChildShardContext {
    private final WriteableDOMDataTreeShard shard;
    private final DOMDataTreeIdentifier prefix;

    public ChildShardContext(final DOMDataTreeIdentifier prefix, final WriteableDOMDataTreeShard shard) {
        this.prefix = requireNonNull(prefix);
        this.shard = requireNonNull(shard);
    }

    public WriteableDOMDataTreeShard getShard() {
        return shard;
    }

    public DOMDataTreeIdentifier getPrefix() {
        return prefix;
    }
}
