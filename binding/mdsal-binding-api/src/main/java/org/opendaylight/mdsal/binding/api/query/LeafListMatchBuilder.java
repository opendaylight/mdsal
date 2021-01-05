/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Basic builder for a match of a leaf-list value.
 *
 * @param <T> query result type
 * @param <V> value type
 */
@Beta
public interface LeafListMatchBuilder<T extends DataObject, V> extends ValueMatchBuilder<T, Set<V>> {

    @NonNull ValueMatch<T> isEmpty();

    // Note: amounts to isEmpty().negate()
    @NonNull ValueMatch<T> notEmpty();

    @NonNull ContainsMatchBuilder<T, V> contains();

    @NonNull ContainsMatchBuilder<T, V> allMatch();

    @NonNull ContainsMatchBuilder<T, V> anyMatch();

    // Note: amounts to anyMatch().negate()
    @NonNull ContainsMatchBuilder<T, V> noneMatch();
}
