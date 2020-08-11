package org.opendaylight.mdsal.binding.java.api.generator.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FileSearchUtil {
    public static boolean findInFile(final File file, final String searchText) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                final String nextLine = scanner.nextLine();
                if (nextLine.contains(searchText)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Map<String, File> getFiles(final File path) {
        return getFiles(path, new HashMap<>());
    }

    public static Map<String, File> getFiles(final File path, final Map<String, File> files) {
        final File [] dirFiles = path.listFiles();
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                return getFiles(file, files);
            }

            files.put(file.getName(), file);
        }
        return files;
    }
}
