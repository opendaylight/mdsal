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
import org.eclipse.jdt.annotation.NonNull;
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
    <T extends DOMDataTreeListener> @NonNull ListenerRegistration<T> registerListener(@NonNull T listener,
            @NonNull Collection<DOMDataTreeIdentifier> subtrees, boolean allowRxMerges);
}
