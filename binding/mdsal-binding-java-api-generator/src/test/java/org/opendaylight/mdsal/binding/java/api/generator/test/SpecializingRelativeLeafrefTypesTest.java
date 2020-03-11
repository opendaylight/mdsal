/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.mdsal.binding.java.api.generator.test.BaseCompilationTest.generateTestSources;
import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.getFiles;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.Types;

public class SpecializingRelativeLeafrefTypesTest extends BaseCompilationTest {
    private File sourcesOutputDir;
    private File compiledOutputDir;
    private List<Type> types;
    private Map<String, File> files;

    @Before
    public void before() throws IOException, URISyntaxException {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal426");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal426");
        types = generateTestSources("/compilation/mdsal426", sourcesOutputDir);
        files = getFiles(sourcesOutputDir);
    }

    @Test
    public void generatedTypeTest() {
        verifyReturnType("FooGrp", "getLeaf1", Types.objectType());
        verifyReturnType("BarGrp", "getLeaf1", Types.STRING);
        verifyReturnType("BarCont", "getLeaf1", Types.STRING);
        verifyReturnType("BazCont", "getLeaf1", Types.STRING);
        verifyReturnType("BazGrp", "getLeaf1", Types.STRING);
    }

    @Test
    public void fooGrpDataObjectTest() throws IOException {
        File file = files.get("FooGrp.java");
        assertNotNull(Files.readString(Path.of(file.getAbsolutePath())));
        final String content = Files.readString(Path.of(file.getAbsolutePath()));
        assertTrue(content.contains("@Nullable Object getLeaf1();"));
    }

    @Test
    public void barGrpDataObjectTest() throws IOException {
        File file = files.get("BarGrp.java");
        assertNotNull(Files.readString(Path.of(file.getAbsolutePath())));
        final String content = Files.readString(Path.of(file.getAbsolutePath()));
        assertTrue(content.contains("@Override\n"
                + "    @Nullable String getLeaf1();"));
    }

    @Test
    public void barContDataObjectTest() throws IOException {
        File file = files.get("BarCont.java");
        assertNotNull(Files.readString(Path.of(file.getAbsolutePath())));
        final String content = Files.readString(Path.of(file.getAbsolutePath()));
        // BarCont shouldn't override getLeaf1(), it's already overridden at BarGrp
        assertFalse(content.contains(" getLeaf1()"));
    }

    @Test
    public void bazGrpDataObjectTest() throws IOException {
        File file = files.get("BazGrp.java");
        assertNotNull(Files.readString(Path.of(file.getAbsolutePath())));
        final String content = Files.readString(Path.of(file.getAbsolutePath()));
        // BarCont shouldn't override getLeaf1(), it's already overridden at BarGrp
        assertFalse(content.contains(" getLeaf1()"));
    }

    @Test
    public void bazContDataObjectTest() throws IOException {
        File file = files.get("BazCont.java");
        assertNotNull(Files.readString(Path.of(file.getAbsolutePath())));
        final String content = Files.readString(Path.of(file.getAbsolutePath()));
        // BazCont shouldn't override getLeaf1(), it's already overridden at BarGrp
        assertFalse(content.contains(" getLeaf1()"));
    }

    @Test
    public void barContBuilderDataObjectTest() throws IOException {
        File file = files.get("BarContBuilder.java");
        assertNotNull(Files.readString(Path.of(file.getAbsolutePath())));
        final String content = Files.readString(Path.of(file.getAbsolutePath()));
        // BarGrp leaf1 doesn't need cast
        assertTrue(content.contains("public BarContBuilder(org.opendaylight.yang.gen.v1.mdsal426.norev.BarGrp arg) {\n"
                + "        this._leaf1 = arg.getLeaf1();\n"
                + "        this._name = arg.getName();\n"
                + "        this._leaf2 = arg.getLeaf2();\n"
                + "    }"));
        // FooGrp leaf1 return type is Object, cast needed
        assertTrue(content.contains("public BarContBuilder(org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp arg) {\n"
                + "        this._leaf1 = CodeHelpers.checkFieldCast(java.lang.String.class, arg.getLeaf1(), \"leaf1\");"
                + "\n"
                + "        this._leaf2 = arg.getLeaf2();\n"
                + "    }"));

        // cast FooGrp leaf1 property
        assertTrue(content.contains("public void fieldsFrom(DataObject arg) {\n"
                + "        boolean isValidArg = false;\n"
                + "        if (arg instanceof org.opendaylight.yang.gen.v1.mdsal426.norev.BarGrp) {\n"
                + "            this._name = ((org.opendaylight.yang.gen.v1.mdsal426.norev.BarGrp)arg).getName();\n"
                + "            isValidArg = true;\n"
                + "        }\n"
                + "        if (arg instanceof org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp) {\n"
                + "            this._leaf1 = CodeHelpers.checkFieldCast(java.lang.String.class,"
                + " ((org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp)arg).getLeaf1(), \"leaf1\");\n"
                + "            this._leaf2 = ((org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp)arg).getLeaf2();\n"
                + "            isValidArg = true;\n"
                + "        }\n"
                + "        CodeHelpers.validValue(isValidArg, arg,"
                + " \"[org.opendaylight.yang.gen.v1.mdsal426.norev.BarGrp,"
                + " org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp]\");\n"
                + "    }"));
    }

    @Test
    public void bazContBuilderDataObjectTest() throws IOException {
        File file = files.get("BazContBuilder.java");
        assertNotNull(Files.readString(Path.of(file.getAbsolutePath())));
        final String content = Files.readString(Path.of(file.getAbsolutePath()));
        // BazGrp leaf1 doesn't need cast
        assertTrue(content.contains("public BazContBuilder(org.opendaylight.yang.gen.v1.mdsal426.norev.BazGrp arg) {\n"
                + "        this._leaf1 = arg.getLeaf1();\n"
                + "        this._name = arg.getName();\n"
                + "        this._leaf2 = arg.getLeaf2();\n"
                + "    }"));
        // BarGrp leaf1 doesn't need cast
        assertTrue(content.contains("public BazContBuilder(org.opendaylight.yang.gen.v1.mdsal426.norev.BarGrp arg) {\n"
                + "        this._leaf1 = arg.getLeaf1();\n"
                + "        this._name = arg.getName();\n"
                + "        this._leaf2 = arg.getLeaf2();\n"
                + "    }"));
        // FooGrp leaf1 return type is Object, cast needed
        assertTrue(content.contains("public BazContBuilder(org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp arg) {\n"
                + "        this._leaf1 = CodeHelpers.checkFieldCast(java.lang.String.class, arg.getLeaf1(), \"leaf1\");"
                + "\n"
                + "        this._leaf2 = arg.getLeaf2();\n"
                + "    }"));
        // Skip checking if is arg instanceof BazGrp, because BazGrp has no own properties, cast FooGrp leaf1 property
        assertTrue(content.contains("public void fieldsFrom(DataObject arg) {\n"
                + "        boolean isValidArg = false;\n"
                + "        if (arg instanceof org.opendaylight.yang.gen.v1.mdsal426.norev.BarGrp) {\n"
                + "            this._name = ((org.opendaylight.yang.gen.v1.mdsal426.norev.BarGrp)arg).getName();\n"
                + "            isValidArg = true;\n"
                + "        }\n"
                + "        if (arg instanceof org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp) {\n"
                + "            this._leaf1 = CodeHelpers.checkFieldCast(java.lang.String.class,"
                + " ((org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp)arg).getLeaf1(), \"leaf1\");\n"
                + "            this._leaf2 = ((org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp)arg).getLeaf2();\n"
                + "            isValidArg = true;\n"
                + "        }\n"
                + "        "
                + "CodeHelpers.validValue(isValidArg, arg, \"[org.opendaylight.yang.gen.v1.mdsal426.norev.BazGrp,"
                + " org.opendaylight.yang.gen.v1.mdsal426.norev.BarGrp,"
                + " org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp]\");\n"
                + "    }"));
    }

    @Test
    public void compilationTest() {
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
    }

    @After
    public void after() {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private void verifyReturnType(final String typeName, final String getterName, final Type returnType) {
        final Type type = typeByName(types, typeName);
        assertNotNull(type);
        assertTrue(type instanceof GeneratedType);
        final GeneratedType generated = (GeneratedType)type;
        assertEquals(returnType, returnTypeByMethodName(generated, getterName));
    }

    private static Type typeByName(final List<Type> types, final String name) {
        for (Type type : types) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    private static Type returnTypeByMethodName(final GeneratedType type, final String name) {
        for (MethodSignature m : type.getMethodDefinitions()) {
            if (m.getName().equals(name)) {
                return m.getReturnType();
            }
        }
        return null;
    }

    private static String removeLineEndings(String str) {
        return str.replace("\n", "").replace("\r", "");
    }
}
