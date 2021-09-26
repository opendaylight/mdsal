/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.spi;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Test various bits of DescriptorConstants. This suite also tests negative scenarios so our runtime can be make
 * assumptions about which errors XML validation will catch before we ever get to interpret the document.
 */
// FIXME: use text blocks when we have JDK15+
public class XMLTest {
    @Test
    public void testValidSchema() {
        assertNotNull(XML.SCHEMA);
    }

    @Test
    public void testValidSimpleXml() {
        assertValid("<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
            + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
            + "       class=\"org.opendaylight.mdsal.binding.scr.it.ExampleTestImplementation\"\n"
            + "       name=\"INITIAL_CONT\"/>\n"
            + "</cfg:initial>");
    }

    @Test
    public void testNoFields() {
        assertFailureEndsWith("The content of element 'cfg:initial' is not complete. One of '{field}' is expected.",
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\"/>");
    }

    @Test
    public void testNoType() {
        assertMissingAttribute("type",
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
            + "<field class=\"org.opendaylight.mdsal.binding.scr.it.ExampleTestImplementation\"\n"
            + "       name=\"INITIAL_CONT\"/>\n"
            + "</cfg:initial>");
    }

    @Test
    public void testEmptyType() {
        assertEmptyAttribute(
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
                + "<field type=\"\"\n"
                + "       class=\"org.opendaylight.mdsal.binding.scr.it.ExampleTestImplementation\"\n"
                + "       name=\"INITIAL_CONT\"/>\n"
                + "</cfg:initial>");
    }

    @Test
    public void testNoClass() {
        assertMissingAttribute("class",
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
            + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
            + "       name=\"INITIAL_CONT\"/>\n"
            + "</cfg:initial>");
    }

    @Test
    public void testNoName() {
        assertMissingAttribute("name",
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
                + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
                + "       class=\"org.opendaylight.mdsal.binding.scr.it.ExampleTestImplementation\"/>"
                + "</cfg:initial>");
    }

    @Test
    public void testEmptyName() {
        assertEmptyAttribute(
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
                + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
                + "       class=\"org.opendaylight.mdsal.binding.scr.it.ExampleTestImplementation\"\n"
                + "       name=\"\"/>\n"
                + "</cfg:initial>");
    }

    @Test
    public void testDuplicateType() {
        assertFailureEndsWith(
            "Duplicate key value [org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont] declared for identity "
                + "constraint \"type\" of element \"initial\".",
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
                + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
                + "       class=\"org.opendaylight.mdsal.binding.scr.it.ExampleTestImplementation\"\n"
                + "       name=\"INITIAL_CONT\"/>\n"
                + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
                + "       class=\"org.opendaylight.mdsal.binding.scr.it2.ExampleTestImplementation2\"\n"
                + "       name=\"INITIAL_CONT2\"/>\n"
                + "</cfg:initial>");
    }

    private static void assertEmptyAttribute(final String xml) {
        assertFailureEndsWith(
            "Value '' with length = '0' is not facet-valid with respect to minLength '1' for type 'Tstring'.", xml);
    }

    private static void assertMissingAttribute(final String attr, final String xml) {
        assertFailureEndsWith("Attribute '" + attr + "' must appear on element 'field'.", xml);
    }

    private static void assertFailureEndsWith(final String endsWith, final String xml) {
        assertThat(assertThrows(SAXParseException.class, () -> validate(xml)).getMessage(), endsWith(endsWith));
    }

    private static void assertValid(final String xml) {
        try {
            validate(xml);
        } catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }

    private static void validate(final String xml) throws IOException, SAXException {
        XML.SCHEMA.newValidator().validate(new StreamSource(new StringReader(xml)));
    }
}
