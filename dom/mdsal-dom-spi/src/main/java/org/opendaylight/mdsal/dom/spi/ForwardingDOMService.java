/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMService;

public abstract class ForwardingDOMService<T extends DOMService<T, E>, E extends DOMService.Extension<T, E>>
        extends ForwardingObject implements DOMService<T, E> {
    @Override
    @SuppressWarnings("hiding")
    public final <T extends E> @Nullable T extension(final Class<T> type) {
        return DOMService.super.extension(type);
    }

    @Override
    @SuppressWarnings("hiding")
    public final <T extends E> Optional<T> findExtension(final Class<T> type) {
        return DOMService.super.findExtension(type);
    }

    @Override
    public final Collection<? extends E> supportedExtensions() {
        return supportedExtensions(delegate().supportedExtensions());
    }

    protected @NonNull Collection<? extends E> supportedExtensions(final @NonNull Collection<? extends E> extensions) {
        return extensions;
    }

    @Override
    protected abstract @NonNull T delegate();
}
