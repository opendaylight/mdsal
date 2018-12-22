/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.generator.api.ClassLoaderResolver;

/**
 * A {@link ClassLoaderResolver} suitable for single-classloader deployments. It always returns only its own
 * classloader.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class SingleClassLoaderResolver implements ClassLoaderResolver {
    private static final SingleClassLoaderResolver INSTANCE = new SingleClassLoaderResolver(
        SingleClassLoaderResolver.class.getClassLoader());

    private final ClassLoader classLoader;

    private SingleClassLoaderResolver(final ClassLoader classLoader) {
        this.classLoader = requireNonNull(classLoader);
    }

    /**
     * Return a global singleton {@link SingleClassLoaderResolver}, which loads classes from its own class loader.
     *
     * @return A global singleton instance.
     */
    public static SingleClassLoaderResolver getInstance() {
        return INSTANCE;
    }

    /**
     * Return a {@link SingleClassLoaderResolver} backed by the specified {@link ClassLoader}. The returned instance may
     * be shared.
     *
     * @param classLoader Backing ClassLoader
     * @return A potentially-shared instance
     */
    public static SingleClassLoaderResolver of(final ClassLoader classLoader) {
        return INSTANCE.classLoader.equals(classLoader) ? INSTANCE : new SingleClassLoaderResolver(classLoader);
    }

    @Override
    public Optional<ClassLoader> findClassLoader(final String fqcn) {
        return Optional.of(classLoader);
    }

    @Override
    public Set<ClassLoader> getClassLoaders() {
        return ImmutableSet.of(classLoader);
    }

    @Override
    public int hashCode() {
        return classLoader.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof SingleClassLoaderResolver
                && classLoader.equals(((SingleClassLoaderResolver)obj).classLoader);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("classLoader", classLoader).toString();
    }
}
