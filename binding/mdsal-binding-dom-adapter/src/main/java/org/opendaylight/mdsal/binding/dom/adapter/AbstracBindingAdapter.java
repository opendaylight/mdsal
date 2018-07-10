/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.concepts.Delegator;

@NonNullByDefault
abstract class AbstracBindingAdapter<T extends DOMService> implements Delegator<T> {
    private final BindingToNormalizedNodeCodec codec;
    private final T delegate;

    public AbstracBindingAdapter(final BindingToNormalizedNodeCodec codec, final T delegate) {
        this.codec = requireNonNull(codec);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public final T getDelegate() {
        return delegate;
    }

    final BindingToNormalizedNodeCodec getCodec() {
        return codec;
    }
}
