/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.api;

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

// FIXME: use text blocks when we have JDK15+
public class DescriptorConstantsTest {
    @Test
    public void testValidSchema() {
        assertNotNull(DescriptorConstants.descriptorSchema());
    }

    @Test
    public void testValidSimpleXml() {
        assertValid("<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
            + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
            + "       package=\"org.opendaylight.mdsal.binding.scr.it\"\n"
            + "       class=\"ExampleTestImplementation\"\n"
            + "       name=\"INITIAL_CONT\"/>\n"
            + "</cfg:initial>");
    }

    @Test
    public void testNoFields() {
        assertInvalid("The content of element 'cfg:initial' is not complete. One of '{field}' is expected.",
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\"/>");
    }

    @Test
    public void testNoType() {
        assertMissingAttribute("type",
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
            + "<field package=\"org.opendaylight.mdsal.binding.scr.it\"\n"
            + "       class=\"ExampleTestImplementation\"\n"
            + "       name=\"INITIAL_CONT\"/>\n"
            + "</cfg:initial>");
    }

    @Test
    public void testEmptyType() {
        assertEmptyAttribute(
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
                + "<field type=\"\"\n"
                + "       package=\"org.opendaylight.mdsal.binding.scr.it\"\n"
                + "       class=\"ExampleTestImplementation\"\n"
                + "       name=\"INITIAL_CONT\"/>\n"
                + "</cfg:initial>");
    }

    @Test
    public void testNoPackage() {
        assertMissingAttribute("package",
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
                + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
                + "       class=\"ExampleTestImplementation\"\n"
                + "       name=\"INITIAL_CONT\"/>\n"
                + "</cfg:initial>");
    }

    @Test
    public void testEmptyPackage() {
        assertEmptyAttribute(
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
                + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
                + "       package=\"\"\n"
                + "       class=\"ExampleTestImplementation\"\n"
                + "       name=\"INITIAL_CONT\"/>\n"
                + "</cfg:initial>");
    }

    @Test
    public void testNoClass() {
        assertMissingAttribute("class",
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
            + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
            + "       package=\"org.opendaylight.mdsal.binding.scr.it\"\n"
            + "       name=\"INITIAL_CONT\"/>\n"
            + "</cfg:initial>");
    }

    @Test
    public void testEmptyClass() {
        assertEmptyAttribute(
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
                + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
                + "       package=\"org.opendaylight.mdsal.binding.scr.it\"\n"
                + "       class=\"\"\n"
                + "       name=\"INITIAL_CONT\"/>\n"
                + "</cfg:initial>");
    }

    @Test
    public void testNoName() {
        assertMissingAttribute("name",
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
                + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
                + "       package=\"org.opendaylight.mdsal.binding.scr.it\"\n"
                + "       class=\"ExampleTestImplementation\"/>"
                + "</cfg:initial>");
    }

    @Test
    public void testEmptyName() {
        assertEmptyAttribute(
            "<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n"
                + "<field type=\"org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont\"\n"
                + "       package=\"org.opendaylight.mdsal.binding.scr.it\"\n"
                + "       class=\"ExampleTestImplementation\"\n"
                + "       name=\"\"/>\n"
                + "</cfg:initial>");
    }

    private static void assertEmptyAttribute(final String xml) {
        assertInvalid("Value '' with length = '0' is not facet-valid with respect to minLength '1' for type 'Tstring'.",
            xml);
    }

    private static void assertMissingAttribute(final String attr, final String xml) {
        assertInvalid("Attribute '" + attr + "' must appear on element 'field'.", xml);
    }

    private static void assertInvalid(final String endsWith, final String xml) {
        final SAXParseException ex = assertThrows(SAXParseException.class, () -> validate(xml));
        assertThat(ex.getMessage(), endsWith(endsWith));
    }

    private static void assertValid(final String xml) {
        try {
            validate(xml);
        } catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }

    private static void validate(final String xml) throws IOException, SAXException {
        DescriptorConstants.descriptorSchema().newValidator().validate(new StreamSource(new StringReader(xml)));
    }
}
