/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.base;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;

/**
 * An Item represents an object that probably is only one of it's kind. For example a Nodes object
 * is only one of a kind. In YANG terms this would probably represent a container.
 *
 * @param <T>
 */

@Beta
public final class Item<T extends TreeNode> extends TreeArgument<T> {
    private final Class<T> type;

    public Item(final Class<T> type) {
        this.type = requireNonNull(type);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.getName();
    }

}
