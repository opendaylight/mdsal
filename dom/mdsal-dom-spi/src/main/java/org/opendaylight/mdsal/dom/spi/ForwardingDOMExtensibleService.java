/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMExtensibleService;
import org.opendaylight.mdsal.dom.api.DOMServiceExtension;

@Beta
public abstract class ForwardingDOMExtensibleService<T extends DOMExtensibleService<T, E>,
        E extends DOMServiceExtension<T, E>> extends ForwardingObject implements DOMExtensibleService<T, E> {
    @Override
    public <X extends E> @Nullable X extension(final Class<X> type) {
        return delegate().extension(type);
    }

    @Override
    public Collection<? extends E> supportedExtensions() {
        return delegate().supportedExtensions();
    }

    @Override
    protected abstract @NonNull T delegate();
}
