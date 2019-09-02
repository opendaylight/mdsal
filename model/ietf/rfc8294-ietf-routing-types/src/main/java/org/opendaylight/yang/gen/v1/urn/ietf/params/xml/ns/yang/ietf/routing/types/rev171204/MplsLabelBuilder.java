/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.types.rev171204;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for {@link MplsLabel} instances.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class MplsLabelBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(MplsLabelBuilder.class);

    @SuppressWarnings("null")
    private static final LoadingCache<Entry<ClassLoader, String>, Optional<MplsLabel>> CLASS_CACHE =
    CacheBuilder.newBuilder()
            .weakKeys().build(new CacheLoader<Entry<ClassLoader, String>, Optional<MplsLabel>>() {
                @Override
                public Optional<MplsLabel> load(final Entry<ClassLoader, String> key) {
                    return loadClass(key.getKey(), key.getValue());
                }
            });

    private MplsLabelBuilder() {

    }

    public static MplsLabel getDefaultInstance(final String defaultValue) {
        if (defaultValue.startsWith("interface ")) {
            final Optional<MplsLabel> optStatic = CLASS_CACHE.getUnchecked(
                new SimpleImmutableEntry<>(MplsLabelBuilder.class.getClassLoader(), defaultValue));
            if (optStatic.isPresent()) {
                return optStatic.get();
            }

            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            if (tccl != null) {
                final Optional<MplsLabel> optThreadLocal = CLASS_CACHE.getUnchecked(
                    new SimpleImmutableEntry<>(tccl, defaultValue));
                if (optThreadLocal.isPresent()) {
                    return optThreadLocal.get();
                }
            }
        }

        return new MplsLabel(new MplsLabelGeneralUse(Uint32.valueOf(defaultValue)));
    }

    static Optional<MplsLabel> loadClass(final ClassLoader loader, final String key) {
        final Class<?> cls;
        try {
            cls = ClassLoaderUtils.loadClass(loader, key);
        } catch (ClassNotFoundException e) {
            LOG.debug("%s not found in classloader of %s", key, loader);
            return Optional.empty();
        }

        final Class<? extends MplsLabelSpecialPurposeValue> cast;
        try {
            cast = cls.asSubclass(MplsLabelSpecialPurposeValue.class);
        } catch (ClassCastException e) {
            LOG.warn("%s does not implement %s", MplsLabelSpecialPurposeValue.class);
            return Optional.empty();
        }

        return Optional.of(new MplsLabel(cast));
    }
}
