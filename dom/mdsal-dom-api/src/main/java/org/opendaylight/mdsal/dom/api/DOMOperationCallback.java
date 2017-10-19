/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.CheckedValue;

/**
 * A callback to be executed when an operation invocation completes. This interface is modeled as a {@link Consumer}
 * of {@link CheckedValue}, which needs to be unwrapped.
 *
 * <p>
 * Alternatively, you can use {@link #of(Consumer, Consumer)} utility method, which provides the equivalent
 * dispatch with a nicer interface:
 * <code>
 *     DOMOperationCallback callback = DOMOperationCallback.of((success) -&gt; {
 *         // ... code to handle success ...
 *     }, (failure) -&gt; {
 *         // ... code to handle failure ...
 *     });
 * </code>
 *
 * <p>
 * Finally, you can create a bridging {@link DOMOperationCallback} through either
 * {@link #completingFuture(SettableFuture)} or {@link #completingFuture(CompletableFuture)}.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@FunctionalInterface
public interface DOMOperationCallback extends Consumer<CheckedValue<DOMOperationResult, DOMRpcException>> {
    /**
     * Create a DOMOperationCallback composed of two separate consumers, one for success and one for failure.
     *
     * @param onSuccess Callback to invoke on success
     * @param onFailure Callback to invoke on failure
     * @return A {@link DOMOperationCallback} which delegates to provided methods
     * @throws NullPointerException if any of the arguments is null
     */
    static DOMOperationCallback of(final Consumer<DOMOperationResult> onSuccess,
            final Consumer<DOMRpcException> onFailure) {
        requireNonNull(onSuccess);
        requireNonNull(onFailure);
        return result -> {
            if (result.isPresent()) {
                onSuccess.accept(result.get());
            } else {
                onFailure.accept(result.getException());
            }
        };
    }

    /**
     * Create a {@link DOMOperationCallback} which completes the specified future.
     *
     * @param future {@link CompletableFuture} to complete
     * @return A {@link DOMOperationCallback}
     * @throws NullPointerException if any of the arguments is null
     */
    static DOMOperationCallback completingFuture(final CompletableFuture<DOMOperationResult> future) {
        requireNonNull(future);
        return of(future::complete, future::completeExceptionally);
    }

    /**
     * Create a {@link DOMOperationCallback} which completes the specified future.
     *
     * @param future {@link SettableFuture} to complete
     * @return A {@link DOMOperationCallback}
     * @throws NullPointerException if any of the arguments is null
     */
    static DOMOperationCallback completingFuture(final SettableFuture<DOMOperationResult> future) {
        requireNonNull(future);
        return of(future::set, future::setException);
    }
}
