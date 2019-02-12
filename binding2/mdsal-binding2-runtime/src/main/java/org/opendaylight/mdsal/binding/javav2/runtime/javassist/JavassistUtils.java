/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.runtime.javassist;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Users of this utility class are expected to synchronize on this instance it
 * they need to ensure atomic operations on it.
 */
@Beta
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
     * @param pool
     *            - backing class pool
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

    /**
     * Generate and add method to class.
     *
     * @param baseClass
     *            - class for adding method
     * @param methodReturnType
     *            - return type of method
     * @param methodName
     *            - name of method
     * @param methodParameter
     *            - parameter of method
     * @param methodGenerator
     *            - method generator
     * @throws CannotCompileException
     */
    public void method(final CtClass baseClass, final Class<?> methodReturnType, final String methodName,
            final Class<?> methodParameter, final MethodGenerator methodGenerator) throws CannotCompileException {
        final CtClass[] pa = new CtClass[] { asCtClass(methodParameter) };
        final CtMethod method = new CtMethod(asCtClass(methodReturnType), methodName, pa, baseClass);

        methodGenerator.process(method);
        baseClass.addMethod(method);
    }

    /**
     * Generate and add method to class.
     *
     * @param baseClass
     *            - class for adding method
     * @param methodReturnType
     *            - return type of method
     * @param methodName
     *            - name of method
     * @param methodParameters
     *            - parameters of method
     * @param methodGenerator
     *            - method generator
     * @throws CannotCompileException
     */
    public void method(final CtClass baseClass, final Class<?> methodReturnType, final String methodName,
            final Collection<? extends Class<?>> methodParameters, final MethodGenerator methodGenerator)
            throws CannotCompileException {
        final CtClass[] pa = new CtClass[methodParameters.size()];

        int i = 0;
        for (final Class<?> parameter : methodParameters) {
            pa[i] = asCtClass(parameter);
            ++i;
        }

        final CtMethod method = new CtMethod(asCtClass(methodReturnType), methodName, pa, baseClass);
        methodGenerator.process(method);
        baseClass.addMethod(method);
    }

    /**
     * Generate and add static method to class.
     *
     * @param baseClass
     *            - class for adding method
     * @param methodReturnType
     *            - return type of method
     * @param methodName
     *            - name of method
     * @param methodParameter
     *            - parameter of method
     * @param methodGenerator
     *            - method generator
     * @throws CannotCompileException
     */
    public void staticMethod(final CtClass baseClass, final Class<?> methodReturnType, final String methodName,
            final Class<?> methodParameter, final MethodGenerator methodGenerator) throws CannotCompileException {
        final CtClass[] pa = new CtClass[] { asCtClass(methodParameter) };
        final CtMethod method = new CtMethod(asCtClass(methodReturnType), methodName, pa, baseClass);
        methodGenerator.process(method);
        baseClass.addMethod(method);
    }

    /**
     * Implement methods to class from other class.
     *
     * @param target
     *            - class for implementing methods
     * @param source
     *            - source class of methods to be implemented in target
     * @param methodGenerator
     *            - method generator
     * @throws CannotCompileException
     */
    public void implementMethodsFrom(final CtClass target, final CtClass source, final MethodGenerator methodGenerator)
            throws CannotCompileException {
        for (final CtMethod method : source.getMethods()) {
            if (method.getDeclaringClass() == source) {
                final CtMethod redeclaredMethod = new CtMethod(method, target, null);
                methodGenerator.process(redeclaredMethod);
                target.addMethod(redeclaredMethod);
            }
        }
    }

    /**
     * Generate and add class to global class pool.
     *
     * @param className
     *            - name of class
     * @param classGenerator
     *            - class generator
     * @return generated class
     * @throws CannotCompileException
     */
    public CtClass createClass(final String className, final ClassGenerator classGenerator) throws CannotCompileException {
        final CtClass target = classPool.makeClass(className);
        classGenerator.process(target);
        return target;
    }

    /**
     * Generate and add class to global class pool with implemented interface.
     *
     * @param className
     *            - name of class
     * @param superInterface
     *            - interface to be implemented to the class
     * @param classGenerator
     *            - class generator
     * @return generated class with interface
     * @throws CannotCompileException
     */
    public CtClass createClass(final String className, final CtClass superInterface, final ClassGenerator classGenerator)
            throws CannotCompileException {
        final CtClass target = classPool.makeClass(className);
        implementsType(target, superInterface);
        classGenerator.process(target);
        return target;
    }

    /**
     * Instantiate a new class based on a prototype. The class is set to
     * automatically prune.
     *
     * @param prototype
     *            - prototype class fully qualified name
     * @param target
     *            - target class fully qualified name
     * @param customizer
     *            - customization callback to be invoked on the new class
     * @return An instance of the new class
     * @throws NotFoundException
     *             - when the prototype class is not found
     */
    public synchronized CtClass instantiatePrototype(final String prototype, final String target,
            final ClassCustomizer customizer) throws NotFoundException {
        final CtClass result = classPool.getAndRename(prototype, target);

        try {
            customizer.customizeClass(result);
        } catch (final Exception e) {
            LOG.warn("Failed to customize {} from prototype {}", target, prototype, e);
            result.detach();
            throw new IllegalStateException(String.format("Failed to instantiate prototype %s as %s", prototype, target),
                    e);
        }

        result.stopPruning(false);
        return result;
    }

    /**
     * Implements type to class.
     *
     * @param baseClass
     *            - class for implements interface
     * @param superInterface
     *            - interface to be implemented
     */
    public void implementsType(final CtClass baseClass, final CtClass superInterface) {
        Preconditions.checkArgument(superInterface.isInterface(), "Supertype must be interface");
        baseClass.addInterface(superInterface);
    }

    /**
     * Get class from class pool.
     *
     * @param class1
     *            - class for getting from class pool
     * @return class
     */
    public CtClass asCtClass(final Class<?> class1) {
        return get(this.classPool, class1);
    }

    /**
     * Create and add field to class.
     *
     * @param baseClass
     *            - class for adding field
     * @param fieldName
     *            - name of field
     * @param fieldType
     *            - type of field
     * @return class of field
     * @throws CannotCompileException
     */
    public CtField field(final CtClass baseClass, final String fieldName, final Class<?> fieldType)
            throws CannotCompileException {
        final CtField field = new CtField(asCtClass(fieldType), fieldName, baseClass);
        field.setModifiers(Modifier.PUBLIC);
        baseClass.addField(field);
        return field;
    }

    /**
     * Create and add static field to class.
     *
     * @param baseClass
     *            - class for adding field
     * @param fieldName
     *            - name of field
     * @param fieldType
     *            - type of field
     * @return class of field
     * @throws CannotCompileException
     */
    public CtField staticField(final CtClass baseClass, final String fieldName, final Class<?> fieldType)
            throws CannotCompileException {
        return staticField(baseClass, fieldName, fieldType, null);
    }

    /**
     * Create and add static field to class.
     *
     * @param baseClass
     *            - class for adding field
     * @param fieldName
     *            - name of field
     * @param fieldType
     *            - type of field
     * @param sourceGenerator
     *            - source generator
     * @return class of field
     * @throws CannotCompileException
     */
    public CtField staticField(final CtClass baseClass, final String fieldName, final Class<?> fieldType,
            final SourceCodeGenerator sourceGenerator) throws CannotCompileException {
        final CtField field = new CtField(asCtClass(fieldType), fieldName, baseClass);
        field.setModifiers(Modifier.PUBLIC + Modifier.STATIC);
        baseClass.addField(field);

        if (sourceGenerator != null) {
            sourceGenerator.appendField(field, null);
        }

        return field;
    }

    /**
     * Get class from pool.
     *
     * @param pool
     *            - class pool
     * @param clazz
     *            - search class in class pool
     * @return class if exists
     */
    public CtClass get(final ClassPool pool, final Class<? extends Object> clazz) {
        try {
            return pool.get(clazz.getName());
        } catch (final NotFoundException nfe1) {
            appendClassLoaderIfMissing(clazz.getClassLoader());
            try {
                return pool.get(clazz.getName());
            } catch (final NotFoundException nfe2) {
                LOG.warn("Appending ClassClassPath for {}", clazz, nfe2);
                pool.appendClassPath(new ClassClassPath(clazz));
                try {
                    return pool.get(clazz.getName());
                } catch (final NotFoundException e) {
                    LOG.warn("Failed to load class {} from pool {}", clazz, pool, e);
                    throw new IllegalStateException("Failed to load class", e);
                }
            }
        }
    }

    /**
     * Append class to class pool if doesn't exist.
     *
     * @param loader
     *            - class loader of search class
     */
    public synchronized void appendClassLoaderIfMissing(final ClassLoader loader) {
        if (!loaderClassPaths.containsKey(loader)) {
            final ClassPath ctLoader = new LoaderClassPath(loader);
            classPool.appendClassPath(ctLoader);
            loaderClassPaths.put(loader, ctLoader);
        }
    }

    /**
     * Ensure if is class in class loader.
     *
     * @param child
     *            - search class
     */
    public void ensureClassLoader(final Class<?> child) {
        appendClassLoaderIfMissing(child.getClassLoader());
    }
}
