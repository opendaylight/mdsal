/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Basic builder for a match of a leaf-list value.
 *
 * @param <T> query result type
 * @param <V> value type
 */
@Beta
public interface LeafListValueMatchBuilder<T extends DataObject, V> extends ValueMatchBuilder<T, List<V>> {

    @NonNull ValueMatch<T> isEmpty();

    @NonNull ValueMatch<T> notEmpty();

    @NonNull ContainsMatchBuilder<T, V> contains();
}
