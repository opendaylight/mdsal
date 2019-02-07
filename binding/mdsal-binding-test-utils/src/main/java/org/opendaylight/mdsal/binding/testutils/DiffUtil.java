/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.List;

/**
 * Utility to create diff of text.
 *
 * @author Michael Vorburger
 */
// package-local: no need to expose this, consider it an implementation detail; public API is the AssertDataObjects
final class DiffUtil {

    // Configuration which we could tune as we use this more
    private static final int MAX_DIFFS = 1;
    private static final int CONTEXT_LINES = 3; // number of lines of context output around each difference

    private static final Splitter SPLITTER = Splitter.on(System.getProperty("line.separator"));
    private static final Joiner JOINER = Joiner.on(System.getProperty("line.separator"));

    public static String diff(String expectedText, String actualText) {
        List<String> originalLines = SPLITTER.splitToList(expectedText);
        List<String> revisedLines = SPLITTER.splitToList(actualText);
        Patch<String> patch;
        try {
            patch = DiffUtils.diff(originalLines, revisedLines);
        } catch (DiffException e) {
            throw new IllegalArgumentException("Failed to generate patch", e);
        }
        List<String> diff = UnifiedDiffUtils.generateUnifiedDiff("expected", "actual", originalLines, patch,
            CONTEXT_LINES);

        String header = "";
        int deltas = patch.getDeltas().size();
        if (deltas > MAX_DIFFS) {
            header = String.format("(Too many differences (%d); only showing first %d)%n", deltas, MAX_DIFFS);
            diff = diff.subList(0, MAX_DIFFS);
        }
        return header + JOINER.join(diff);
    }

    private DiffUtil() { }

}
