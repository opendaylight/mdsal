/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathExpression;

/**
 * {@link DOMDataTreeService} extension which allows users to subscribe to a stream of XPath expression evaluations
 * and track as they change. This is similar to a {@link DOMDataTreeListener}, except the user does not see the tree
 * changing, but rather the result of the XPath expression changing.
 */
public interface DOMXPathListenerService extends DOMDataTreeServiceExtension {

    /**
     * Register a {@link DOMDataTreeListener} instance. Once registered, the listener
     * will start receiving changes on the selected subtrees. If the listener cannot
     * keep up with the rate of changes, and allowRxMerges is set to true, this service
     * is free to merge the changes, so that a smaller number of them will be reported,
     * possibly hiding some data transitions (like flaps).
     *
     * <p>
     * If the listener wants to write into any producer, that producer has to be mentioned
     * in the call to this method. Those producers will be bound exclusively to the
     * registration, so that accessing them outside of this listener's callback will trigger
     * an error. Any producers mentioned must be idle, e.g. they may not have an open
     * transaction at the time this method is invoked.
     *
     * <p>
     * Each listener instance can be registered at most once. Implementations of this
     * interface have to guarantee that the listener's methods will not be invoked
     * concurrently from multiple threads.
     *
     * @param listener {@link DOMDataTreeListener} that is being registered
     * @param subtrees Conceptual subtree identifier of subtrees which should be monitored
     *                 for changes. May not be null or empty.
     * @param allowRxMerges True if the backend may perform ingress state compression.
     * @param producers {@link DOMDataTreeProducer} instances to bind to the listener.
     * @return A listener registration. Once closed, the listener will no longer be
     *         invoked and the producers will be unbound.
     * @throws IllegalArgumentException if subtrees is empty or the listener is already bound
     * @throws DOMDataTreeLoopException if the registration of the listener to the specified
     *                                  subtrees with specified producers would form a
     *                                  feedback loop
     */
    @NonNull <T extends DOMDataTreeListener> ListenerRegistration<T> registerListener(@Nonnull T listener,
            @Nonnull XPathExpression expression, boolean allowRxMerges,
            @Nonnull Collection<DOMDataTreeProducer> producers) throws DOMDataTreeLoopException;

}
