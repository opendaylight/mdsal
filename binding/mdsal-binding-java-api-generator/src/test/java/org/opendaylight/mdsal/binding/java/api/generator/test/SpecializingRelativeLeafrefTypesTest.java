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
import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.doubleTab;
import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.getFiles;
import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.tab;
import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.tripleTab;

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
    private final String CLOSING_METHOD_BRACE = "}";
    private File sourcesOutputDir;
    private File compiledOutputDir;
    private List<Type> types;
    private Map<String, File> files;

    private static final Type LIST_STRING_TYPE  = Types.listTypeFor(Types.STRING);

    private static final String ARG_AS_FOO_GRP = "((org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp)arg)";
    private static final String ARG_AS_RESOLVED_LEAF_GRP =
            "((org.opendaylight.yang.gen.v1.mdsal426.norev.ResolvedLeafGrp)arg)";

    private static final String LS = System.lineSeparator();
    private static final String LEAF2_ASSIGNMENT = "this._leaf2 = arg.getLeaf2();";
    private static final String LIST_STRING_CAST_LEAFLIST1_ASSIGNMENT =
            "this._leafList1 = (List<String>)(arg.getLeafList1());";
    private final String CHECK_ARE_LEAFLIST_ITEMS_STRINGS = "CodeHelpers.checkListItemsType(java.lang.String.class, arg.getLeafList1(), \"leafList1\");";
    private final String BAR_CONT_BUILDER_FOO_GRP = "public BarContBuilder(org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp arg) {" ;
    private final String SUPPRESS_WARNINGS_UNCHECKED = "@SuppressWarnings(\"unchecked\")";
    private final String CONSTRUCTOR_BAR_CONT_BUILDER_RESOLVED_LEAF_GRP = "public BarContBuilder(org.opendaylight.yang.gen.v1.mdsal426.norev.ResolvedLeafGrp arg) {";
    private static final String FIELDS_FROM_SIGNATURE = "public void fieldsFrom(DataObject arg) {";
    private static final String TAB_IS_VALID_ARG_FALSE = TAB + "boolean isValidArg = false;";
    private static final String IS_VALID_ARG_TRUE = "boolean isValidArg = false;";
    private static final String IF_INSTANCEOF_FOO_GRP =
            "if (arg instanceof org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp) {";
    private static final String TO_STRING_CAST_LEAF1_ASSIGNMENT_FROM_FOO_GRP = "this._leaf1 ="
            + " CodeHelpers.checkedFieldCast(java.lang.String.class,"
            + " " + ARG_AS_FOO_GRP + ".getLeaf1(), \"leaf1\");";
    private static final String CHECK_IF_FOO_GRP_LIST_ITEMS_ARE_STRINGS =
            "CodeHelpers.checkListItemsType(java.lang.String.class,"
                    + " " + ARG_AS_FOO_GRP + ".getLeafList1(), \"leafList1\");";
    private static final String TO_LIST_STRING_CAST_LEAFLIST1_ASSIGNMENT_FROM_FOO_GRP =
            "this._leafList1 = (List<String>)" + ARG_AS_FOO_GRP + ".getLeafList1());";
    private static final String LEAF2_ASSIGNMENT_FROM_FOO_GRP = "this._leaf2 = " + ARG_AS_FOO_GRP + ".getLeaf2()";
    private static final String NAME_ASSIGNMENT_FROM_RESOLVED_LEAF_GRP = "this._name = " + ARG_AS_RESOLVED_LEAF_GRP + ".getName()";

    private static final String STRING_CAST_LEAF1_ASSIGNMENT = "this._leaf1 ="
            + " CodeHelpers.checkedFieldCast(java.lang.String.class, arg.getLeaf1(), \"leaf1\");" ;

    private static final String GET_LEAF1_NAME = "getLeaf1";
    private static final String GET_LEAFLIST1_NAME = "getLeafList1";
    private static final String IS_LEAF1_NAME = "isLeaf1";

    private static final String BAR_CONT = "BarCont";
    private static final String BAR_LST = "BarLst";
    private static final String BAZ_GRP = "BazGrp";
    private static final String BOOLEAN_CONT = "BooleanCont";
    private static final String FOO_GRP = "FooGrp";
    private static final String RESOLVED_LEAF_GRP = "ResolvedLeafGrp";
    private static final String RESOLVED_LEAFLIST_GRP = "ResolvedLeafListGrp";
    private static final String TRANSITIVE_GROUP = "TransitiveGroup";
    private static final String UNRESOLVED_GROUPING = "UnresolvedGrouping";

    private static final String GET_LEAF1_TYPE_OBJECT = "    @Nullable Object getLeaf1();";
    private static final String GET_LEAF1_TYPE_STRING = "    @Nullable String getLeaf1();";
    private static final String GET_LEAFLIST1_WILDCARD = "    @Nullable List<?> getLeafList1();";
    private static final String GET_LEAFLIST1_STRING = "    @Nullable List<String> getLeafList1();";
    private static final String GET_LEAFLIST1_DECLARATION = " getLeafList1();";
    private static final String GET_LEAF1_DECLARATION = " getLeaf1();";
    private static final String IS_LEAF1_BOOLEAN = "    @Nullable Boolean isLeaf1();";

    private static final String CONSTRUCTOR_LEAF1_ASSIGNMENT = "this._leaf1 = arg.getLeaf1();";
    private static final String CONSTRUCTOR_LEAFLIST1_ASSIGNMENT = "this._leafList1 = arg.getLeafList1();";
    private static final String CONSTRUCTOR_LEAF_2_ASSIGNMENT = "this._leaf2 = arg.getLeaf2();";
    private static final String CONSTRUCTOR_NAME_ASSIGNMENT = "this._name = arg.getName();";

    @Before
    public void before() throws IOException, URISyntaxException {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal426");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal426");
        types = generateTestSources("/compilation/mdsal426", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        files = getFiles(sourcesOutputDir);
    }

    @Test
    public void testGroupingWithUnresolvedLeafRefs() throws IOException {
        verifyReturnType(FOO_GRP, GET_LEAF1_NAME, Types.UNRESOLVED_LEAF_TYPE);
        verifyReturnType(FOO_GRP, GET_LEAFLIST1_NAME, Types.UNRESOLVED_LEAF_LIST_TYPE);

        final String content = getFileContent(FOO_GRP);

        assertTrue(content.contains(GET_LEAF1_TYPE_OBJECT));
        assertTrue(content.contains(GET_LEAFLIST1_WILDCARD));
    }

    @Test
    public void testLeafLeafrefPointsLeaf() throws IOException {
        verifyReturnType(RESOLVED_LEAF_GRP, GET_LEAF1_NAME, Types.STRING);

        final String content = getFileContent(RESOLVED_LEAF_GRP);

        assertTrue(content.contains(GET_LEAF1_TYPE_STRING));
    }

    @Test
    public void testLeafLeafrefPointsLeafList() throws IOException {
        verifyReturnType(RESOLVED_LEAFLIST_GRP, GET_LEAF1_NAME, Types.STRING);

        final String content = getFileContent(RESOLVED_LEAF_GRP);

        assertTrue(content.contains(GET_LEAF1_TYPE_STRING));
    }

    private String getFileContent(String fileName) throws IOException {
        File file = files.get(fileName + ".java");
        assertNotNull(Files.lines(file.toPath()).findFirst());
        final String content = Files.readString(Path.of(file.getAbsolutePath()));
        assertNotNull(content);
        return content;
    }

    private String getJavaFileName(final String name) {
        return name + ".java";
    }

    private String getJavaBuilderFileName(final String name) {
        return getJavaFileName(name + "Builder");
    }

    private String getBuilderFileContent(String interfaceName) throws IOException {
        return getFileContent(interfaceName + "Builder");
    }

    @Test
    public void testLeafListLeafrefPointsLeaf() throws IOException {
        verifyReturnType(RESOLVED_LEAF_GRP, GET_LEAFLIST1_NAME, LIST_STRING_TYPE);

        final String content = getFileContent(RESOLVED_LEAF_GRP);

        assertTrue(containsOverriddenGetter(content, GET_LEAFLIST1_STRING));
    }

    @Test
    public void testLeafListLeafrefPointsLeafList() throws IOException {
        verifyReturnType(RESOLVED_LEAFLIST_GRP, GET_LEAFLIST1_NAME, LIST_STRING_TYPE);

        final String content = getFileContent(RESOLVED_LEAFLIST_GRP);

        assertTrue(containsOverriddenGetter(content, GET_LEAFLIST1_STRING));
    }

    @Test
    public void testGroupingWhichInheritUnresolvedLeafrefAndDoesNotDefineIt() throws IOException {
        verifyMethodAbsence(TRANSITIVE_GROUP, GET_LEAF1_NAME);
        verifyMethodAbsence(TRANSITIVE_GROUP, GET_LEAFLIST1_NAME);

        final String content = getFileContent(TRANSITIVE_GROUP);

        assertFalse(content.contains(GET_LEAF1_DECLARATION));
        assertFalse(content.contains(GET_LEAFLIST1_DECLARATION));
    }

    @Test
    public void testLeafrefWhichPointsBoolean() throws IOException {
        verifyReturnType(UNRESOLVED_GROUPING, GET_LEAF1_NAME, Types.objectType());
        verifyMethodAbsence(BOOLEAN_CONT, GET_LEAF1_NAME);
        verifyReturnType(BOOLEAN_CONT, IS_LEAF1_NAME, Types.BOOLEAN);

        final String unresolvedGrouping = getFileContent(UNRESOLVED_GROUPING);
        final String booleanCont = getFileContent(BOOLEAN_CONT);

        assertTrue(containsNotOverriddenGetter(unresolvedGrouping, GET_LEAF1_TYPE_OBJECT));
        assertFalse(booleanCont.contains(GET_LEAF1_DECLARATION));
        assertTrue(containsNotOverriddenGetter(booleanCont, IS_LEAF1_BOOLEAN));
    }

    @Test
    public void testGroupingsUsageWhereLeafrefAlreadyResolved() throws IOException {
        leafList1AndLeaf1Absence(BAR_CONT);
        leafList1AndLeaf1Absence(BAR_LST);
        leafList1AndLeaf1Absence(BAZ_GRP);
    }

    private void leafList1AndLeaf1Absence(final String typeName) throws IOException {
        verifyMethodAbsence(typeName, GET_LEAF1_NAME);
        verifyMethodAbsence(typeName, GET_LEAFLIST1_NAME);

        final String content = getFileContent(typeName);

        assertFalse(content.contains(GET_LEAF1_DECLARATION));
        assertFalse(content.contains(GET_LEAFLIST1_DECLARATION));
    }

    private static boolean containsOverriddenGetter(final String fileContent, final String getterString) {
        return fileContent.contains("@Override" + System.lineSeparator() + getterString);
    }

    private static boolean containsNotOverriddenGetter(final String fileContent, final String getterString) {
        return !containsOverriddenGetter(fileContent, getterString) && fileContent.contains(getterString);
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
    public void barContBuilderDataObjectTest() throws IOException {
        final File file = files.get(getJavaBuilderFileName(BAR_CONT));
        final String content = Files.readString(file.toPath());

        barContBuilderConstructorResolvedLeafGrp(file, content);
        barContBuilderConstructorFooGrp(file, content);
        barContBuilderFieldsFrom(file, content);

        assertTrue(content.contains("@SuppressWarnings(\"unchecked\")"
                + "    public void fieldsFrom(DataObject arg) {"
                + "        boolean isValidArg = false;"
                + "        if (arg instanceof org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp) {"

                + "            this._leaf1 = CodeHelpers.checkedFieldCast(java.lang.String.class,"
                + " ((org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp)arg).getLeaf1(), \"leaf1\");"

                + "            CodeHelpers.checkListItemsType(java.lang.String.class,"
                + " ((org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp)arg).getLeafList1(), \"leafList1\");"

                + "            this._leafList1 = (List<String>)"
                + "(((org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp)arg).getLeafList1());"

                + "            this._leaf2 = ((org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp)arg).getLeaf2();"

                + "            isValidArg = true;"
                + "        }"
                + "        if (arg instanceof org.opendaylight.yang.gen.v1.mdsal426.norev.ResolvedLeafGrp) {"

                + "            this._name ="
                + " ((org.opendaylight.yang.gen.v1.mdsal426.norev.ResolvedLeafGrp)arg).getName();"

                + "            isValidArg = true;"
                + "        }"
                + "        CodeHelpers.validValue(isValidArg, arg,"
                + " \"[org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp,"
                + " org.opendaylight.yang.gen.v1.mdsal426.norev.ResolvedLeafGrp]\");"
                + CLOSING_METHOD_BRACE));
    }

    private void barContBuilderConstructorResolvedLeafGrp(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                tab(CONSTRUCTOR_BAR_CONT_BUILDER_RESOLVED_LEAF_GRP),
                doubleTab(CONSTRUCTOR_LEAF1_ASSIGNMENT),
                doubleTab(CONSTRUCTOR_LEAFLIST1_ASSIGNMENT),
                doubleTab(CONSTRUCTOR_NAME_ASSIGNMENT),
                doubleTab(CONSTRUCTOR_LEAF_2_ASSIGNMENT),
                tab(CLOSING_METHOD_BRACE));
    }

    private void barContBuilderFieldsFrom(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                tab(SUPPRESS_WARNINGS_UNCHECKED),
                tab(FIELDS_FROM_SIGNATURE),
                doubleTab(IS_VALID_ARG_FALSE),
                doubleTab(IF_INSTANCEOF_FOO_GRP),
                tripleTab(TO_STRING_CAST_LEAF1_ASSIGNMENT_FROM_FOO_GRP),
                tripleTab(CHECK_IF_FOO_GRP_LIST_ITEMS_ARE_STRINGS),
                tripleTab(TO_LIST_STRING_CAST_LEAFLIST1_ASSIGNMENT_FROM_FOO_GRP),
                tripleTab(LEAF2_ASSIGNMENT),
                tripleTab(LEAF2_ASSIGNMENT),
                tab(CLOSING_METHOD_BRACE));
    }

    private void barContBuilderConstructorFooGrp(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                tab(SUPPRESS_WARNINGS_UNCHECKED),
                tab(BAR_CONT_BUILDER_FOO_GRP),
                doubleTab(STRING_CAST_LEAF1_ASSIGNMENT),
                doubleTab(CHECK_ARE_LEAFLIST_ITEMS_STRINGS),
                doubleTab(LIST_STRING_CAST_LEAFLIST1_ASSIGNMENT),
                doubleTab(LEAF2_ASSIGNMENT),
                tab(CLOSING_METHOD_BRACE));
    }

    public void booleanContBuilderDataObjectTest(final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(new File("/"), content,
                tab(CONSTRUCTOR_BAR_CONT_BUILDER_RESOLVED_LEAF_GRP),
                doubleTab(CONSTRUCTOR_LEAF1_ASSIGNMENT),
                doubleTab(CONSTRUCTOR_LEAFLIST1_ASSIGNMENT),
                doubleTab(CONSTRUCTOR_NAME_ASSIGNMENT),
                doubleTab(CONSTRUCTOR_LEAF_2_ASSIGNMENT),
                tab(CLOSING_METHOD_BRACE));
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
                + CLOSING_METHOD_BRACE));
        // BarGrp leaf1 doesn't need cast
        assertTrue(content.contains("public BazContBuilder(org.opendaylight.yang.gen.v1.mdsal426.norev.BarGrp arg) {\n"
                + "        this._leaf1 = arg.getLeaf1();\n"
                + "        this._name = arg.getName();\n"
                + "        this._leaf2 = arg.getLeaf2();\n"
                + CLOSING_METHOD_BRACE));
        // FooGrp leaf1 return type is Object, cast needed
        assertTrue(content.contains("public BazContBuilder(org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp arg) {\n"
                + "        this._leaf1 = CodeHelpers.checkFieldCast(java.lang.String.class, arg.getLeaf1(), \"leaf1\");"
                + "\n"
                + "        this._leaf2 = arg.getLeaf2();\n"
                + CLOSING_METHOD_BRACE));
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
                + CLOSING_METHOD_BRACE));
    }

    @Test
    public void compilationTest() {
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
    }

    @After
    public void after() {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private void verifyMethodAbsence(final String typeName, final String getterName) {
        verifyReturnType(typeName, getterName, null);
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

}
