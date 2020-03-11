/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.DOUBLE_TAB;
import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.TAB;
import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.TRIPLE_TAB;
import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.doubleTab;
import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.getFiles;
import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.tab;
import static org.opendaylight.mdsal.binding.java.api.generator.test.FileSearchUtil.tripleTab;
import static org.opendaylight.mdsal.binding.java.api.generator.test.SpecializingLeafrefGeneratedTypesTest.BAR_CONT;
import static org.opendaylight.mdsal.binding.java.api.generator.test.SpecializingLeafrefGeneratedTypesTest.BOOLEAN_CONT;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Type;

public class SpecializingLeafrefBindingsTest extends BaseCompilationTest {
    private File sourcesOutputDir;
    private File compiledOutputDir;
    private Map<String, File> files;

    private static final char CLOSING_METHOD_BRACE = '}';
    private static final String TAB_CLOSING_METHOD_BRACE = TAB + CLOSING_METHOD_BRACE;
    private static final String DTAB_CLOSING_METHOD_BRACE = DOUBLE_TAB + CLOSING_METHOD_BRACE;

    private static final String FOO_GRP_REF = "org.opendaylight.yang.gen.v1.mdsal426.norev.FooGrp";
    private static final String RESOLVED_LEAF_GRP_REF = "org.opendaylight.yang.gen.v1.mdsal426.norev.ResolvedLeafGrp";

    private static final String UNRESOLVED_GROUPING_REF =
            "org.opendaylight.yang.gen.v1.mdsal426.norev.UnresolvedGrouping";

    private static final String ARG_AS_FOO_GRP = "((" + FOO_GRP_REF + ")arg)";
    private static final String ARG_AS_RESOLVED_LEAF_GRP = "((" + RESOLVED_LEAF_GRP_REF + ")arg)";
    private static final String ARG_AS_UNRESOLVED_GROUPING_REF = "((" + UNRESOLVED_GROUPING_REF + ")arg)";

    private static final String CHECK_ARE_LEAFLIST_ITEMS_STRINGS =
            "CodeHelpers.checkListItemsType(java.lang.String.class, arg.getLeafList1(), \"leafList1\");";

    private static final String CHECK_IF_FOO_GRP_LIST_ITEMS_ARE_STRINGS =
            "CodeHelpers.checkListItemsType(java.lang.String.class, "
                    + ARG_AS_FOO_GRP + ".getLeafList1(), \"leafList1\");";

    private static final String CONSTR_BAR_CONT_BUILDER_FOO_GRP = "public BarContBuilder(" + FOO_GRP_REF + " arg) {" ;
    private static final String CONSTR_BAR_CONT_BUILDER_RESOLVED_LEAF_GRP =
            "public BarContBuilder(" + RESOLVED_LEAF_GRP_REF + " arg) {";
    private static final String CONSTR_BOOLEAN_CONT_BUILDER_UNRESOLVED_GROUPING =
            "public BooleanContBuilder(" + UNRESOLVED_GROUPING_REF + " arg) {";

    private static final String IF_INSTANCEOF_FOO_GRP = "if (arg instanceof " + FOO_GRP_REF + ") {";

    private static final String IF_INSTANCEOF_RESOLVED_LEAF_GRP =
            "if (arg instanceof " + RESOLVED_LEAF_GRP_REF + ") {";

    private static final String IF_INSTANCEOF_UNRESOLVED_GROUPING =
            "if (arg instanceof " + UNRESOLVED_GROUPING_REF + ") {";

    private static final String LEAF1_ASSIGNMENT = "this._leaf1 = arg.getLeaf1();";
    private static final String LEAF2_ASSIGNMENT = "this._leaf2 = arg.getLeaf2();";
    private static final String LEAF2_ASSIGNMENT_FROM_FOO_GRP = "this._leaf2 = " + ARG_AS_FOO_GRP + ".getLeaf2();";
    private static final String LEAFLIST1_ASSIGNMENT = "this._leafList1 = arg.getLeafList1();";
    private static final String NAME_ASSIGNMENT = "this._name = arg.getName();";

    private static final String NAME_ASSIGNMENT_FROM_RESOLVED_LEAF_GRP =
            "this._name = " + ARG_AS_RESOLVED_LEAF_GRP + ".getName();";

    private static final String TO_LIST_STRING_CAST_LEAFLIST1_ASSIGNMENT =
            "this._leafList1 = (List<String>)(arg.getLeafList1());";

    private static final String TO_LIST_STRING_CAST_LEAFLIST1_ASSIGNMENT_FROM_FOO_GRP =
            "this._leafList1 = (List<String>)(" + ARG_AS_FOO_GRP + ".getLeafList1());";

    private static final String TO_STRING_CAST_LEAF1_ASSIGNMENT = "this._leaf1 ="
            + " CodeHelpers.checkedFieldCast(java.lang.String.class, arg.getLeaf1(), \"leaf1\");" ;

    private static final String TO_STRING_CAST_LEAF1_ASSIGNMENT_FROM_FOO_GRP = "this._leaf1 ="
            + " CodeHelpers.checkedFieldCast(java.lang.String.class, "
            + ARG_AS_FOO_GRP + ".getLeaf1(), \"leaf1\");";

    private static final String TO_BOOLEAN_CAST_LEAF1_ASSIGNMENT_FROM_UNRESOLVED_GROUPING =
            "this._leaf1 = CodeHelpers.checkedFieldCast(java.lang.Boolean.class, "
                    + ARG_AS_UNRESOLVED_GROUPING_REF + ".getLeaf1(), \"leaf1\");";

    private static final String TO_BOOLEAN_CAST_LEAF1_ASSIGNMENT =
            "this._leaf1 = CodeHelpers.checkedFieldCast(java.lang.Boolean.class, arg.getLeaf1(), \"leaf1\");";

    private static final String VALID_VALUE_BAR_CONT = "CodeHelpers.validValue(isValidArg, arg,"
            + " \"[" + FOO_GRP_REF + ", " + RESOLVED_LEAF_GRP_REF + "]\");";

    private static final String VALID_VALUE_BOOLEAN_CONT = "CodeHelpers.validValue(isValidArg, arg,"
            + " \"[" + UNRESOLVED_GROUPING_REF + "]\");";

    private static final String TAB_SUPPRESS_WARNINGS_UNCHECKED = TAB + "@SuppressWarnings(\"unchecked\")";
    private static final String TAB_FIELDS_FROM_SIGNATURE = TAB + "public void fieldsFrom(DataObject arg) {";
    private static final String TTAB_SET_IS_VALID_ARG_TRUE = TRIPLE_TAB + "isValidArg = true;";
    private static final String DTAB_INIT_IS_VALID_ARG_FALSE = DOUBLE_TAB + "boolean isValidArg = false;";

    @Before
    public void before() throws IOException, URISyntaxException {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal426");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal426");
        generateTestSources("/compilation/mdsal426", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        files = getFiles(sourcesOutputDir);
    }

    @Test
    public void barContBuilderDataObjectTest() throws IOException {
        final File file = files.get(getJavaBuilderFileName(BAR_CONT));
        final String content = Files.readString(file.toPath());

        barContBuilderConstructorResolvedLeafGrpTest(file, content);
        barContBuilderConstructorFooGrpTest(file, content);
        barContBuilderFieldsFromTest(file, content);
    }

    private void barContBuilderConstructorResolvedLeafGrpTest(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                tab(CONSTR_BAR_CONT_BUILDER_RESOLVED_LEAF_GRP),
                doubleTab(LEAF1_ASSIGNMENT),
                doubleTab(LEAFLIST1_ASSIGNMENT),
                doubleTab(NAME_ASSIGNMENT),
                doubleTab(LEAF2_ASSIGNMENT),
                TAB_CLOSING_METHOD_BRACE);
    }

    private void barContBuilderFieldsFromTest(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                TAB_SUPPRESS_WARNINGS_UNCHECKED,
                TAB_FIELDS_FROM_SIGNATURE,
                DTAB_INIT_IS_VALID_ARG_FALSE,
                doubleTab(IF_INSTANCEOF_FOO_GRP),
                tripleTab(TO_STRING_CAST_LEAF1_ASSIGNMENT_FROM_FOO_GRP),
                tripleTab(CHECK_IF_FOO_GRP_LIST_ITEMS_ARE_STRINGS),
                tripleTab(TO_LIST_STRING_CAST_LEAFLIST1_ASSIGNMENT_FROM_FOO_GRP),
                tripleTab(LEAF2_ASSIGNMENT_FROM_FOO_GRP),
                TTAB_SET_IS_VALID_ARG_TRUE,
                DTAB_CLOSING_METHOD_BRACE,
                doubleTab(IF_INSTANCEOF_RESOLVED_LEAF_GRP),
                tripleTab(NAME_ASSIGNMENT_FROM_RESOLVED_LEAF_GRP),
                TTAB_SET_IS_VALID_ARG_TRUE,
                DTAB_CLOSING_METHOD_BRACE,
                doubleTab(VALID_VALUE_BAR_CONT),
                TAB_CLOSING_METHOD_BRACE);
    }

    private void barContBuilderConstructorFooGrpTest(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                TAB_SUPPRESS_WARNINGS_UNCHECKED,
                tab(CONSTR_BAR_CONT_BUILDER_FOO_GRP),
                doubleTab(TO_STRING_CAST_LEAF1_ASSIGNMENT),
                doubleTab(CHECK_ARE_LEAFLIST_ITEMS_STRINGS),
                doubleTab(TO_LIST_STRING_CAST_LEAFLIST1_ASSIGNMENT),
                doubleTab(LEAF2_ASSIGNMENT),
                TAB_CLOSING_METHOD_BRACE);
    }

    @Test
    public void booleanContBuilderDataObjectTest() throws IOException {
        final File file = files.get(getJavaBuilderFileName(BOOLEAN_CONT));
        final String content = Files.readString(file.toPath());

        booleanContBuilderFieldsFromTest(file, content);
        booleanContBuilderConstructorTest(file, content);
    }

    public void booleanContBuilderFieldsFromTest(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                TAB_FIELDS_FROM_SIGNATURE,
                DTAB_INIT_IS_VALID_ARG_FALSE,
                doubleTab(IF_INSTANCEOF_UNRESOLVED_GROUPING),
                tripleTab(TO_BOOLEAN_CAST_LEAF1_ASSIGNMENT_FROM_UNRESOLVED_GROUPING),
                TTAB_SET_IS_VALID_ARG_TRUE,
                DTAB_CLOSING_METHOD_BRACE,
                doubleTab(VALID_VALUE_BOOLEAN_CONT),
                TAB_CLOSING_METHOD_BRACE);
    }

    public void booleanContBuilderConstructorTest(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                tab(CONSTR_BOOLEAN_CONT_BUILDER_UNRESOLVED_GROUPING),
                doubleTab(TO_BOOLEAN_CAST_LEAF1_ASSIGNMENT),
                TAB_CLOSING_METHOD_BRACE);
    }

    @After
    public void after() {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private String getJavaFileName(final String name) {
        return name + ".java";
    }

    private String getJavaBuilderFileName(final String name) {
        return getJavaFileName(name + "Builder");
    }

}
