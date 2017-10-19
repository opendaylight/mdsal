/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

/**
 * Caching variant of Binding to Normalized Node codec.
 *
 * <p>
 * Caching may introduce performance penalty to serialization / deserialization
 * but may decrease use of heap for repetitive objects.
 *
 * @param <T>
 *            - Binding representation of data
 */
@Beta
public interface BindingNormalizedNodeCachingCodec<T extends TreeNode>
        extends BindingNormalizedNodeCodec<T>, AutoCloseable {

    /**
     * Invoking close will invalidate this codec and any of its child codecs and
     * will invalidate cache.
     *
     * <p>
     * Any subsequent calls to this codec will fail with
     * {@link IllegalStateException} thrown.
     */
    @Override
    void close();
}

