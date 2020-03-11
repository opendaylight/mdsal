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

public class SpecializingLeafrefGeneratedTypesTest extends BaseCompilationTest {
    private File sourcesOutputDir;
    private File compiledOutputDir;
    private List<Type> types;
    private Map<String, File> files;

    private static final Type LIST_STRING_TYPE  = Types.listTypeFor(Types.STRING);

    public static final String BAR_CONT = "BarCont";
    public static final String BOOLEAN_CONT = "BooleanCont";

    private static final String BAR_LST = "BarLst";
    private static final String BAZ_GRP = "BazGrp";
    private static final String FOO_GRP = "FooGrp";
    private static final String RESOLVED_LEAF_GRP = "ResolvedLeafGrp";
    private static final String RESOLVED_LEAFLIST_GRP = "ResolvedLeafListGrp";
    private static final String TRANSITIVE_GROUP = "TransitiveGroup";
    private static final String UNRESOLVED_GROUPING = "UnresolvedGrouping";

    private static final String GET_LEAF1_NAME = "getLeaf1";
    private static final String GET_LEAFLIST1_NAME = "getLeafList1";
    private static final String IS_LEAF1_NAME = "isLeaf1";

    private static final String GET_LEAF1_TYPE_OBJECT = "    @Nullable Object getLeaf1();";
    private static final String GET_LEAF1_TYPE_STRING = "    @Nullable String getLeaf1();";
    private static final String GET_LEAFLIST1_WILDCARD = "    @Nullable List<?> getLeafList1();";
    private static final String GET_LEAFLIST1_STRING = "    @Nullable List<String> getLeafList1();";
    private static final String GET_LEAFLIST1_DECLARATION = " getLeafList1();";
    private static final String GET_LEAF1_DECLARATION = " getLeaf1();";
    private static final String IS_LEAF1_BOOLEAN = "    @Nullable Boolean isLeaf1();";


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

    @After
    public void after() {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private String getFileContent(String fileName) throws IOException {
        final File file = files.get(getJavaFileName(fileName));
        assertNotNull(Files.lines(file.toPath()).findFirst());
        final String content = Files.readString(Path.of(file.getAbsolutePath()));
        assertNotNull(content);
        return content;
    }

    private String getJavaFileName(final String name) {
        return name + ".java";
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
        for (final Type type : types) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    private static Type returnTypeByMethodName(final GeneratedType type, final String name) {
        for (final MethodSignature m : type.getMethodDefinitions()) {
            if (m.getName().equals(name)) {
                return m.getReturnType();
            }
        }
        return null;
    }
}
