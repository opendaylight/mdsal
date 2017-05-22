/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

@Beta
public interface DataTreeWriteCursor extends DataTreeCursor {

    void delete(TreeArgument<?> child);

    <T extends TreeNode> void merge(TreeArgument<T> child, T data);

    <T extends TreeNode> void write(TreeArgument<T> child, T data);
}
