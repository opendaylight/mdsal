/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.spi;

import static com.google.common.base.Verify.verifyNotNull;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.eclipse.jdt.annotation.NonNull;
import org.xml.sax.SAXException;

final class XML {
    static final Schema SCHEMA;

    static {
        try {
            final var schemaFactory = verifyNotNull(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI));
            schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            SCHEMA = verifyNotNull(schemaFactory.newSchema(new StreamSource(
                verifyNotNull(XML.class.getResourceAsStream("/scr-configuration.xsd")))));
        } catch (SAXException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static final @NonNull DocumentBuilderFactory DBF;

    static {
        final DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setCoalescing(true);
        f.setExpandEntityReferences(false);
        f.setIgnoringElementContentWhitespace(true);
        f.setIgnoringComments(true);
        f.setNamespaceAware(true);
        f.setXIncludeAware(false);
        try {
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (ParserConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }

        try {
            // This makes our parsing code a breeze, as we only have valid documents
            f.setSchema(SCHEMA);
        } catch (UnsupportedOperationException e) {
            throw new ExceptionInInitializerError(e);
        }

        DBF = f;
    }

    private XML() {
        // Hidden on purpose
    }
}