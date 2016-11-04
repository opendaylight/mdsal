/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import difflib.DiffUtils;
import difflib.Patch;
import java.util.List;

/**
 * Utility to create diff of text.
 *
 * @author Michael Vorburger
 */
// package-local: no need to expose this, consider it an implementation detail; public API is the AssertDataObjects
class DiffUtil {

    // Configuration which we could tune as we use this more
    private static final int MAX_DIFFS = 1;
    private static final int CONTEXT_LINES = 3; // number of lines of context output around each difference

    private static final Splitter SPLITTER = Splitter.on(System.getProperty("line.separator"));
    private static final Joiner JOINER = Joiner.on(System.getProperty("line.separator"));

    public static String diff(String expectedText, String actualText) {
        List<String> originalLines = splitIntoLines(expectedText);
        List<String> revisedLines = splitIntoLines(actualText);
        Patch<String> patch = DiffUtils.diff(originalLines, revisedLines);
        List<String> diff = DiffUtils.generateUnifiedDiff("expected", "actual", originalLines, patch, CONTEXT_LINES);

        String header = "";
        if (patch.getDeltas().size() > MAX_DIFFS) {
            header = "(More than " + MAX_DIFFS + " differences; only showing first " + MAX_DIFFS + ")\n";
            diff = diff.subList(0, MAX_DIFFS);
        }
        return header + JOINER.join(diff);
    }

    private static List<String> splitIntoLines(String expectedText) {
        return Lists.newArrayList(SPLITTER.split(expectedText));
    }

}
