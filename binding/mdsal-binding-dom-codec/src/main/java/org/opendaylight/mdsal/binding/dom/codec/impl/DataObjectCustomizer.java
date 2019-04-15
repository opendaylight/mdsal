/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
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
import org.opendaylight.mdsal.binding.dom.codec.gen.spi.CodecClassLoader;
import org.opendaylight.mdsal.binding.dom.codec.gen.spi.CodecClassLoader.SubclassCustomizer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Private support for generating AbstractDataObject specializations.
 */
final class DataObjectCustomizer implements SubclassCustomizer {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectCustomizer.class);
    private static final CtClass[] EMPTY_ARGS = new CtClass[0];

    private final List<Method> properties;
    private final List<Method> methods;

    DataObjectCustomizer(final List<Method> properties, final List<Method> methods) {
        this.properties = requireNonNull(properties);
        this.methods = requireNonNull(methods);
    }

    @Override
    // FIXME: optimize to lower the use compilation
    public void customize(final CodecClassLoader loader, final CtClass generated) throws NotFoundException,
            CannotCompileException {
        final CtClass[] ifaces = generated.getInterfaces();
        verify(ifaces.length == 1);
        final CtClass iface = ifaces[0];

        final CtClass ctArfu = loader.findCodecClass(AtomicReferenceFieldUpdater.class);
        final CtClass ctBoolean = loader.findCodecClass(boolean.class);
        final CtClass ctInt = loader.findCodecClass(int.class);
        final CtClass ctObject = loader.findCodecClass(Object.class);
        final CtClass ctHelper = loader.findCodecClass(ToStringHelper.class);
        final CtClass ctDataObject = loader.findCodecClass(DataObject.class);
        final String classFqn = generated.getName();

        // Generate members for all methods ...
        LOG.trace("Generating class {}", classFqn);
        for (Method method : methods) {
            LOG.trace("Generating for method {}", method);

            final String methodName = method.getName();
            final String methodArfu = arfuName(methodName);

            // AtomicReferenceFieldUpdater ...
            final CtField arfuField = new CtField(ctArfu, methodArfu, generated);
            arfuField.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
            generated.addField(arfuField, new StringBuilder().append(ctArfu.getName()).append(".newUpdater(")
                .append(classFqn).append(".class, java.lang.Object.class, \"").append(methodName).append("\")")
                .toString());

            // ... corresponding volatile field ...
            final CtField field = new CtField(ctObject, methodName, generated);
            field.setModifiers(Modifier.PRIVATE | Modifier.VOLATILE);
            generated.addField(field);

            // ... and the getter
            final CtMethod getter = new CtMethod(loader.findBindingClass(method.getReturnType()), methodName,
                EMPTY_ARGS, generated);
            final Class<?> retType = method.getReturnType();
            final String retName = retType.isArray() ? retType.getSimpleName() : retType.getName();

            getter.setBody(new StringBuilder()
                .append("{\n")
                .append("final java.lang.Object cached = ").append(methodArfu).append(".get(this);\n")
                .append("final java.lang.Object result;\n")
                .append("if (cached == null) {\n")
                .append("    result = codecMember(\"").append(methodName).append("\");\n")
                .append("    if (!").append(methodArfu).append(".compareAndSet(this, null, codecMaskNull(result))) {\n")
                // Unlikely event, just re-invoke the same method, which will fast-path
                .append("        return ").append(methodName).append("();\n")
                .append("    }\n")
                .append("} else {\n")
                .append("    result = codecUnmaskNull(cached);\n")
                .append("}\n")
                .append("return (").append(retName).append(") result;\n")
                .append('}').toString());
            getter.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
            generated.addMethod(getter);
        }

        // Final bits: codecHashCode() ...
        final CtMethod codecHashCode = new CtMethod(ctInt, "codecHashCode", EMPTY_ARGS, generated);
        codecHashCode.setModifiers(Modifier.PROTECTED | Modifier.FINAL);
        codecHashCode.setBody(codecHashCodeBody());
        generated.addMethod(codecHashCode);

        // ... equals
        final CtMethod codecEquals = new CtMethod(ctBoolean, "codecEquals", new CtClass[] { ctDataObject }, generated);
        codecEquals.setModifiers(Modifier.PROTECTED | Modifier.FINAL);
        codecEquals.setBody(codecEqualsBody(iface.getName()));
        generated.addMethod(codecEquals);

        // ... and codecFillToString()
        final CtMethod codecFillToString = new CtMethod(ctHelper, "codecFillToString", new CtClass[] { ctHelper },
            generated);
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
            final String methodName = method.getName();
            sb.append("result = prime * result + java.util.Objects.hashCode(").append(methodName).append("());\n");
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
            // We can either have objects or byte[], we cannot have Object[]
            final String equality = method.getReturnType().isArray() ? "java.util.Arrays.equals("
                    : "java.util.Objects.equals(";
            sb.append("\n&& ").append(equality).append(methodName).append("(), other.").append(methodName)
            .append("())");
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

    private static String arfuName(final String methodName) {
        return methodName + "$$$ARFU";
    }
}