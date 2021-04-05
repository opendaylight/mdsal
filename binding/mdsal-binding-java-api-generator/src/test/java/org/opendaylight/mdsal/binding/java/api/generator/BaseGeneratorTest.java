/*
 * Copyright (c) 2021 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class BaseGeneratorTest {
    private static final String LINE_SEPARATOR_PROP_NAME = "line.separator";
    private String originalLineSeparator;

    @BeforeClass
    public void initLineSeparator() throws IOException, URISyntaxException {
        // Define line separator explicitly for this test, and rollback in the end.
        // This fixes a problem of the windows-type OS, where the Xtend plugin will generate code with '\r\n' ending.
        originalLineSeparator = System.getProperty(LINE_SEPARATOR_PROP_NAME);
        System.setProperty(LINE_SEPARATOR_PROP_NAME, "\n");
    }

    @AfterClass
    public void rollbackLineSeparator() {
        System.setProperty(LINE_SEPARATOR_PROP_NAME, originalLineSeparator);
    }
}
