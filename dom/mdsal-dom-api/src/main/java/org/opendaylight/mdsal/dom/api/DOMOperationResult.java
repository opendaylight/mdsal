/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Interface defining a result of an operation invocation.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface DOMOperationResult {
    /**
     * Returns a set of errors and warnings which occurred during processing the call.
     *
     * @return a Collection of {@link RpcError}, guaranteed to be non-null. In case no errors are reported, an empty
     *         collection is returned.
     */
    Collection<RpcError> getErrors();

    /**
     * Returns the value result of the call.
     *
     * @return Invocation result, absent if the operation has not produced a result. This might be the case if the
     *         operation does not produce a result, or if it failed.
     */
    Optional<NormalizedNode<?, ?>> getOutput();
}
