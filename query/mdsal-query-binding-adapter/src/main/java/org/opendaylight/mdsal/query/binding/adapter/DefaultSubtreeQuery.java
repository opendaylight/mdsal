/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.query.binding.api.DescendantQueryBuilder;
import org.opendaylight.mdsal.query.binding.api.SubtreeQuery;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultSubtreeQuery<T extends DataObject> implements SubtreeQuery<T> {
    private final AdaptingQueryBuilder builder;

    private final InstanceIdentifier<T> rootPath;

    DefaultSubtreeQuery(final BindingCodecTree codec, final InstanceIdentifier<T> rootPath) {
        this.builder = new AdaptingQueryBuilder(codec);
        this.rootPath = requireNonNull(rootPath);
        builder.setRootPath(rootPath);
    }

    @Override
    public <C extends ChildOf<? super T>> DescendantQueryBuilder<C> extractChild(final Class<C> childClass) {
        return new DefaultDescendantQueryBuilder<>(builder, rootPath.builder().child(childClass));
    }
}
