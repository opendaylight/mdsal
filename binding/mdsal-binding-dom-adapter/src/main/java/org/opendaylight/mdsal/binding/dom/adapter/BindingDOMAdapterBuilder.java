/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.dom.api.DOMService;

abstract class BindingDOMAdapterBuilder<T extends BindingService> extends AdapterBuilder<T, DOMService> {

    @FunctionalInterface
    interface Factory<T extends BindingService> {

        BindingDOMAdapterBuilder<T> newBuilder();

    }

    private BindingToNormalizedNodeCodec codec;

    public void setCodec(final BindingToNormalizedNodeCodec codec) {
        this.codec = codec;
    }

    @Override
    protected final T createInstance(final ClassToInstanceMap<DOMService> delegates) {
        Preconditions.checkState(codec != null);
        return createInstance(codec,delegates);
    }

    protected abstract T createInstance(BindingToNormalizedNodeCodec codec2, ClassToInstanceMap<DOMService> delegates);

}
