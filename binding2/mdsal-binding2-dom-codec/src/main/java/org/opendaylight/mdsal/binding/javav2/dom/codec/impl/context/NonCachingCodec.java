/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.codecs.BindingNormalizedNodeCachingCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.codecs.BindingNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Non caching codec.
 *
 * @param <D>
 *            - type of tree node
 */
@Beta
public class NonCachingCodec<D extends TreeNode> implements BindingNormalizedNodeCachingCodec<D> {

    private final BindingNormalizedNodeCodec<D> delegate;

    public NonCachingCodec(final BindingNormalizedNodeCodec<D> delegate) {
        this.delegate = delegate;
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> data) {
        return delegate.deserialize(data);
    }

    @Override
    public NormalizedNode<?, ?> serialize(final D data) {
        return delegate.serialize(data);
    }

    @Override
    public void close() {
    }

}
