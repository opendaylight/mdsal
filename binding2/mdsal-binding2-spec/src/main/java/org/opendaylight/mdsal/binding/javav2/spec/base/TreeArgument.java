/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.base;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;

@Beta
public abstract class TreeArgument<T> {

    static TreeArgument<TreeRoot> root() {
        throw new UnsupportedOperationException();
    }

    static <T extends TreeChildNode<?, ?>> Item<T> singular(Class<T> implementedInterface) {
        throw new UnsupportedOperationException();
    }

    TreeArgument() {
        // Intentionally package-visible & noop
    }

    public abstract Class<T> getType();

    @Override
    public int hashCode() {
        return getType().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TreeArgument<?> other = (TreeArgument<?>) obj;
        return getType().equals(other.getType());
    }

}
