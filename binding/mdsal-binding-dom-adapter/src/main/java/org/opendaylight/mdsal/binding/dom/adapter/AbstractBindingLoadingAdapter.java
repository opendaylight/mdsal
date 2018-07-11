/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
abstract class AbstractBindingLoadingAdapter<D, K, V> extends AbstractBindingAdapter<D> {
    private final LoadingCache<K, V> proxies = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<K, V>() {
        @Override
        public V load(final K key) {
            return loadAdapter(key);
        }
    });

    AbstractBindingLoadingAdapter(final BindingToNormalizedNodeCodec codec, final D delegate) {
        super(codec, delegate);
    }

    final V getAdapter(final K key) {
        return proxies.getUnchecked(key);
    }

    abstract V loadAdapter(K key);
}
