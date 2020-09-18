/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yang.generator;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

public class ModelWriter {
    public static final String MODULE_NAME = "benchmark-model";
    public static final String PREFIX = "bm";
    public static final String NAMESPACE = "bm";
    public static final String REVISION = "2020-08-29";

    public List<SchemaNode> nodes;

    public ModelWriter(ModuleElem module) {

    }

    private static String leafTypeString(int number) {
        return "leaf lf" + number + " {\n"
                + "  type string;" + "\n"
                + "}\n";
    }

    private static StringBuilder listOneLeaf(int number) {
        return new StringBuilder().append("list ls").append(number).append("{\n")
                .append(leafTypeString(1))
                .append("}\n");
    }

    private static StringBuilder moduleHeader() {
        return new StringBuilder().append("module a {\n")
                .append("  yang-version 1;\n")
                .append("  namespace a;\n")
                .append("  prefix a;\n");
    }

    private static String model200lists() {
        final StringBuilder model = moduleHeader();
        for (int i = 1; i <= 200; i++) {
            model.append(listOneLeaf(i));
        }
        return model.append("}").toString();
    }

//    @Test
//    public void generate() {
//        try {
//            final Path path = Path.of("target", "model");
//        final Path filePath = Path.of("model", "a.yang");
//        final Path path = Path.of("model");
//            final Path path = Path.of("target", "model", "a.yang");
//            final Path path = Path.of("target", "model", "a.yang");
//            Files.createDirectory(path);
//            Files.createFile(filePath);
//            Files.writeString(filePath, model200lists());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
