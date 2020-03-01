/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi.query;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.api.query.DescendantQueryBuilder;
import org.opendaylight.mdsal.binding.api.query.SubtreeQuery;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultSubtreeQuery<T extends DataObject> implements SubtreeQuery<T> {
    private final InstanceIdentifier<T> rootPath;

    DefaultSubtreeQuery(final InstanceIdentifier<T> rootPath) {
        this.rootPath = requireNonNull(rootPath);
    }

    @Override
    public <C extends ChildOf<? super T>> DescendantQueryBuilder<C> extractChild(final Class<C> childClass) {
        return new DefaultDescendantQueryBuilder<>(rootPath, rootPath.builder().child(childClass));
    }
}
