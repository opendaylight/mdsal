/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeLoopException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeServiceExtension;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

@Beta
public abstract class ForwardingDOMDataTreeService
        extends ForwardingDOMExtensibleService<DOMDataTreeService, DOMDataTreeServiceExtension>
        implements DOMDataTreeService {
    @Override
    public DOMDataTreeProducer createProducer(final Collection<DOMDataTreeIdentifier> subtrees) {
        return delegate().createProducer(subtrees);
    }

    @Override
    public <T extends DOMDataTreeListener> ListenerRegistration<T> registerListener(final T listener,
            final Collection<DOMDataTreeIdentifier> subtrees, final boolean allowRxMerges,
            final Collection<DOMDataTreeProducer> producers) throws DOMDataTreeLoopException {
        return delegate().registerListener(listener, subtrees, allowRxMerges, producers);
    }
}
