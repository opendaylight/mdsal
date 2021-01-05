/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Builder for a match of a leaf-list value which define a total ordering by implementing the {@link Comparable}
 * interface.
 *
 * @param <T> query result type
 * @param <V> value type
 */
@Beta
public interface LeafListComparableMatchBuilder<T extends DataObject, V extends Comparable<V>>
        extends LeafListMatchBuilder<T, V> {
    @Override
    ContainsComparableMatchBuilder<T, V> contains();

    @Override
    ContainsComparableMatchBuilder<T, V> allMatch();

    @Override
    ContainsComparableMatchBuilder<T, V> anyMatch();

    @Override
    ContainsComparableMatchBuilder<T, V> noneMatch();
}
