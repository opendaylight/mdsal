/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import java.lang.ref.WeakReference;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class RpcInputCodec extends WeakReference<BindingCodecTree> {
    private final @NonNull BindingDataObjectCodecTreeNode<RpcInput> codec;

    RpcInputCodec(final BindingCodecTree referent, final QName rpcName, final QName inputName) {
        super(referent);

        final var path = Absolute.of(rpcName, inputName);
        @SuppressWarnings("unchecked")
        final var found = (BindingDataObjectCodecTreeNode<RpcInput>) referent.getSubtreeCodec(path);
        if (found == null) {
            throw new IllegalStateException("Cannot findl codec for " + path);
        }
        codec = found;
    }

    @Nullable BindingDataObjectCodecTreeNode<RpcInput> accessCodec(final BindingCodecTree currentTree) {
        return requireNonNull(currentTree) == get() ? codec : null;
    }
}
