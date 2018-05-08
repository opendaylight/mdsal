/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Map;
import java.util.WeakHashMap;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import javax.annotation.concurrent.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Users of this utility class are expected to synchronize on this instance
 * it they need to ensure atomic operations on it.
 */
public final class JavassistUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JavassistUtils.class);
    private static final Map<ClassPool, JavassistUtils> INSTANCES = new WeakHashMap<>();

    private final Map<ClassLoader, ClassPath> loaderClassPaths = new WeakHashMap<>();
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
        JavassistUtils ret = INSTANCES.get(requireNonNull(pool));
        if (ret == null) {
            ret = new JavassistUtils(pool);
            INSTANCES.put(pool, ret);
        }
        return ret;
    }

    public CtClass createClass(final String fqn, final ClassGenerator cls) throws CannotCompileException {
        CtClass target = classPool.makeClass(fqn);
        cls.process(target);
        return target;
    }

    public CtClass createClass(final String fqn, final CtClass superInterface, final ClassGenerator cls)
            throws CannotCompileException {
        CtClass target = classPool.makeClass(fqn);
        implementsType(target, superInterface);
        cls.process(target);
        return target;
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

    private static void implementsType(final CtClass it, final CtClass supertype) {
        checkArgument(supertype.isInterface(), "Supertype %s is not an interface", supertype);
        it.addInterface(supertype);
    }

    @GuardedBy("this")
    public CtClass asCtClass(final Class<?> class1) {
        return get(this.classPool, class1);
    }

    public CtField field(final CtClass it, final String name, final Class<?> returnValue) throws CannotCompileException {
        final CtField field = new CtField(asCtClass(returnValue), name, it);
        field.setModifiers(Modifier.PUBLIC);
        it.addField(field);
        return field;
    }

    public CtField staticField(final CtClass it, final String name, final Class<?> returnValue) throws CannotCompileException {
        return staticField(it, name, returnValue, null);
    }

    public CtField staticField(final CtClass it, final String name,
            final Class<?> returnValue,
            final SourceCodeGenerator sourceGenerator) throws CannotCompileException {
        final CtField field = new CtField(asCtClass(returnValue), name, it);
        field.setModifiers(Modifier.PUBLIC + Modifier.STATIC);
        it.addField(field);

        if (sourceGenerator != null) {
            sourceGenerator.appendField(field, null);
        }

        return field;
    }

    public CtClass get(final ClassPool pool, final Class<? extends Object> cls) {
        try {
            return pool.get(cls.getName());
        } catch (NotFoundException nfe1) {
            appendClassLoaderIfMissing(cls.getClassLoader());
            try {
                return pool.get(cls.getName());
            } catch (final NotFoundException nfe2) {
                LOG.warn("Appending ClassClassPath for {}", cls, nfe2);
                pool.appendClassPath(new ClassClassPath(cls));
                try {
                    return pool.get(cls.getName());
                } catch (NotFoundException e) {
                    LOG.warn("Failed to load class {} from pool {}", cls, pool, e);
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

    public void ensureClassLoader(final Class<?> child) {
        appendClassLoaderIfMissing(child.getClassLoader());
    }
}
