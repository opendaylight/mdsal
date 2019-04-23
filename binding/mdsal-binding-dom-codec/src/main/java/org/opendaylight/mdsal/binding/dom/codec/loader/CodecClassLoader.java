/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.loader;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ClassLoader hosting types generated for a particular type. A root instance is attached to a
 * BindingCodecContext instance, so any generated classes from it can be garbage-collected when the context
 * is destroyed, as well as to prevent two contexts trampling over each other.
 *
 * <p>
 * It semantically combines two class loaders: the class loader in which this class is loaded and the class loader in
 * which a target Binding interface/class is loaded. This inherently supports multi-classloader environments -- the root
 * instance has visibility only into codec classes and for each classloader we encounter when presented with a binding
 * class we create a leaf instance and cache it in the root instance. Leaf instances are using the root loader as their
 * parent, but consult the binding class's class loader if the root loader fails to load a particular class.
 *
 * <p>In single-classloader environments, obviously, the root loader can load all binding classes, and hence no leaf
 * loader is created.
 *
 * @author Robert Varga
 */
@Beta
public abstract class CodecClassLoader extends ClassLoader {
    public interface ClassGenerator<T> {
        /**
         * Generate a class.
         *
         * @param fqcn Generated class Fully-qualified class name
         * @param bindingInterface Binding interface for which the class is being generated
         * @return A result.
         */
        GeneratorResult<T> generateClass(CodecClassLoader loader, String fqcn, Class<?> bindingInterface);

        /**
         * Run the specified loader in a customized environment. The environment customizations must be cleaned up by
         * the time this method returns. The default implementation performs no customization.
         *
         * @param loader Class loader to execute
         * @return Class returned by the loader
         */
        default Class<T> customizeLoading(final @NonNull Supplier<Class<T>> loader) {
            return loader.get();
        }
    }

    public static final class GeneratorResult<T> {
        private final @NonNull ImmutableSet<Class<?>> dependecies;
        private final @NonNull Unloaded<T> result;

        GeneratorResult(final Unloaded<T> result, final ImmutableSet<Class<?>> dependecies) {
            this.result = requireNonNull(result);
            this.dependecies = requireNonNull(dependecies);
        }

        public static <T> @NonNull GeneratorResult<T> of(final Unloaded<T> result) {
            return new GeneratorResult<>(result, ImmutableSet.of());
        }

        public static <T> @NonNull GeneratorResult<T> of(final Unloaded<T> result,
                final Collection<Class<?>> dependencies) {
            return dependencies.isEmpty() ? of(result) : new GeneratorResult<>(result,
                    ImmutableSet.copyOf(dependencies));
        }

        @NonNull Unloaded<T> getResult() {
            return result;
        }

        @NonNull ImmutableSet<Class<?>> getDependencies() {
            return dependecies;
        }
    }

    static {
        verify(ClassLoader.registerAsParallelCapable());
    }

    private final Logger LOG = LoggerFactory.getLogger(CodecClassLoader.class);

    CodecClassLoader(final ClassLoader parentLoader) {
        super(parentLoader);
    }

    /**
     * Instantiate a new CodecClassLoader, which serves as the root of generated code loading.
     *
     * @return A new CodecClassLoader.
     */
    public static @NonNull CodecClassLoader create() {
        return AccessController.doPrivileged((PrivilegedAction<CodecClassLoader>)() -> new RootCodecClassLoader());
    }

    /**
     * The name of the target class is formed through concatenation of the name of a {@code bindingInterface} and
     * specified {@code suffix}
     *
     * @param bindingInterface Binding compile-time-generated interface
     * @param suffix Suffix to use
     * @param generator Code generator to run
     * @return A generated class object
     * @throws NullPointerException if any argument is null
     */
    public final <T> Class<T> generateClass(final Class<?> bindingInterface,
            final String suffix, final ClassGenerator<T> generator)  {
        return findClassLoader(requireNonNull(bindingInterface)).doGenerateClass(bindingInterface, suffix, generator);
    }

    /**
     * Append specified loaders to this class loader for the purposes of looking up generated classes. Note that the
     * loaders are expected to have required classes already loaded. This is required to support generation of
     * inter-dependent structures, such as those used for streaming binding interfaces.
     *
     * @param newLoaders Loaders to append
     * @throws NullPointerException if {@code loaders} is null
     */
    abstract void appendLoaders(@NonNull Set<LeafCodecClassLoader> newLoaders);

    /**
     * Find the loader responsible for holding classes related to a binding class.
     *
     * @param bindingClass Class to locate
     * @return a Loader instance
     * @throws NullPointerException if {@code bindingClass} is null
     */
    abstract @NonNull CodecClassLoader findClassLoader(@NonNull Class<?> bindingClass);

    private <T> Class<T> doGenerateClass(final Class<?> bindingInterface, final String suffix,
            final ClassGenerator<T> generator)  {
        final String fqn = bindingInterface.getName() + "$$$" + suffix;

        synchronized (getClassLoadingLock(fqn)) {
            // Attempt to find a loaded class
            final Class<?> loaded = findLoadedClass(fqn);
            if (loaded != null) {
                return (Class<T>) loaded;
            }

            final GeneratorResult<T> result = generator.generateClass(this, fqn, bindingInterface);
            processDependencies(result.getDependencies());

            final byte[] byteCode = result.getResult().getBytes();
            return generator.customizeLoading(() -> {
                final Class<?> newClass = defineClass(fqn, byteCode, 0, byteCode.length);
                resolveClass(newClass);
                return (Class<T>) newClass;
            });
        }
    }

    private void processDependencies(final Collection<Class<?>> deps) {
        final Set<LeafCodecClassLoader> depLoaders = new HashSet<>();
        for (Class<?> dep : deps) {
            final ClassLoader depLoader = dep.getClassLoader();
            verify(depLoader instanceof CodecClassLoader, "Dependency %s is not a generated class", dep);
            if (this.equals(depLoader)) {
                // Same loader, skip
                continue;
            }

            try {
                loadClass(dep.getName());
            } catch (ClassNotFoundException e) {
                LOG.debug("Cannot find {} in local loader, attempting to compensate", dep, e);
                // Root loader is always visible from a leaf, hence the dependency can only be a leaf
                verify(depLoader instanceof LeafCodecClassLoader, "Dependency loader %s is not a leaf", depLoader);
                depLoaders.add((LeafCodecClassLoader) depLoader);
            }
        }

        if (!depLoaders.isEmpty()) {
            appendLoaders(depLoaders);
        }
    }
}
