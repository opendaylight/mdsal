/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import static org.opendaylight.mdsal.binding2.java.api.generator.util.TextTemplateUtil.asJavadoc;
import static org.opendaylight.mdsal.binding2.java.api.generator.util.TextTemplateUtil.encodeAngleBrackets;

import java.util.LinkedList;
import java.util.List;
import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.txt.enumTemplate;

public class EnumRenderer extends BaseRenderer {
    private final Enumeration enums;

    public EnumRenderer(final Enumeration type) {
        super(((GeneratedType) type));
        enums = type;
    }

    @Override
    protected String body() {
        String importedName = importedName(String.class);
        return enumTemplate.render(enums, importedName).body();
    }

    /**
     * @param enumeration
     * @return List of enumeration pairs with javadoc
     */
    public static String writeEnumeration(final Enumeration enumeration) {
        final List<String> strings = new LinkedList<>();
        if (!enumeration.getValues().isEmpty()) {
            for (Enumeration.Pair pair : enumeration.getValues()) {
                final StringBuilder parameterWithType = new StringBuilder();
                parameterWithType.append(asJavadoc(encodeAngleBrackets(pair.getDescription())));
                parameterWithType.append("\n");
                parameterWithType.append(pair.getMappedName());
                parameterWithType.append("(");
                parameterWithType.append(pair.getValue());
                parameterWithType.append(", \"");
                parameterWithType.append(pair.getName());
                parameterWithType.append("\")");
                strings.add(parameterWithType.toString());
            }
        }
        return String.join(",\n", strings).concat(";");
    }
}