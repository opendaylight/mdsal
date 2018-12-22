/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A resolver which locates the {@link ClassLoader} which is expected to be able to load specified class.
 * Implementations are expected to be {@link Immutable}, hence they must return same results irrespective of invocation
 * context.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface ClassLoaderResolver extends Immutable {
    /**
     * An {@link InvariantClassLoadingStrategy} backed by a {@link ClassLoaderResolver}, which is the sole source
     * of ClassLoaders through which the strategy loads classes.
     */
    public final class LoadingStrategy implements InvariantClassLoadingStrategy {
        private final ClassLoaderResolver resolver;

        public LoadingStrategy(final ClassLoaderResolver resolver) {
            this.resolver = requireNonNull(resolver);
        }

        @Override
        public Class<?> loadClass(final @Nullable String fullyQualifiedName) throws ClassNotFoundException {
            final @NonNull String checked = requireNonNull(fullyQualifiedName);
            final Optional<ClassLoader> optLoader = resolver.findClassLoader(checked);
            if (optLoader.isPresent()) {
                return optLoader.get().loadClass(checked);
            }
            throw new ClassNotFoundException("Failed to resolve ClassLoader for " + checked);
        }
    }

    /**
     * Find the class loader for specified fully-qualified class name, as returned by {@link Class#getName()}.
     *
     * @param fqcn Fully-Qualified Class Name
     * @return ClassLoader if found.
     */
    Optional<ClassLoader> findClassLoader(String fqcn);

    /**
     * Return all constituent class loaders. This set must include all classloaders which can ever be returned by
     * {@link #findClassLoader(String)}.
     *
     * @return Set of class loaders.
     */
    Set<ClassLoader> getClassLoaders();

    /**
     * Find the class loader for the specified {@link Type}. The default implementation defers to
     * {@link #findClassLoader(String)}.
     *
     * @param type Binding type
     * @return ClassLoader if found.
     */
    default Optional<ClassLoader> findClassLoader(final Type type) {
        return findClassLoader(requireNonNull(type.getFullyQualifiedName()));
    }

    default LoadingStrategy asLoadingStrategy() {
        return new LoadingStrategy(this);
    }
}
