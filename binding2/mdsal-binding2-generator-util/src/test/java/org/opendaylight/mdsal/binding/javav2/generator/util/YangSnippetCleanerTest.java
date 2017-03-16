/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.Assert;
import org.junit.Test;

public class YangSnippetCleanerTest {

    @Test
    public void cleanerTest() throws Exception {
        final String badWs = readFile(this.getClass().getResourceAsStream("/yangs/break_ws/bad-ws.yang"));
        final String fixedBadWs = readFile(this.getClass().getResourceAsStream("/yangs/break_ws/fixed-bad-ws.yang"));
        final String cleanBadWs = YangSnippetCleaner.clean(badWs);
        Assert.assertEquals(fixedBadWs, cleanBadWs);
    }

    private String readFile(final InputStream inputStream) throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        try {
            final StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                if (!line.contains("//")) {
                    sb.append(line);
                }
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }
}
