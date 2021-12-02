/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;

/**
 * Template for generating Java class.
 */
public final class ListKeyTemplate extends ClassTemplate {
    /**
     * Creates instance of this class with concrete {@code genType}.
     *
     * @param genType generated transfer object which will be transformed to Java class source code
     */
    public ListKeyTemplate(final GeneratedTransferObject genType) {
        super(genType);
    }

    @Override
    public CharSequence allValuesConstructor() {
        final StringConcatenation sc = new StringConcatenation();
        sc.append("public ");
        sc.append(type().getName());
        sc.append("(");
        sc.append(asNonNullArgumentsDeclaration(allProperties));
        sc.append(") {");
        sc.newLine();
        for (GeneratedProperty prop : allProperties) {
            final String fieldName = fieldName(prop);
            sc.append("    ");
            sc.newLineIfNotEmpty();
            sc.append("    ");
            sc.append("this.");
            sc.append(fieldName, "    ");
            sc.append(" = ");
            sc.append(importedName(CODEHELPERS), "    ");
            sc.append(".requireKeyProp(");
            sc.append(fieldName, "    ");
            sc.append(", \"");
            sc.append(prop.getName(), "    ");
            sc.append("\")");
            sc.append(cloneCall(prop), "    ");
            sc.append(";");
            sc.newLine();
        }
        for (GeneratedProperty prop : properties) {
            sc.append("    ");
            sc.append(generateRestrictions(type(), fieldName(prop), prop.getReturnType()), "    ");
            sc.newLineIfNotEmpty();
        }
        sc.append("}");
        sc.newLine();
        return sc;
    }

    @Override
    public CharSequence getterMethod(final GeneratedProperty field) {
        final StringConcatenation sc = new StringConcatenation();
        sc.append("public ");
        sc.append(importedNonNull(field.getReturnType()));
        sc.append(" ");
        sc.append(getterMethodName(field));
        sc.append("() {");
        sc.newLine();
        sc.append("    ");
        sc.append("return ");
        sc.append(fieldName(field), "    ");
        sc.append(cloneCall(field), "    ");
        sc.append(";");
        sc.newLine();
        sc.append("}");
        sc.newLine();
        return sc;
    }
}
