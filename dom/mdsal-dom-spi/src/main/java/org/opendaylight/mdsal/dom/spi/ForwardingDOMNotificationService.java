/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import java.util.Map;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Utility implementation of a {@link DOMNotificationService} which forwards all requests
 * to a delegate instance.
 */
public abstract class ForwardingDOMNotificationService extends ForwardingObject implements DOMNotificationService {
    @Override
    protected abstract DOMNotificationService delegate();

    @Override
    public Registration registerNotificationListener(final DOMNotificationListener listener,
            final Collection<Absolute> types) {
        return delegate().registerNotificationListener(listener, types);
    }

    @Override
    public Registration registerNotificationListener(final DOMNotificationListener listener, final Absolute... types) {
        return delegate().registerNotificationListener(listener, types);
    }

    @Override
    public Registration registerNotificationListeners(final Map<Absolute, DOMNotificationListener> typeToListener) {
        return delegate().registerNotificationListeners(typeToListener);
    }
}
