/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.api.query.MatchBuilderValue;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.mdsal.binding.spi.query.LambdaDecoder.LambdaTarget;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

abstract class AbstractMatchBuilderValue<R extends DataObject, O extends DataObject, T extends DataObject, V>
        implements MatchBuilderValue<T, V> {
    private final InstanceIdentifier<R> rootPath;
    private final InstanceIdentifier<O> childPath;
    private final InstanceIdentifier<T> targetPath;
    private final LambdaTarget targetLeaf;

    AbstractMatchBuilderValue(final InstanceIdentifier<R> rootPath, final InstanceIdentifier<O> childPath,
            final InstanceIdentifier<T> targetPath,final LambdaTarget targetLeaf) {
        this.rootPath = requireNonNull(rootPath);
        this.childPath = requireNonNull(childPath);
        this.targetPath = requireNonNull(targetPath);
        this.targetLeaf = requireNonNull(targetLeaf);
    }

    @Override
    public final ValueMatch<T> nonNull() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final ValueMatch<T> valueEquals(final V value) {
        // TODO Auto-generated method stub
        return null;
    }
}
