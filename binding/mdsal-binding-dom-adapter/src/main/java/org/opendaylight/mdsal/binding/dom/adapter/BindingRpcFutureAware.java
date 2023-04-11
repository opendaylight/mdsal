/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Migration trait for exposing the Binding listenable future.
 */
// FIXME: is this interface still useful? can we integrate it into our two implementations?
interface BindingRpcFutureAware<O extends RpcOutput> {

    @NonNull ListenableFuture<RpcResult<O>> getBindingFuture();
}
