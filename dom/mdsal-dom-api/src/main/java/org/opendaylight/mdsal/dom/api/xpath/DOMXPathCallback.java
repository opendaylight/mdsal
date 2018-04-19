/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.xpath;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.xml.xpath.XPathException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.CheckedValue;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathResult;

/**
 * A callback to be executed when XPath evaluation is complete. This interface is modeled as a {@link BiConsumer},
 * with implementations expected to check for failure before processing the result, for example like this:
 * <code>
 *     DOMXPathCallback callback = (success, failure) -&gt; {
 *         if (failure != null) {
 *             // ... code to handle failure ...
 *         } else {
 *             // ... code to handle success ...
 *         }
 *     };
 * </code>
 *
 * <p>
 * Alternatively, you can use {@link #of(Consumer, Consumer)} utility method, which provides the equivalent
 * dispatch with a nicer interface:
 * <code>
 *     DOMXPathCallback callback = DOMXPathCallback.of((success) -&gt; {
 *         // ... code to handle success ...
 *     }, (failure) -&gt; {
 *         // ... code to handle failure ...
 *     });
 * </code>
 *
 * <p>
 * Finally, you can create a bridging {@link DOMXPathCallback} through either {@link #completingFuture(SettableFuture)}
 * or {@link #completingFuture(CompletableFuture)}.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@FunctionalInterface
public interface DOMXPathCallback extends Consumer<CheckedValue<Optional<? extends XPathResult<?>>, XPathException>> {
    /**
     * Create a DOMXPathCallback composed of two separate consumers, one for success and one for failure.
     *
     * @param onSuccess Callback to invoke on success
     * @param onFailure Callback to invoke on failure
     * @return A {@link DOMXPathCallback} which delegates to provided methods
     * @throws NullPointerException if any of the arguments is null
     */
    static DOMXPathCallback of(final Consumer<Optional<? extends XPathResult<?>>> onSuccess,
            final Consumer<XPathException> onFailure) {
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
     * Create a {@link DOMXPathCallback} which completes the specified future.
     *
     * @param future {@link CompletableFuture} to complete
     * @return A {@link DOMXPathCallback}
     * @throws NullPointerException if any of the arguments is null
     */
    static DOMXPathCallback completingFuture(final CompletableFuture<Optional<? extends XPathResult<?>>> future) {
        requireNonNull(future);
        return result -> result.completeFuture(future);
    }

    /**
     * Create a {@link DOMXPathCallback} which completes the specified future.
     *
     * @param future {@link SettableFuture} to complete
     * @return A {@link DOMXPathCallback}
     * @throws NullPointerException if any of the arguments is null
     */
    static DOMXPathCallback completingFuture(final SettableFuture<Optional<? extends XPathResult<?>>> future) {
        requireNonNull(future);
        return result -> result.completeFuture(future);
    }
}
