/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.query.dom.api.DOMQueryBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class AdaptingQueryBuilder {
    private final DOMQueryBuilder builder = new DOMQueryBuilder();
    private final BindingCodecTree codec;

    AdaptingQueryBuilder(final BindingCodecTree codec) {
        this.codec = requireNonNull(codec);
    }

    void setRootPath(final @NonNull InstanceIdentifier<?> rootPath) {
        builder.setRootPath(codec.getInstanceIdentifierCodec().fromBinding(rootPath));
    }

    void setSelectPath(final @NonNull InstanceIdentifier<?> selectPath) {
        builder.setSelectPath(codec.getInstanceIdentifierCodec().fromBinding(selectPath));
    }
}
