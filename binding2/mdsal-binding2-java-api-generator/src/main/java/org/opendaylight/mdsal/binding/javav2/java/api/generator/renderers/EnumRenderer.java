/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers;

import static org.opendaylight.mdsal.binding.javav2.java.api.generator.util.TextTemplateUtil.asJavadoc;
import static org.opendaylight.mdsal.binding.javav2.java.api.generator.util.TextTemplateUtil.encodeAngleBrackets;

import java.util.LinkedList;
import java.util.List;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.enumTemplate;

public class EnumRenderer extends BaseRenderer {
    private final Enumeration enums;

    public EnumRenderer(final Enumeration type) {
        super(type);
        enums = type;
    }

    @Override
    protected String body() {
        String importedName = importedName(String.class);
        return enumTemplate.render(enums, importedName).body();
    }

    /**
     * @param enumeration enumeration to write
     * @return List of enumeration pairs with javadoc
     */
    public static String writeEnumeration(final Enumeration enumeration) {
        final List<CharSequence> strings = new LinkedList<>();
        if (!enumeration.getValues().isEmpty()) {
            for (Enumeration.Pair pair : enumeration.getValues()) {
                final StringBuilder sb = new StringBuilder();
                sb.append(asJavadoc(encodeAngleBrackets(pair.getDescription().orElse(null))));
                sb.append("\n");
                sb.append(pair.getMappedName());
                sb.append('(');
                sb.append(pair.getValue());
                sb.append(", \"");
                sb.append(pair.getName());
                sb.append("\")");
                strings.add(sb);
            }
        }
        return String.join(",\n", strings).concat(";");
    }
}