/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader.Customizer;
import org.opendaylight.mdsal.binding.dom.codec.loader.StaticClassPool;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Private support for generating AbstractDataObject specializations.
 */
final class CodecDataObjectCustomizer implements Customizer {
    private abstract static class FieldStrategy {

        abstract String generate(CtClass generated, String methodName, final String returnTypeName)
                throws CannotCompileException;
    }

    /**
     * AtomicReferenceFieldUpdater-based strategy. Worst-case scenario involves a volatile read, a compare-and-swap and
     * another volatile read. Concurrent invocation guarantees same-object return. Cache fills are completely concurrent
     * and lock-free. This comes at the cost of having an AtomicReferenceFieldUpdater instantiated for each field.
     */
    // FIXME: add a Java 9+ VarHandle-based strategy and evaluate its performance/memory footprint against ARFU
    private static final class Atomic extends FieldStrategy {
        private static final CtClass CT_ARFU = StaticClassPool.findClass(AtomicReferenceFieldUpdater.class);

        @Override
        String generate(final CtClass generated, final String methodName, final String returnTypeName)
                throws CannotCompileException {
            final String methodArfu = methodName + "$$$ARFU";

            // AtomicReferenceFieldUpdater ...
            final CtField arfuField = new CtField(CT_ARFU, methodArfu, generated);
            arfuField.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
            generated.addField(arfuField, new StringBuilder().append(CT_ARFU.getName()).append(".newUpdater(")
                .append(generated.getName()).append(".class, java.lang.Object.class, \"").append(methodName)
                .append("\")").toString());

            return new StringBuilder()
                    .append("{\n")
                    .append("final java.lang.Object cached = ").append(methodName).append(";\n")
                    .append("final java.lang.Object result;\n")
                    .append("if (cached == null) {\n")
                    .append("    result = codecMember(\"").append(methodName).append("\");\n")
                    .append("    if (!").append(methodArfu)
                        .append(".compareAndSet(this, null, codecMaskNull(result))) {\n")
                    // Unlikely event, just re-invoke the same method, which will fast-path
                    .append("        return ").append(methodName).append("();\n")
                    .append("    }\n")
                    .append("} else {\n")
                    .append("    result = codecUnmaskNull(cached);\n")
                    .append("}\n")
                    .append("return (").append(returnTypeName).append(") result;\n")
                    .append('}').toString();
        }
    }

    /**
     * Double-checked-lock-based strategy. The worst-case scenario involves an volatile read, a lock, a volatile read
     * and a volatile write. Concurrent invocation guarantees same-object return. Cache fills perform concurrent
     * computation, but field updates are synchronized.
     */
    private static final class Concurrent extends FieldStrategy {
        @Override
        String generate(final CtClass generated, final String methodName, final String returnTypeName) {
            return new StringBuilder()
                    .append("{\n")
                    .append("final java.lang.Object cached = ").append(methodName).append(";\n")
                    .append("java.lang.Object result;\n")
                    .append("if (cached == null) {\n")
                    .append("    result = codecMember(\"").append(methodName).append("\");\n")
                    .append("    synchronized (this) {\n")
                    .append("        final java.lang.Object recheck =").append(methodName).append(";\n")
                    .append("        if (recheck == null) {\n")
                    .append("            ").append(methodName).append(" = codecMaskNull(result);\n")
                    .append("        } else {\n")
                    .append("            result = codecUnmaskNull(recheck);\n")
                    .append("        }\n")
                    .append("    }\n")
                    .append("} else {\n")
                    .append("    result = codecUnmaskNull(cached);\n")
                    .append("}\n")
                    .append("return (").append(returnTypeName).append(") result;\n")
                    .append('}').toString();
        }
    }

    /**
     * Double-check-lock-based strategy. The worst-case scenario involves an volatile read, a lock, a volatile read
     * and a volatile write. Concurrent invocation guarantees same-object return. Unlike {@link Concurrent} strategy,
     * cache fills are completely synchronized.
     */
    private static final class Exclusive extends FieldStrategy {
        @Override
        String generate(final CtClass generated, final String methodName, final String returnTypeName) {
            return new StringBuilder()
                    .append("{\n")
                    .append("final java.lang.Object cached = ").append(methodName).append(";\n")
                    .append("java.lang.Object result;\n")
                    .append("if (cached == null) {\n")
                    .append("    synchronized (this) {\n")
                    .append("        final java.lang.Object recheck =").append(methodName).append(";\n")
                    .append("        if (recheck == null) {\n")
                    .append("            result = codecMember(\"").append(methodName).append("\");\n")
                    .append("            ").append(methodName).append(" = codecMaskNull(result);\n")
                    .append("        } else {\n")
                    .append("            result = codecUnmaskNull(recheck);\n")
                    .append("        }\n")
                    .append("    }\n")
                    .append("} else {\n")
                    .append("    result = codecUnmaskNull(cached);\n")
                    .append("}\n")
                    .append("return (").append(returnTypeName).append(") result;\n")
                    .append('}').toString();
        }
    }

    /**
     * Simple optimistic strategy. The worst-case scenario involves a volatile read and a volatile write. Concurrent
     * invocation DOES NOT guarantee same-object return and the cache will be populated by one of the computed objects.
     * Cache fills are completely concurrent and lock-free.
     */
    private static final class Simple extends FieldStrategy {
        @Override
        String generate(final CtClass generated, final String methodName, final String returnTypeName) {
            return new StringBuilder()
                    .append("{\n")
                    .append("final java.lang.Object cached = ").append(methodName).append(";\n")
                    .append("final java.lang.Object result;\n")
                    .append("if (cached == null) {\n")
                    .append("    result = codecMember(\"").append(methodName).append("\");\n")
                    .append("    ").append(methodName).append(" = codecMaskNull(result);\n")
                    .append("} else {\n")
                    .append("    result = codecUnmaskNull(cached);\n")
                    .append("}\n")
                    .append("return (").append(returnTypeName).append(") result;\n")
                    .append('}').toString();
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(CodecDataObjectCustomizer.class);
    private static final CtClass CT_BOOLEAN = StaticClassPool.findClass(boolean.class);
    private static final CtClass CT_INT = StaticClassPool.findClass(int.class);
    private static final CtClass CT_OBJECT = StaticClassPool.findClass(Object.class);
    private static final CtClass CT_HELPER = StaticClassPool.findClass(ToStringHelper.class);
    private static final CtClass CT_DATAOBJECT = StaticClassPool.findClass(DataObject.class);
    private static final CtClass[] EMPTY_ARGS = new CtClass[0];
    private static final CtClass[] EQUALS_ARGS = new CtClass[] { CT_DATAOBJECT };
    private static final CtClass[] TOSTRING_ARGS = new CtClass[] { CT_HELPER };

    private static final FieldStrategy STRATEGY = getStrategy();

    private static FieldStrategy getStrategy() {
        String stratString = System.getProperty("org.opendaylight.mdsal.binding.dom.codec.impl.field-strategy");
        if (stratString == null) {
            stratString = "atomic";
        }
        switch (stratString) {
            case "atomic":
                return new Atomic();
            case "concurrent":
                return new Concurrent();
            case "exclusive":
                return new Exclusive();
            case "simple":
                return new Simple();
            default:
                LOG.warn("Unknown strategy {}, defaulting to atomic");
                return new Atomic();
        }
    }

    private final List<Method> properties;
    private final List<Method> methods;

    CodecDataObjectCustomizer(final List<Method> properties, final List<Method> methods) {
        this.properties = requireNonNull(properties);
        this.methods = requireNonNull(methods);
    }

    @Override
    public void customize(final CodecClassLoader loader, final CtClass bindingClass, final CtClass generated)
            throws NotFoundException, CannotCompileException {
        final String classFqn = generated.getName();
        generated.addInterface(bindingClass);

        // Generate members for all methods ...
        LOG.trace("Generating class {}", classFqn);
        for (Method method : methods) {
            LOG.trace("Generating for method {}", method);
            final String methodName = method.getName();

            // corresponding volatile field ...
            final CtField field = new CtField(CT_OBJECT, methodName, generated);
            field.setModifiers(Modifier.PRIVATE | Modifier.VOLATILE);
            generated.addField(field);

            // ... and the getter
            final CtMethod getter = new CtMethod(loader.findClass(method.getReturnType()), methodName,
                EMPTY_ARGS, generated);
            final Class<?> retType = method.getReturnType();
            final String retName = retType.isArray() ? retType.getSimpleName() : retType.getName();

            getter.setBody(STRATEGY.generate(generated, methodName, retName));
            generated.addMethod(getter);
        }

        // Final bits: codecHashCode() ...
        final CtMethod codecHashCode = new CtMethod(CT_INT, "codecHashCode", EMPTY_ARGS, generated);
        codecHashCode.setModifiers(Modifier.PROTECTED | Modifier.FINAL);
        codecHashCode.setBody(codecHashCodeBody());
        generated.addMethod(codecHashCode);

        // ... equals
        final CtMethod codecEquals = new CtMethod(CT_BOOLEAN, "codecEquals", EQUALS_ARGS, generated);
        codecEquals.setModifiers(Modifier.PROTECTED | Modifier.FINAL);
        codecEquals.setBody(codecEqualsBody(bindingClass.getName()));
        generated.addMethod(codecEquals);

        // ... and codecFillToString()
        final CtMethod codecFillToString = new CtMethod(CT_HELPER, "codecFillToString", TOSTRING_ARGS, generated);
        codecFillToString.setModifiers(Modifier.PROTECTED | Modifier.FINAL);
        codecFillToString.setBody(codecFillToStringBody());
        generated.addMethod(codecFillToString);

        generated.setModifiers(Modifier.FINAL | Modifier.PUBLIC);
    }

    private String codecHashCodeBody() {
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("final int prime = 31;\n")
                .append("int result = 1;\n");

        for (Method method : properties) {
            sb.append("result = prime * result + java.util.").append(utilClass(method)).append(".hashCode(")
            .append(method.getName()).append("());\n");
        }

        return sb.append("return result;\n")
                .append('}').toString();
    }

    private String codecEqualsBody(final String ifaceName) {
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("final ").append(ifaceName).append(" other = $1;")
                .append("return true");

        for (Method method : properties) {
            final String methodName = method.getName();
            sb.append("\n&& java.util.").append(utilClass(method)).append(".equals(").append(methodName)
            .append("(), other.").append(methodName).append("())");
        }

        return sb.append(";\n")
                .append('}').toString();
    }

    private String codecFillToStringBody() {
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("return $1");
        for (Method method : properties) {
            final String methodName = method.getName();
            sb.append("\n.add(\"").append(methodName).append("\", ").append(methodName).append("())");
        }

        return sb.append(";\n")
                .append('}').toString();
    }

    private static String utilClass(final Method method) {
        // We can either have objects or byte[], we cannot have Object[]
        return method.getReturnType().isArray() ? "Arrays" : "Objects";
    }
}