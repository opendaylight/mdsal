/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.structural;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.util.ClassLoaderUtils;

/**
 *
 * Replaces org.opendaylight.yangtools.yang.binding.ChildOf from Binding Spec v1
 */
@Beta
public interface TreeChildNode<P extends TreeNode, I extends TreeArgument<?>> extends TreeNode {

    I treeIdentifier();


    // REPLACES: BindingReflections#findHierarchicalParent()
    default Class<P> treeParent() {
        return ClassLoaderUtils.findFirstGenericArgument(getClass(), TreeChildNode.class);
    }

}
