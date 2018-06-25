/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterators;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.ServiceReference;

@NonNullByDefault
final class Dict extends Dictionary<String, Object> {
    private static final Dict EMPTY = new Dict(ImmutableMap.of());

    private final Map<String, Object> map;

    private Dict(final Map<String, Object> map) {
        this.map = requireNonNull(map);
    }

    static Dict fromReference(final ServiceReference<?> ref) {
        final String[] keys = ref.getPropertyKeys();
        if (keys.length == 0) {
            return EMPTY;
        }

        final Builder<String, Object> b = ImmutableMap.builderWithExpectedSize(keys.length);
        for (String key : keys) {
            final Object value = ref.getProperty(key);
            if (value != null) {
                b.put(key, value);
            }
        }

        return new Dict(b.build());
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Enumeration<String> keys() {
        return Iterators.asEnumeration(map.keySet().iterator());
    }

    @Override
    public Enumeration<Object> elements() {
        return Iterators.asEnumeration(map.values().iterator());
    }

    @Override
    public Object get(final @Nullable Object key) {
        return map.get(key);
    }

    @Override
    public Object put(final String key, final Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(final @Nullable Object key) {
        return map.remove(key);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Dict && map.equals(((Dict) obj).map);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
