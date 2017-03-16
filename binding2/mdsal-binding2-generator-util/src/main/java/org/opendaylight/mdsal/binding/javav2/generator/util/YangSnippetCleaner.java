/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

import com.google.common.annotations.Beta;
import org.apache.commons.lang3.StringUtils;

@Beta
public final class YangSnippetCleaner {

    private static final String[] RESERVED_LINES = { "yang-version", "namespace" };

    private static final String REGEX_NEW_LINE = "\\r?\\n";
    private static final String REGES_WS = "\\s+";
    private static final String START_BODY = "{";
    private static final String END_BODY = "}";
    private static final String KEY_PART = new StringBuilder("key ").append('"').toString();
    private static final String UNIQUE_PART = new StringBuilder("unique ").append('"').toString();
    private static final String SPECIAL_END_LINE = new StringBuilder("\";").toString();

    private static final char NEW_LINE = '\n';
    private static final char SPACE = ' ';

    private static final int INDENT = 4;

    private YangSnippetCleaner() {
        throw new UnsupportedOperationException("Util class");
    }

    /**
     * Cleaning yang model of excess whitespaces, adding indentions
     *
     * @param unformedYang
     *            - unformed yang model
     * @return cleaned yang model
     */
    public static String clean(final String unformedYang) {
        int indentCount = 0;
        final StringBuilder sb = new StringBuilder();
        final String[] spliter = unformedYang.split(REGEX_NEW_LINE);

        for (int i = 0; i < spliter.length; i++) {
            if (!StringUtils.isBlank(spliter[i])) {
                String line = cleanLine(spliter[i]);
                if(line.equals(START_BODY)){
                    indentCount = mergeWithPrevious(sb, indentCount);
                    continue;
                }
                if ((line.contains(KEY_PART) || line.contains(UNIQUE_PART)) && line.endsWith(SPECIAL_END_LINE)) {
                    line = new StringBuilder(line.substring(0, line.indexOf(SPECIAL_END_LINE) - 1))
                            .append(SPECIAL_END_LINE).toString();
                }
                indentCount = lineIndent(sb, indentCount, line);
                sb.append(line).append(NEW_LINE);
                if (!StringUtils.startsWithAny(line, RESERVED_LINES) && indentCount == 1 && i != 0) {
                        sb.append(NEW_LINE);
                }
            }
        }
        if (sb.charAt(sb.length() - 4) == NEW_LINE && sb.charAt(sb.length() - 3) == NEW_LINE) {
            sb.deleteCharAt(sb.length() - 3);
        }
        removeRemainingWhitespace(sb);
        sb.append(NEW_LINE);

        return sb.toString();
    }

    private static int mergeWithPrevious(final StringBuilder sb, final int indentCount) {
        int newIndentCount = indentCount;
        removeRemainingWhitespace(sb);
        sb.append(SPACE);
        sb.append(START_BODY);
        sb.append(NEW_LINE);
        return ++newIndentCount;
    }

    private static void removeRemainingWhitespace(final StringBuilder sb) {
        while (StringUtils.isWhitespace(String.valueOf(sb.charAt(sb.length() - 1)))) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    private static String cleanLine(final String split) {
        final StringBuilder sb = new StringBuilder();
        final String[] s = split.split(REGES_WS);
        for (int i = 0; i < s.length; i++) {
            if (!StringUtils.isBlank(s[i])) {
                sb.append(s[i]);
                if (i != (s.length - 1)) {
                    sb.append(SPACE);
                }
            }
        }
        return sb.toString();
    }

    private static int lineIndent(final StringBuilder sb, final int indentCount, final String line) {
        int newIndentCount = indentCount;
        if (line.contains(END_BODY) && !line.contains(START_BODY)) {
            newIndentCount--;
        }
        for (int i = 0; i < (newIndentCount * INDENT); i++) {
            sb.append(SPACE);
        }

        if (line.contains(START_BODY) && !line.contains(END_BODY)) {
            newIndentCount++;
        }

        return newIndentCount;
    }
}
