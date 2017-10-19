/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.api;

import com.google.common.annotations.Beta;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Navigable tree representing hierarchy of Binding to Normalized Node codecs.
 *
 * <p>
 * This navigable tree is associated to concrete set of YANG models, represented
 * by SchemaContext and provides access to subtree specific serialization
 * context.
 */
@Beta
public interface BindingTreeCodec {

    /**
     * Get specific subtree serialization context by Binding path.
     *
     * @param path
     *            - {@link InstanceIdentifier} path
     * @param <T> data type
     * @return subtree codec
     */
    @Nullable
    <T extends TreeNode> BindingTreeNodeCodec<T> getSubtreeCodec(InstanceIdentifier<T> path);

    /**
     * Get specific subtree serialization context by DOM path.
     *
     * @param path
     *            - {@link YangInstanceIdentifier} path
     * @return subtree codec
     */
    @Nullable
    BindingTreeNodeCodec<?> getSubtreeCodec(YangInstanceIdentifier path);

    /**
     * Get specific subtree serialization context by {@link SchemaPath} path.
     *
     * @param path
     *            - {@link SchemaPath} path
     * @return specific subtree codec
     */
    @Nullable
    BindingTreeNodeCodec<?> getSubtreeCodec(SchemaPath path);

}
