/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

/**
 * Represents root of modification.
 */
@Beta
public interface DataTreeModification<T extends TreeNode> {

    /**
     * Get the modification root path. This is the path of the root node
     * relative to the root of InstanceIdentifier namespace.
     *
     * @return absolute path of the root node
     */
    @Nonnull
    DataTreeIdentifier<T> getRootPath();

    /**
     * Get the modification root node.
     *
     * @return modification root node
     */
    @Nonnull
    TreeNodeModification<T> getRootNode();
}
