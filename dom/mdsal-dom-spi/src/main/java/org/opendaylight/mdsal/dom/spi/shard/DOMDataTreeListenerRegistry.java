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
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * Utility interface for objects dispatching events to {@link DOMDataTreeListener}.
 *
 * @author Robert Varga
 */
@Beta
public interface DOMDataTreeListenerRegistry {

    @Nonnull <T extends DOMDataTreeListener> ListenerRegistration<T> registerListener(@Nonnull T listener,
            @Nonnull Collection<DOMDataTreeIdentifier> subtrees, boolean allowRxMerges);
}
