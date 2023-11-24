/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.opendaylight.yangtools.concepts.ExtensibleObject;
import org.opendaylight.yangtools.concepts.ObjectExtension;

/**
 * Marker interface for services which can be obtained from a {@link DOMMountPoint} instance. The only further semantics
 * implied are that each service can also host related {@link Extension}s supported via the {@link ExtensibleObject}
 * contract.
 *
 * @param <T> Concrete service type
 * @param <E> Extension type
 */
public interface DOMService<T extends DOMService<T, E>, E extends DOMService.Extension<T, E>>
        extends ExtensibleObject<T, E> {
    /**
     * Extension to a concrete {@link DOMService}.
     *
     * @param <T> Concrete service type
     * @param <E> Extension type
     */
    interface Extension<T extends DOMService<T, E>, E extends Extension<T, E>> extends ObjectExtension<T, E> {
        // Only a marker interface
    }
}
