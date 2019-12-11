/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.testkit.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;

@Beta
@NonNullByDefault
public abstract class AbstractListenerEvent<T> {
    private final T listener;
    private final Object marker;

    protected AbstractListenerEvent(final Object marker, final T listener) {
        this.listener = requireNonNull(listener);
        this.marker = requireNonNull(listener);
    }

    public final T listener() {
        return listener;
    }

    public final Object marker() {
        return marker;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("listener", listener);
    }
}
