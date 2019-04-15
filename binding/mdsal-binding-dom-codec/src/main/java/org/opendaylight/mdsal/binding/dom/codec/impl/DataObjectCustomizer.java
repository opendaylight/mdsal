package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import org.opendaylight.mdsal.binding.dom.codec.gen.spi.CodecClassLoader.SubclassCustomizer;

/**
 * Private support for generating AbstractDataObject specializations.
 */
final class DataObjectCustomizer implements SubclassCustomizer {
    private final Method[] methods;
    private final String classFqn;

    DataObjectCustomizer(final String classFqn, final Method[] methods) {
        this.classFqn = requireNonNull(classFqn);
        this.methods = requireNonNull(methods);
    }

    @Override
    // FIXME: optimize to lower the use compilation
    public void customize(final ClassPool pool, final CtClass generated) throws NotFoundException,
            CannotCompileException {
        final String arfu = AtomicReferenceFieldUpdater.class.getName();

        // Generate members for all methods ...
        for (Method method : methods) {
            final String methodName = method.getName();
            final String methodArfu = arfuName(methodName);
            final String retName = method.getReturnType().getName();

            // AtomicReferenceFieldUpdater ...
            generated.addField(CtField.make(new StringBuilder()
                .append("private static final ").append(methodArfu).append('<').append(classFqn)
                .append(", java.lang.Object> ").append(arfuName(methodName)).append(" = ")
                .append(arfu).append(".newUpdater(").append(classFqn).append(".class, java.lang.Object.class, \"")
                .append(methodName).append("\");").toString(),
                generated));

            // ... corresponding volatile field ...
            generated.addField(CtField.make("private volatile java.lang.Object " + methodName + ";", generated));

            // ... and the getter
            generated.getMethod("methodName", retName).setBody(new StringBuilder()
                .append("{\n")
                .append("final java.lang.Object cached = ").append(methodArfu).append(".get(this);\n")
                .append("final java.lang.Object result;\n")
                .append("if (cached == null) {\n")
                .append("    result = codecMember(").append(methodName).append(");\n")
                .append("    if (!").append(methodArfu).append(".compareAndSet(this, null, codecMaskNull(result))) {\n")
                // Unlikely event, just re-invoke the same method, which will fast-path
                .append("        return ").append(methodName).append("();\n")
                .append("    }\n")
                .append("} else {")
                .append("    result = codecUnmaskNull(cached);\n")
                .append("}\n")
                .append("return (").append(retName).append(") result;\n")
                .append('}').toString());
        }

        // Final bits: codecHashCode() ...
        generated.getMethod("codecHashCode", "I").setBody(codecHashCodeBody());

        // ... equals
        generated.getMethod("codecEquals", "Z").setBody(codecEqualsBody());

        // ... and codecFillToString()
        generated.getMethod("codecFillToString", ToStringHelper.class.getName()).setBody(codecFillToStringBody());

        generated.setModifiers(Modifier.FINAL | Modifier.PUBLIC);
    }

    private String codecHashCodeBody() {
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("final int prime = 31;\n")
                .append("int result = 1;\n");

        for (Method method : methods) {
            final String methodName = method.getName();
            sb.append("result = prime * result + java.util.Objects.hashCode(").append(methodName).append("());\n");
        }

        return sb.append("return result;\n")
                .append('}').toString();
    }

    private String codecEqualsBody() {
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("return true");

        for (Method method : methods) {
            final String methodName = method.getName();
            sb.append("\n&& java.util.Objects.equals(").append(methodName).append("(), other.").append(methodName)
            .append("())");
        }

        return sb.append(";\n")
                .append('}').toString();
    }

    private String codecFillToStringBody() {
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("return helper");
        for (Method method : methods) {
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