/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.runtime;

import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

/**
 * Contract for registry of {@link TreeNodeSerializer}.
 * The contract is kept between implementation of {@link TreeNodeSerializerImplementation},
 * Registry provides lookup for serializers to support recursive
 * serialization of nested {@link TreeNode}s.
 */
public interface TreeNodeSerializerRegistry {

    /**
     * Returns implementation of requested serializer.
     * @param binding input binding class
     * @return returns serializer, based on input binding class
     */
    TreeNodeSerializer getSerializer(Class<? extends TreeNode> binding);
}
