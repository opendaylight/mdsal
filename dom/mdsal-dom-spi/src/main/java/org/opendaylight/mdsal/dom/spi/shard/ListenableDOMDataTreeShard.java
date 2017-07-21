/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.annotations.Beta;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeLoopException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * A {@link DOMDataTreeShard} which allows registration of listeners.
 * See {@link org.opendaylight.mdsal.dom.api.DOMDataTreeService} for details.
 */
@Beta
public interface ListenableDOMDataTreeShard extends DOMDataTreeShard {

    @Nonnull <T extends DOMDataTreeListener> ListenerRegistration<T> registerListener(@Nonnull T listener,
            @Nonnull Collection<DOMDataTreeIdentifier> subtrees, boolean allowRxMerges,
            @Nonnull Collection<DOMDataTreeProducer> producers) throws DOMDataTreeLoopException;
}
