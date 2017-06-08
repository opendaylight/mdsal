/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Singleton codec for translating operations with implicit input statements, which are not mapped by binding
 * spec v2. Since there is no equivalent, we always return null.
 *
 * @param <D>
 *            - TreeNode type
 */
@Beta
public final class UnmappedOperationInputCodec<D extends TreeNode> implements OperationInputCodec<D> {

    private static final UnmappedOperationInputCodec<?> INSTANCE = new UnmappedOperationInputCodec<>();

    private UnmappedOperationInputCodec() {

    }

    @SuppressWarnings("unchecked")
    public static <D extends TreeNode> UnmappedOperationInputCodec<D> getInstance() {
        return (UnmappedOperationInputCodec<D>) INSTANCE;
    }

    @Nonnull
    @Override
    public D deserialize(@Nonnull final NormalizedNode<?, ?> data) {
        return null;
    }

    @Nonnull
    @Override
    public NormalizedNode<?, ?> serialize(@Nonnull final D data) {
        throw new UnsupportedOperationException("Serialization of " + data + " not supported");
    }
}