/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.eclipse.jdt.annotation.NonNull;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Modeled configuration descriptor. Exposed initial configuration fields via {@link #fields}.
 */
public final class ConfigurationDescriptor {
    /**
     * Location of SCR/Configuration descriptor file in a conforming JAR.
     */
    public static final @NonNull String METAINF_SCR_CONFIGURATION =
        "META-INF/org.opendaylight.mdsal.binding/scr-configuration.xml";

    public final @NonNull Map<String, ConfigurationField> fields;

    private ConfigurationDescriptor(final Map<String, ConfigurationField> fields) {
        this.fields = requireNonNull(fields);
    }

    public static @NonNull ConfigurationDescriptor of(final Map<String, ConfigurationField> fields) {
        return new ConfigurationDescriptor(Map.copyOf(fields));
    }

    public static @NonNull ConfigurationDescriptor fromXML(final InputStream xmlStream) throws IOException {
        // Try to parse the XML fragment with
        final Element initialNode;
        try {
            initialNode = XML.DBF.newDocumentBuilder().parse(xmlStream).getDocumentElement();
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse XML stream", e);
        }

        final var fields = ImmutableMap.<String, ConfigurationField>builder();
        for (var fieldNode = initialNode.getFirstChild(); fieldNode != null; fieldNode = fieldNode.getNextSibling()) {
            final var element = (Element) fieldNode;
            final var field = ConfigurationField.of(element.getAttribute("type"), element.getAttribute("class"),
                element.getAttribute("name"));
            fields.put(field.typeName, field);
        }

        return new ConfigurationDescriptor(fields.build());
    }

    public @NonNull String toXML() throws IOException {
        final var sb = new StringBuilder()
            .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n");

        for (var entry : fields.entrySet()) {
            sb.append("<field type=\"").append(entry.getKey()).append("\"\n");

            final var field = entry.getValue();
            sb.append("       class=\"").append(field.className).append("\"\n");
            sb.append("       name=\"").append(field.fieldName).append("\"/>\n");
        }

        final var xmlBody = sb.append("</cfg:initial>").toString();
        try {
            XML.SCHEMA.newValidator().validate(new StreamSource(new StringReader(xmlBody)));
        } catch (SAXException e) {
            throw new IOException("Refusing to write invalid descriptor", e);
        }

        return xmlBody;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("fields", fields).toString();
    }
}
