/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

public interface BindingObjectCodecTreeNode<T extends BindingObject> extends BindingCodecTreeNode {
    /**
     * Returns binding class which represents API of current schema node.
     *
     * @return interface which defines API of binding representation of data.
     */
    @NonNull Class<T> getBindingClass();

    @Beta
    void writeAsNormalizedNode(T data, NormalizedNodeStreamWriter writer);
}
