/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;


import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

final class FileSearchUtil {
    private static final String LS = System.lineSeparator();
    private FileSearchUtil() {
        // Hidden on purpose
    }

    static void assertFileContains(final File file, final String searchText) throws IOException {
        assertFileContains(file, Files.readString(file.toPath()), searchText);
    }

    static void assertFileContains(final File file, final String fileContent, final String searchText) {
        if (!fileContent.contains(searchText)) {
            throw new AssertionError("File " + file + " does not contain '" + searchText + "'");
        }
    }

    static void assertFileContainsConsecutiveLines(final File file, final String fileContent, final String ... lines) {
        for (final String line : lines) {
            assertFileContains(file, fileContent, line);
        }
        assertFileContains(file, fileContent, Joiner.on(LS).join(lines));
    }

    static Map<String, File> getFiles(final File path) {
        final Map<String, File> ret = new HashMap<>();
        getFiles(path, ret);
        return ret;
    }

    private static void getFiles(final File path, final Map<String, File> files) {
        final File [] dirFiles = path.listFiles();
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                getFiles(file, files);
            }

            files.put(file.getName(), file);
        }
    }
}
