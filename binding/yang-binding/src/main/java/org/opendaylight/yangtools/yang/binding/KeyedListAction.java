/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.util.concurrent.ListenableFuture;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Interface extended by all interfaces generated for a YANG {@code action} instantiated in keyed lists.
 *
 * @author Robert Varga
 */
public interface KeyedListAction<K extends Key<T>, T extends DataObject & KeyAware<K>,
        I extends RpcInput, O extends RpcOutput> extends Action<KeyedInstanceIdentifier<T, K>, I, O> {
    @Override
    @CheckReturnValue
    ListenableFuture<RpcResult<O>> invoke(KeyedInstanceIdentifier<T, K> path, I input);
}
