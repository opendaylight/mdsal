/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.api;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.xml.sax.SAXException;

/**
 * Constants relating to location and content of the descriptor XML file.
 */
@Beta
@NonNullByDefault
public final class DescriptorConstants {
    private static final class SchemaHolder {
        static final Schema SCHEMA;

        static {
            try {
                final var schemaFactory = verifyNotNull(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI));
                schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

                SCHEMA = verifyNotNull(schemaFactory.newSchema(new StreamSource(newDescriptorSchemaStream())));
            } catch (SAXException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    /**
     * Location of SCR/Configuration descriptor file in a conforming JAR.
     */
    public static final String METAINF_SCR_CONFIGURATION =
        "META-INF/org.opendaylight.mdsal.binding/scr-configuration.xml";

    /**
     * Open a {@link Reader} containing the latest schema.
     *
     * @return A new Reader
     */
    public static InputStream newDescriptorSchemaStream() {
        return verifyNotNull(DescriptorConstants.class.getResourceAsStream("/scr-configuration.xsd"));
    }

    /**
     * Return the XML validation {@link Schema} corresponding to the descriptor XSD interpreted by the default
     * {@link SchemaFactory}.
     *
     * @return A shared Schema object
     */
    public static Schema descriptorSchema() {
        return SchemaHolder.SCHEMA;
    }

    private DescriptorConstants() {
        // Hidden on purpose
    }
}
