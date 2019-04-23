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
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Builder;
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
    /**
     * A customizer allowing a generated class to be modified before it is loader.
     */
    public interface ByteBuddyCustomizer {
        /**
         * Customize a generated class before it is instantiated in the loader.
         *
         * @param loader CodecClassLoader which will hold the class. It can be used to lookup/instantiate other classes
         * @param bindingInterface Binding class for which the customized class is being generated
         * @param fqn Fully-qualified Class Name of generated class
         * @param builder ByteBuddy builder
         * @return A set of generated classes the generated class depends on
         */
        <T> @NonNull ByteBuddyResult<T> customize(@NonNull CodecClassLoader loader, @NonNull Class<?> bindingInterface,
                @NonNull String fqn, @NonNull Builder<T> builder);

        /**
         * Run the specified loader in a customized environment. The environment customizations must be cleaned up by
         * the time this method returns. The default implementation performs no customization.
         *
         * @param loader Class loader to execute
         * @return Class returned by the loader
         */
        default Class<?> customizeLoading(final @NonNull Supplier<Class<?>> loader) {
            return loader.get();
        }
    }

    public static final class ByteBuddyResult<T> {
        private final @NonNull ImmutableSet<Class<?>> dependecies;
        private final @NonNull Unloaded<T> result;

        ByteBuddyResult(final Unloaded<T> result, final ImmutableSet<Class<?>> dependecies) {
            this.result = requireNonNull(result);
            this.dependecies = requireNonNull(dependecies);
        }

        public static <T> @NonNull ByteBuddyResult<T> of(final Unloaded<T> result) {
            return new ByteBuddyResult<>(result, ImmutableSet.of());
        }

        public static <T> @NonNull ByteBuddyResult<T> of(final Unloaded<T> result,
                final Collection<Class<?>> dependencies) {
            return dependencies.isEmpty() ? of(result) : new ByteBuddyResult<>(result,
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

    private static final Logger LOG = LoggerFactory.getLogger(CodecClassLoader.class);

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
     * Create a new class by subclassing specified class and running a customizer on it. The name of the target class
     * is formed through concatenation of the name of a {@code bindingInterface} and specified {@code suffix}
     *
     * @param superClass Superclass from which to derive
     * @param bindingInterface Binding compile-time-generated interface
     * @param suffix Suffix to use
     * @param customizer Customizer to use to process the class
     * @return A generated class object
     * @throws NullPointerException if any argument is null
     */
    public final <T> Class<T> generateSubclass(final Class<? extends T> superClass, final Class<?> bindingInterface,
            final String suffix, final ByteBuddyCustomizer customizer)  {
        return findClassLoader(requireNonNull(bindingInterface))
                .doGenerateSubclass(superClass, bindingInterface, suffix, customizer);
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

    private <T> Class<T> doGenerateSubclass(final Class<? extends T> superClass,
            final Class<?> bindingInterface, final String suffix, final ByteBuddyCustomizer customizer)  {
        final String bindingName = bindingInterface.getName();
        final String fqn = bindingName + "$$$" + suffix;

        synchronized (getClassLoadingLock(fqn)) {
            // Attempt to find a loaded class
            final Class<?> loaded = findLoadedClass(fqn);
            if (loaded != null) {
                return (Class<T>) loaded;
            }

            final ByteBuddyResult<? extends T> result = customizer.customize(this, bindingInterface, fqn,
                new ByteBuddy().subclass(superClass).name(fqn));
            processDependencies(result.getDependencies());

            final byte[] byteCode = result.getResult().getBytes();

            return (Class<T>) customizer.customizeLoading(() -> {
                final Class<?> newClass = defineClass(fqn, byteCode, 0, byteCode.length);
                resolveClass(newClass);
                return newClass;
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
