/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;

@Beta
public interface ContainsMatchBuilder<T extends DataObject, V> {

    // List.contains(value)
    @NonNull ValueMatch<T> item(V value);

    // values.stream().allMatch(x -> List.contains(x))
    @NonNull ValueMatch<T> allOf(Collection<? extends V> values);

    // values.stream().anyMatch(x -> List.contains(x))
    @NonNull ValueMatch<T> anyOf(Collection<? extends V> values);

    // values.stream().noneMatch(x -> List.contains(x))
    @NonNull ValueMatch<T> noneOf(Collection<? extends V> values);

    // The function 'values' -> 'this' is a surjective function
    //
    // i.e. [a, b].hasOnlyElementsOf([a]) == false
    // i.e. [a, a].hasOnlyElementsOf([a]) == true
    // i.e. [a, a].hasOnlyElementsOf([a, b]) == true
    // i.e. [a, a].hasOnlyElementsOf([ ]) == false
    // i.e. [a, a].hasOnlyElementsOf([z]) == false
    @NonNull ValueMatch<T> subsetOf(Collection<? extends V> values);
}
