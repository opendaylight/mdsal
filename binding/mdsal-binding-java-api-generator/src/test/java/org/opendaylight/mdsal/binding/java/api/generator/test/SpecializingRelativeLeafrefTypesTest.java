package org.opendaylight.mdsal.binding.java.api.generator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.mdsal.binding.java.api.generator.test.BaseCompilationTest.generateTestSources;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.Types;

public class SpecializingRelativeLeafrefTypesTest {
    private File sourcesOutputDir;
    private File compiledOutputDir;
    private List<Type> types;

    @Before
    public void before() throws IOException, URISyntaxException {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal426");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal426");
        types = generateTestSources("/compilation/mdsal426", sourcesOutputDir);
    }

    @Test
    public void generatedTypeTest() {
        verifyReturnType(types, "FooGrp", "getKeyLeaf1", Types.objectType());
        verifyReturnType(types, "BarGrp", "getKeyLeaf1", Types.STRING);
        verifyReturnType(types, "BarCont", "getKeyLeaf1", Types.STRING);
        verifyReturnType(types, "BzzCont", "getKeyLeaf1", Types.STRING);
        verifyReturnType(types, "BzzGrp", "getKeyLeaf1", Types.STRING);
    }

    @Test
    public void compilationTest() {
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
    }

    private static void verifyReturnType(final List<Type> types, final String typeName, final String getterName,
            final Type returnType) {
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
