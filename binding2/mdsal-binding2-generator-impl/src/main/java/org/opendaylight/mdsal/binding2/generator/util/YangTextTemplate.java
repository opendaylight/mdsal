/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding2.generator.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import java.util.StringTokenizer;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Util class
 */
public final class YangTextTemplate {
    private static final CharMatcher NEWLINE_OR_TAB = CharMatcher.anyOf("\n\t");

    private YangTextTemplate() {
        throw new UnsupportedOperationException("Util class");
    }

    /**
     * @param text Content of tag description
     * @param nextLineIndent Number of spaces from left side default is 12
     * @return formatted description
     */
    public static String formatToParagraph(final String text, final int nextLineIndent) {
        if (Strings.isNullOrEmpty(text)) {
            return "";
        }

        boolean isFirstElementOnNewLineEmptyChar = false;
        final StringBuilder sb = new StringBuilder();
        final StringBuilder lineBuilder = new StringBuilder();
        final String lineIndent = Strings.repeat(" ", nextLineIndent);
        final String textToFormat = NEWLINE_OR_TAB.removeFrom(text);
        final String formattedText = textToFormat.replaceAll(" +", " ");
        final StringTokenizer tokenizer = new StringTokenizer(formattedText, " ", true);

        while (tokenizer.hasMoreElements()) {
            final String nextElement = tokenizer.nextElement().toString();

            if (lineBuilder.length() + nextElement.length() > 80) {
                // Trim trailing whitespace
                for (int i = lineBuilder.length() - 1; i >= 0 && lineBuilder.charAt(i) != ' '; --i) {
                    lineBuilder.setLength(i);
                }
                // Trim leading whitespace
                while (lineBuilder.charAt(0) == ' ') {
                    lineBuilder.deleteCharAt(0);
                }
                sb.append(lineBuilder).append('\n');
                lineBuilder.setLength(0);

                if (nextLineIndent > 0) {
                    sb.append(lineIndent);
                }

                if (" ".equals(nextElement)) {
                    isFirstElementOnNewLineEmptyChar = true;
                }
            }
            if (isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = false;
            } else {
                lineBuilder.append(nextElement);
            }
        }
        return sb.append(lineBuilder).append('\n').toString();
    }

    public static String formatToAugmentPath(final Iterable<QName> schemaPath) {
        final StringBuilder sb = new StringBuilder();
        for (QName pathElement : schemaPath) {
            sb.append("\\(")
            .append(pathElement.getNamespace())
            .append(')')
            .append(pathElement.getLocalName());
        }
        return sb.toString();
    }
}