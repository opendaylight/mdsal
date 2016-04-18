/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.test.mock;

import org.opendaylight.yangtools.yang.binding.ChildTreeNode;
import org.opendaylight.yangtools.yang.binding.TreeNode;

public interface InstantiatedFoo extends TreeNode, GroupingFoo, ChildTreeNode<Nodes> {

}
