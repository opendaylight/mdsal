/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.YangFeature;
import org.opendaylight.yangtools.yang.common.QName;

final class FeatureTemplate extends ClassTemplate {
    private static final @NonNull JavaTypeName QNAME = JavaTypeName.create(QName.class);
    private static final @NonNull JavaTypeName YANG_FEATURE = JavaTypeName.create(YangFeature.class);

    FeatureTemplate(final GeneratedTransferObject genType) {
        super(genType);
    }

    @Override
    protected String generateClassDeclaration(final boolean isInnerClass) {
        final var typeName = type().getName();

        return "public final class " + typeName + " extends " + importedName(YANG_FEATURE) + '<' + typeName + '>';
    }

    @Override
    protected String constructors() {
        final var typeName = type().getName();

        return "private " + typeName + "() {\n"
            + "    // Hidden on purpose\n"
            + "}";
    }

    @Override
    protected CharSequence emitConstant(final Constant c) {
        if (!BindingMapping.VALUE_STATIC_FIELD_NAME.equals(c.getName()) || !YangFeature.class.equals(c.getValue())) {
            return super.emitConstant(c);
        }

        final var type = type();
        final var typeName = type.getName();
        return "/**\n"
            + " * {@link " + typeName + "} singleton instance.\n"
            + " */\n"
            + "public static final " + importedNonNull(type) + ' ' + BindingMapping.VALUE_STATIC_FIELD_NAME + " = new "
            + type.getName() + "();";
    }

    @Override
    protected String propertyMethods() {
        final var override = importedName(OVERRIDE);
        final var typeName = type().getName();
        final var clazz = importedName(CLASS);

        return '@' + override + '\n'
            + "public " + clazz + '<' + typeName + "> " + BindingMapping.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME
            + "() {\n"
            + "    return " + typeName + ".class;\n"
            + "}\n"
            + '\n'
            + '@' + override + '\n'
            + "public " + importedName(QNAME) + " qname() {\n"
            + "    return " + BindingMapping.QNAME_STATIC_FIELD_NAME + ";\n"
            + "}\n";
    }
}
