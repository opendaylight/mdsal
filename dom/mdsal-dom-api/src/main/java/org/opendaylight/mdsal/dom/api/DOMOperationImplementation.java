/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Interface implemented by an individual RPC implementation. This API allows for dispatch implementations, e.g.
 * an individual object handling a multitude of operations.
 */
@NonNullByDefault
public interface DOMOperationImplementation extends DOMImplementation {
    /**
     * Initiate invocation of the operation. Implementations of this method are expected to not block.
     *
     * @param operation Operation identifier which was invoked
     * @param input Input arguments
     * @param callback Callback to invoke with the invocation result
     * @param callbackExecutor Executor to use for executing the callback
     */
    void invokeOperation(DOMRpcIdentifier operation, NormalizedNode<?, ?> input, DOMOperationCallback callback,
            Executor callbackExecutor);
}
