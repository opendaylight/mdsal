/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ClassLoader hosting types generated for a particular type. A root instance is attached to a
 * {@link BindingCodecContext} instance, so any generated classes from it can be garbage-collected when the context
 * is destroyed, as well as to prevent two contexts trampling over each other.
 *
 * <p>
 * It semantically combines two class loaders: the class loader in which this class is loaded and the class loader in
 * which a target Binding interface/class is loaded. This inherently supports multi-classloader environments -- the root
 * instance has visibility only into codec classes and for each classloader we encounter when presented with a binding
 * class we create a leaf instance and cache it in the root instance. Leaf instances are using the root loader as their
 * parent, but consult the binding class's class loader if the root loader fails to load a particular class.
 *
 * <p>
 * In single-classloader environments, obviously, the root loader can load all binding classes, and hence no leaf loader
 * is created.
 *
 * <p>
 * Each {@link CodecClassLoader} has a {@link ClassPool} attached to it and can perform operations on it. Leaf loaders
 * specify the root loader's ClassPool as their parent, but are configured to lookup classes first in themselves.
 *
 * @author Robert Varga
 */
abstract class CodecClassLoader extends ClassLoader {
    @FunctionalInterface
    interface SubclassCustomizer {
        void customize(CtClass generated) throws NotFoundException;
    }

    private static final class Root extends CodecClassLoader {
        static {
            verify(registerAsParallelCapable());
        }

        @SuppressWarnings("rawtypes")
        private static final AtomicReferenceFieldUpdater<Root, ImmutableMap> LOADERS_UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(Root.class, ImmutableMap.class, "loaders");

        private volatile ImmutableMap<ClassLoader, CodecClassLoader> loaders = ImmutableMap.of();

        Root() {
            super(CodecClassLoader.class.getClassLoader());
        }

        @Override
        CodecClassLoader findClassLoader(final Class<?> bindingClass) {
            final ClassLoader target = bindingClass.getClassLoader();

            // Cache for update
            ImmutableMap<ClassLoader, CodecClassLoader> local = loaders;
            final CodecClassLoader known = local.get(target);
            if (known != null) {
                return known;
            }

            // Alright, we need to determine if the class is accessible through our hierarchy (in which case we use
            // ourselves) or we need to create a new Leaf.
            final CodecClassLoader found;
            if (!isOurClass(bindingClass)) {
                verifyClassLoader(target);
                found = new Leaf(this, target);
            } else {
                found = this;
            }

            // Now make sure we cache this result
            while (true) {
                final Builder<ClassLoader, CodecClassLoader> builder = ImmutableMap.builderWithExpectedSize(
                    local.size() + 1);
                builder.putAll(local);
                builder.put(target, found);

                if (LOADERS_UPDATER.compareAndSet(this, local, builder.build())) {
                    return found;
                }

                local = loaders;
                final CodecClassLoader recheck = local.get(target);
                if (recheck != null) {
                    return recheck;
                }
            }
        }

        @Override
        CtClass getRootFrozen(final String name) throws NotFoundException {
            return getLocalFrozen(name);
        }

        private boolean isOurClass(final Class<?> bindingClass) {
            final Class<?> ourClass;
            try {
                ourClass = loadClass(bindingClass.getName(), false);
            } catch (ClassNotFoundException e) {
                LOG.debug("Failed to load class {}", e);
                return false;
            }
            return bindingClass.equals(ourClass);
        }

        private static void verifyClassLoader(final ClassLoader target) {
            // Sanity check: target has to resolve yang-binding contents to the same class, otherwise we are in a pickle
            final Class<?> targetClazz;
            try {
                targetClazz = target.loadClass(DataContainer.class.getName());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("ClassLoader " + target + " cannot load " + DataContainer.class, e);
            }
            verify(DataContainer.class.equals(targetClazz),
                "Class mismatch on DataContainer. Ours is from %s, target %s has %s from %s",
                DataContainer.class.getClassLoader(), target, targetClazz, targetClazz.getClassLoader());
        }
    }

    private static final class Leaf extends CodecClassLoader {
        static {
            verify(registerAsParallelCapable());
        }

        private final @NonNull ClassLoader target;
        private final @NonNull Root root;

        Leaf(final Root root, final ClassLoader target) {
            super(root);
            this.root = requireNonNull(root);
            this.target = requireNonNull(target);

        }

        @Override
        protected Class<?> findClass(final String name) throws ClassNotFoundException {
            return target.loadClass(name);
        }

        @Override
        CodecClassLoader findClassLoader(final Class<?> bindingClass) {
            final ClassLoader bindingTarget = bindingClass.getClassLoader();
            return target.equals(bindingTarget) ? this : root.findClassLoader(bindingClass);
        }

        @Override
        CtClass getRootFrozen(final String name) throws NotFoundException {
            return root.getLocalFrozen(name);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(CodecClassLoader.class);

    static {
        verify(ClassLoader.registerAsParallelCapable());
    }

    private final ClassPool classPool;

    private CodecClassLoader(final ClassLoader parent) {
        super(parent);
        this.classPool = new ClassPool();
        this.classPool.appendClassPath(new LoaderClassPath(this));
    }

    private CodecClassLoader(final CodecClassLoader parent) {
        super(parent);
        this.classPool = new ClassPool(parent.classPool);
        this.classPool.childFirstLookup = true;
        this.classPool.appendClassPath(new LoaderClassPath(this));
    }

    static CodecClassLoader createRoot() {
        return new Root();
    }

    abstract @NonNull CodecClassLoader findClassLoader(Class<?> bindingClass);

    abstract @NonNull CtClass getRootFrozen(String name) throws NotFoundException;

    final @NonNull CtClass getLocalFrozen(final String name) throws NotFoundException {
        synchronized (getClassLoadingLock(name)) {
            final CtClass result = classPool.get(name);
            result.freeze();
            return result;
        }
    }

    final Class<?> generateSubclass(final Class<?> superClass, final Class<?> bindingInterface, final String suffix,
            final SubclassCustomizer customizer) throws CannotCompileException, IOException, NotFoundException {
        checkArgument(!superClass.isInterface(), "%s must not be an interface", superClass);
        checkArgument(bindingInterface.isInterface(), "%s is not an interface", bindingInterface);
        checkArgument(!Strings.isNullOrEmpty(suffix));

        final String bindingName = bindingInterface.getName();
        final String fqn = bindingName + "$$$" + suffix;
        synchronized (getClassLoadingLock(fqn)) {
            // Get the superclass
            final CtClass superCt = getRootFrozen(superClass.getName());

            // Get the interface
            final CtClass bindingCt = getLocalFrozen(bindingName);
            try {
                final byte[] byteCode;
                final CtClass generated = classPool.makeClass(fqn, superCt);
                try {
                    generated.setModifiers(Modifier.FINAL | Modifier.PUBLIC);
                    generated.addInterface(bindingCt);
                    customizer.customize(generated);

                    final String ctName = generated.getName();
                    verify(fqn.equals(ctName), "Target class is %s returned result is %s", fqn, ctName);
                    byteCode = generated.toBytecode();
                } finally {
                    // Always detach the result, as we will never use it again
                    generated.detach();
                }

                final Class<?> newClass = defineClass(fqn, byteCode, 0, byteCode.length);
                resolveClass(newClass);
                return newClass;
            } finally {
                // Binding interfaces are used only a few times, hence it does not make sense to cache them in the class
                // pool.
                bindingCt.detach();
            }
        }
    }
}
