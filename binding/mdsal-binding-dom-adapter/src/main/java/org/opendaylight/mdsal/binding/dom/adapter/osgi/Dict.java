/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
final class Dict extends Dictionary<String, Object> {
    private static final Logger LOG = LoggerFactory.getLogger(Dict.class);
    private static final Dict EMPTY = new Dict(ImmutableMap.of());

    private final Map<String, Object> map;

    private Dict(final Map<String, Object> map) {
        this.map = ImmutableMap.copyOf(map);
    }

    static Dict fromReference(final ServiceReference<?> ref) {
        final String[] keys = ref.getPropertyKeys();
        if (keys.length == 0) {
            return EMPTY;
        }

        final Map<String, Object> props = Maps.newHashMapWithExpectedSize(keys.length);
        for (String key : keys) {
            // Ignore properties with our prefix: we are not exporting those
            if (!key.startsWith(ServiceProperties.PREFIX)) {
                final Object value = ref.getProperty(key);
                if (value != null) {
                    props.put(key, value);
                }
            }
        }

        // Second phase: apply any our properties
        for (String key : keys) {
            if (key.startsWith(ServiceProperties.OVERRIDE_PREFIX)) {
                final Object value = ref.getProperty(key);
                if (value != null) {
                    final String newKey = key.substring(ServiceProperties.OVERRIDE_PREFIX.length());
                    if (!newKey.isEmpty()) {
                        LOG.debug("Overriding property {}", newKey);
                        props.put(newKey, value);
                    }
                }
            }
        }

        return new Dict(props);
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
