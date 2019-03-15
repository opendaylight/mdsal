/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Map;
import java.util.WeakHashMap;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Users of this utility class are expected to synchronize on this instance it they need to ensure atomic operations
 * on it. Individual operations are synchronized and therefore are thread-safe.
 */
@NonNullByDefault
public final class JavassistUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JavassistUtils.class);
    private static final Map<ClassPool, JavassistUtils> INSTANCES = new WeakHashMap<>();

    @GuardedBy("this")
    private final Map<ClassLoader, ClassPath> loaderClassPaths = new WeakHashMap<>();
    @GuardedBy("this")
    private final ClassPool classPool;

    private JavassistUtils(final ClassPool pool) {
        classPool = requireNonNull(pool);
    }

    /**
     * Get a utility instance for a particular class pool. A new instance is
     * created if this is a new pool. If an instance already exists, is is
     * returned.
     *
     * @param pool Backing class pool
     * @return shared utility instance for specified pool
     */
    public static synchronized JavassistUtils forClassPool(final ClassPool pool) {
        return INSTANCES.computeIfAbsent(requireNonNull(pool), JavassistUtils::new);
    }

    /**
     * Instantiate a new class based on a prototype. The class is set to automatically prune. The {@code customizer}
     * is guaranteed to run with this object locked.
     *
     * @param prototype Prototype class fully qualified name
     * @param fqn Target class fully qualified name
     * @param customizer Customization callback to be invoked on the new class
     * @return An instance of the new class
     * @throws NotFoundException when the prototype class is not found
     */
    @Beta
    @SuppressWarnings("checkstyle:illegalCatch")
    public synchronized CtClass instantiatePrototype(final String prototype, final String fqn,
            final ClassCustomizer customizer) throws CannotCompileException, NotFoundException {
        final CtClass result = classPool.getAndRename(prototype, fqn);
        try {
            customizer.customizeClass(result);
        } catch (CannotCompileException | NotFoundException e) {
            result.detach();
            throw e;
        } catch (Exception e) {
            LOG.warn("Failed to customize {} from prototype {}", fqn, prototype, e);
            result.detach();
            throw new IllegalStateException(String.format("Failed to instantiate prototype %s as %s", prototype, fqn),
                e);
        }

        result.stopPruning(false);
        return result;
    }

    @GuardedBy("this")
    public CtClass asCtClass(final Class<?> cls) {
        try {
            return classPool.get(cls.getName());
        } catch (NotFoundException nfe1) {
            appendClassLoaderIfMissing(cls.getClassLoader());
            try {
                return classPool.get(cls.getName());
            } catch (final NotFoundException nfe2) {
                LOG.warn("Appending ClassClassPath for {}", cls, nfe2);
                classPool.appendClassPath(new ClassClassPath(cls));
                try {
                    return classPool.get(cls.getName());
                } catch (NotFoundException e) {
                    LOG.warn("Failed to load class {} from pool {}", cls, classPool, e);
                    throw new IllegalStateException("Failed to load class", e);
                }
            }
        }
    }

    public synchronized void appendClassLoaderIfMissing(final ClassLoader loader) {
        if (!loaderClassPaths.containsKey(loader)) {
            final ClassPath ctLoader = new LoaderClassPath(loader);
            classPool.appendClassPath(ctLoader);
            loaderClassPaths.put(loader, ctLoader);
        }
    }
}
