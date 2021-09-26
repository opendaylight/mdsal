/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.api;

import java.io.IOException;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;
import org.xml.sax.SAXException;

public class DescriptorConstantsTest {
    @Test
    public void testExampleXml() throws SAXException, IOException {
        DescriptorConstants.descriptorSchema().newValidator().validate(
            new StreamSource(DescriptorConstantsTest.class.getResourceAsStream("/example-scr-configuration.xml")));
    }
}
