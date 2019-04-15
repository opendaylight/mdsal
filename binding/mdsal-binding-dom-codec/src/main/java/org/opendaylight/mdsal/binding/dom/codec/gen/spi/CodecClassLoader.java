/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.spi;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import com.google.common.base.Strings;
import java.io.IOException;
import java.lang.reflect.Modifier;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.eclipse.jdt.annotation.NonNull;

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
public abstract class CodecClassLoader extends ClassLoader {
    @FunctionalInterface
    public interface SubclassCustomizer {
        void customize(CtClass generated) throws NotFoundException;
    }

    static {
        verify(ClassLoader.registerAsParallelCapable());
    }

    private final ClassPool classPool;

    CodecClassLoader(final ClassLoader parent) {
        super(parent);
        this.classPool = new ClassPool();
        this.classPool.appendClassPath(new LoaderClassPath(this));
    }

    CodecClassLoader(final CodecClassLoader parent) {
        super(parent);
        this.classPool = new ClassPool(parent.classPool);
        this.classPool.childFirstLookup = true;
        this.classPool.appendClassPath(new LoaderClassPath(this));
    }

    public static CodecClassLoader createRoot() {
        return new RootCodecClassLoader();
    }

    public final Class<?> generateSubclass(final Class<?> superClass, final Class<?> bindingInterface,
            final String suffix, final SubclassCustomizer customizer) throws CannotCompileException, IOException,
                NotFoundException {
        return findClassLoader(bindingInterface).doGenerateSubclass(superClass, bindingInterface, suffix, customizer);
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

    private Class<?> doGenerateSubclass(final Class<?> superClass, final Class<?> bindingInterface, final String suffix,
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
