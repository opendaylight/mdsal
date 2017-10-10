/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.java.api.generator.util;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.RegEx;
import org.apache.commons.lang3.StringUtils;

/**
 * Pretty-print utility for generated Java code.
 */
public final class JavaCodePrettyPrint {

    // JDoc
    private static final String JDOC_START = "/**";
    private static final String JDOC_PART = " *";
    private static final String JDOC_END = "*/";

    // Comments
    private static final String COMMENTS = "//";

    // Body
    private static final char START_BODY = '{';
    private static final char END_BODY = '}';

    // Whitespaces
    @RegEx
    private static final String NEW_LINE_REGEX = "\\r?\\n";
    @RegEx
    private static final String WS_REGEX = "\\s+";
    private static final char SPACE = ' ';
    private static final char NEW_LINE = '\n';

    // Indention
    private static final int INDENT = 4;

    // Specific keywords
    private static final String PACKAGE = "package";
    private static final String IMPORT = "import";

    // Line
    private static final char END_LINE = ';';
    private static final char AT = '@';

    private JavaCodePrettyPrint() {
        throw new UnsupportedOperationException("Util class");
    }

    /**
     * Pretty-print generated Java code.
     *
     * @param unformedJavaFile
     *            - unformed Java file from generator
     * @return formed Java file
     */
    public static String perform(final String unformedJavaFile) {
        final StringBuilder sb = new StringBuilder();
        String[] splittedByNewLine = unformedJavaFile.split(NEW_LINE_REGEX);
        // remove excessive whitespaces
        splittedByNewLine = phaseOne(splittedByNewLine);
        // merge or divide lines which need it && setup base new lines at the
        // end of lines
        splittedByNewLine = phaseTwo(splittedByNewLine);
        // fix indents
        splittedByNewLine = phaseThree(splittedByNewLine);

        for (final String line : splittedByNewLine) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Set up indents.
     *
     * @param splittedByNewLine
     *            - cleaned and merged/divided lines
     * @return fixed intents in lines
     */
    private static String[] phaseThree(final String[] splittedByNewLine) {
        int indentCount = 0;
        final List<String> lines = new ArrayList<>();
        for (int i = 0; i < splittedByNewLine.length; i++) {
            final StringBuilder sb = new StringBuilder();
            indentCount = lineIndent(sb, indentCount, splittedByNewLine[i]);
            sb.append(splittedByNewLine[i]);
            if ((splittedByNewLine[i].contains(String.valueOf(END_BODY))
                    || splittedByNewLine[i].contains(String.valueOf(END_LINE))) && indentCount == 1) {
                sb.append(NEW_LINE);
            }
            lines.add(sb.toString());
        }

        return lines.toArray(new String[lines.size()]);
    }

    private static int lineIndent(final StringBuilder sb, final int indentCount, final String line) {
        int newIndentCount = indentCount;
        if (line.contains(String.valueOf(END_BODY)) && !line.startsWith(JDOC_PART)) {
            newIndentCount--;
        }
        for (int i = 0; i < (newIndentCount * INDENT); i++) {
            sb.append(SPACE);
        }

        if (line.contains(String.valueOf(START_BODY)) && !line.startsWith(JDOC_PART)) {
            newIndentCount++;
        }

        return newIndentCount;
    }

    /**
     * Join or split lines if necessary.
     *
     * @param splittedByNewLine
     *            - cleaned lines from whitespaces around them
     * @return fixed lines
     */
    private static String[] phaseTwo(final String[] splittedByNewLine) {
        final List<String> fixedLines = new ArrayList<>();

        // prepare package part
        if (splittedByNewLine[0].startsWith(PACKAGE)) {
            fixedLines.add(new StringBuilder(splittedByNewLine[0]).append(NEW_LINE).append(NEW_LINE).toString());
        }

        // prepare imports
        int importsEndAt;
        if (splittedByNewLine[1].startsWith(IMPORT)) {
            importsEndAt = 1;
            for (int i = 1; i < splittedByNewLine.length - 1; i++) {
                if (!splittedByNewLine[i + 1].startsWith(IMPORT)) {
                    fixedLines.add(new StringBuilder(splittedByNewLine[i]).append(NEW_LINE).append(NEW_LINE).toString());
                    importsEndAt = i;
                    break;
                } else {
                    fixedLines.add(new StringBuilder(splittedByNewLine[i]).append(NEW_LINE).toString());
                }
            }
        } else {
            importsEndAt = 0;
        }

        // prepare class
        StringBuilder sbLineClass = new StringBuilder();
        int classStartEnd = 0;
        for (int i = importsEndAt + 1; i < splittedByNewLine.length; i++) {
            i = appendJDoc(splittedByNewLine, fixedLines, i);
            if (!splittedByNewLine[i].contains(String.valueOf(START_BODY))) {
                sbLineClass.append(splittedByNewLine[i]).append(SPACE);
            } else {
                fixedLines.add(sbLineClass.append(splittedByNewLine[i]).append(NEW_LINE).append(NEW_LINE).toString());
                classStartEnd = i + 1;
                break;
            }
        }

        for(int i = classStartEnd; i < splittedByNewLine.length; i++){
            i = appendJDoc(splittedByNewLine, fixedLines, i);
            if (!splittedByNewLine[i].startsWith(COMMENTS)
                    && !splittedByNewLine[i].endsWith(String.valueOf(END_LINE))
                    && !splittedByNewLine[i].endsWith(String.valueOf(START_BODY))
                    && !splittedByNewLine[i].endsWith(String.valueOf(END_BODY))
                    && !splittedByNewLine[i].startsWith(String.valueOf(AT))) {
                sbLineClass = new StringBuilder();
                for (int j = i; j < splittedByNewLine.length; j++) {
                    if (!splittedByNewLine[j].contains(String.valueOf(START_BODY))
                            && !splittedByNewLine[j].contains(String.valueOf(END_LINE))) {
                        final String str = splittedByNewLine[j];
                        sbLineClass.append(str).append(SPACE);
                    } else {
                        fixedLines.add(sbLineClass.append(splittedByNewLine[j]).append(NEW_LINE).toString());
                        i = j;
                        break;
                    }
                }
                continue;
            }
            final String splStri = splittedByNewLine[i];
            final String stringSB = String.valueOf(START_BODY);
            final String stringEB = String.valueOf(END_BODY);
            if (splStri.contains(stringSB) && splStri.endsWith(stringEB)) {
                final StringBuilder sb = new StringBuilder();
                for (int j = 0; j < splittedByNewLine[i].length(); j++) {
                    if (splittedByNewLine[i].charAt(j) == END_BODY) {
                        sb.append(NEW_LINE);
                    }
                    sb.append(splittedByNewLine[i].charAt(j));
                    if (splittedByNewLine[i].charAt(j) == START_BODY) {
                        sb.append(NEW_LINE);
                    }
                }
                final String[] split = sb.toString().split(NEW_LINE_REGEX);
                for (final String s : split) {
                    fixedLines.add(new StringBuilder(s).append(NEW_LINE).toString());
                }
                continue;
            }
            fixedLines.add(new StringBuilder(splittedByNewLine[i]).append(NEW_LINE).toString());
        }

        return fixedLines.toArray(new String[fixedLines.size()]);
    }

    private static int appendJDoc(final String[] splittedByNewLine, final List<String> fixedLines, int i) {
        if (splittedByNewLine[i].contains(JDOC_START)) {
            fixedLines.add(new StringBuilder(splittedByNewLine[i]).append(NEW_LINE).toString());
            for (int j = i + 1; j < splittedByNewLine.length - 1; j++) {
                if (splittedByNewLine[j].contains(JDOC_END)) {
                    fixedLines.add(new StringBuilder(SPACE)
                            .append(SPACE).append(splittedByNewLine[j]).append(NEW_LINE).toString());
                    i = j + 1;
                    break;
                } else {
                    fixedLines.add(new StringBuilder(SPACE)
                            .append(SPACE).append(splittedByNewLine[j]).append(NEW_LINE).toString());
                }
            }
        }
        return i;
    }

    /**
     * Remove empty lines and whitespaces adjacent lines.
     *
     * @param splittedByNewLine
     *            - lines with whitespaces around them
     * @return cleaned lines from whitespaces
     */
    private static String[] phaseOne(final String[] splittedByNewLine) {
        final List<String> linesWithoutWhitespaces = new ArrayList<>();
        for (final String line : splittedByNewLine) {
            if (!StringUtils.isBlank(line)) {
                int lineStart = 0;
                for (int i = 0; i < line.length(); i++) {
                    if (StringUtils.isWhitespace(String.valueOf(line.charAt(i)))) {
                        lineStart++;
                    } else {
                        break;
                    }
                }
                int lineEnd = line.length() - 1;
                while (StringUtils.isWhitespace(String.valueOf(line.charAt(lineEnd)))) {
                    lineEnd--;
                }
                linesWithoutWhitespaces.add(line.substring(lineStart, lineEnd + 1));
            }
        }
        return linesWithoutWhitespaces.toArray(new String[linesWithoutWhitespaces.size()]);
    }
}
