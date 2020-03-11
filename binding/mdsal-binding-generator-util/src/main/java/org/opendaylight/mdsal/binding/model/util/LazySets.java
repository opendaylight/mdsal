/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LazySets {

    private LazySets() {
        // Hidden on purpose
    }

    // FIXME: move this method to LazyCollections yangtools
    /**
     * Add an element to a set, potentially transforming the set.
     *
     * @param <E> the type of elements in the set
     * @param set Current set
     * @param obj Object that needs to be added
     * @return new set
     */
    public static <E> Set<E> lazyAdd(final Set<E> set, final E obj) {
        final Set<E> ret;

        switch (set.size()) {
            case 0:
                return Collections.singleton(obj);
            case 1:
                ret = new HashSet<>(4);
                ret.addAll(set);
                break;
            default:
                ret = set;
        }

        ret.add(obj);
        return ret;
    }
}
