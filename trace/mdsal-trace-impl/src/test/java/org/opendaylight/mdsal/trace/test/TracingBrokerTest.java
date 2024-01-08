/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.trace.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.trace.impl.TracingBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsaltrace.rev160908.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsaltrace.rev160908.ConfigBuilder;

/**
 * Test of {@link TracingBroker}. This test resides outside of org.opendaylight.mdsal.trace.impl package on purpose,
 * as the package name is used to suppress stack entries.
 *
 * @author Michael Vorburger.ch
 */
public class TracingBrokerTest {
    @Test
    @SuppressWarnings({ "resource", "unused" }) // Finding resource leaks is the point of this test
    public void testPrintOpenTransactions() {
        DOMDataBroker domDataBroker = mock(DOMDataBroker.class, RETURNS_DEEP_STUBS);
        Config config = new ConfigBuilder().setTransactionDebugContextEnabled(true).build();
        BindingCodecTree codec = mock(BindingCodecTree.class);
        TracingBroker tracingBroker = new TracingBroker(domDataBroker, config, codec);

        for (int i = 0; i < 3; i++) {
            DOMDataTreeReadWriteTransaction tx = tracingBroker.newReadWriteTransaction();
        }
        DOMDataTreeReadWriteTransaction anotherTx = tracingBroker.newReadWriteTransaction();

        DOMTransactionChain txChain = tracingBroker.createTransactionChain();
        DOMDataTreeReadWriteTransaction txFromChain = txChain.newReadWriteTransaction();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        boolean printReturnValue = tracingBroker.printOpenTransactions(ps, 1);
        String output = new String(baos.toByteArray(), UTF_8);

        assertTrue(printReturnValue);
        // Assert expectations about stack trace
        assertThat(output, containsString("testPrintOpenTransactions(TracingBrokerTest.java"));
        assertThat(output, not(containsString(TracingBroker.class.getName())));

        String previousLine = "";
        for (String line : output.split("\n")) {
            if (line.contains("(...")) {
                assertThat(previousLine, not(containsString("(...)")));
            }
            previousLine = line;
        }

        // assert that the sorting works - the x3 is shown before the x1
        assertThat(output, containsString("  DataBroker : newReadWriteTransaction()\n    3x"));

        // We don't do any verify/times on the mocks,
        // because the main point of the test is just to verify that
        // printOpenTransactions runs through without any exceptions
        // (e.g. it used to have a ClassCastException).
    }
}
