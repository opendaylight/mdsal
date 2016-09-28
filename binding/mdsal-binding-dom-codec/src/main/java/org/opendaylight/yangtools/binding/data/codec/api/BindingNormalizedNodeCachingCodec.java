/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Caching variant of Binding to Normalized Node codec.
 *
 * <p>
 * Caching may introduce performance penalty to serialization / deserialization
 * but may decrease use of heap for repetitive objects.
 *
 * @param <T> Binding representation of data
 */
@Deprecated
public interface BindingNormalizedNodeCachingCodec<T extends DataObject> extends
        BindingNormalizedNodeCodec<T>, AutoCloseable {
    /**
     * Invoking close will invalidate this codec and any of its child
     * codecs and will invalidate cache.
     *
     * <p>
     * Any subsequent calls to this codec will fail with {@link IllegalStateException}
     * thrown.
     */
    @Override
    void close();
}
