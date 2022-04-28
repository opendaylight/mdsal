package org.opendaylight.mdsal.binding.java.api.generator;

import org.junit.Test;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UnionWithMultipleIdentityrefsTest extends BaseCompilationTest {

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("union-with-multiple-identityrefs");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("union-with-multiple-identityrefs");
        generateTestSources("/compilation/union-with-multiple-identityrefs", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> identOneClass = Class.forName(CompilationTestUtils.BASE_PKG
                + ".urn.opendaylight.yang.union.test.rev160509.IdentOne", true, loader);
        Class<?> identTwoClass = Class.forName(CompilationTestUtils.BASE_PKG
                + ".urn.opendaylight.yang.union.test.rev160509.IdentTwo", true, loader);
        Class<?> unionTypeClass = Class.forName(CompilationTestUtils.BASE_PKG
                + ".urn.opendaylight.yang.union.test.rev160509.UnionType", true, loader);

        Object identOneValue = identOneClass.getDeclaredField(BindingMapping.VALUE_STATIC_FIELD_NAME).get(null);
        Object identTwoValue = identTwoClass.getDeclaredField(BindingMapping.VALUE_STATIC_FIELD_NAME).get(null);

        Constructor<?> unionTypeIdentOneConstructor = CompilationTestUtils.assertContainsConstructor(unionTypeClass,
                identOneClass);
        Constructor<?> unionTypeIdentTwoConstructor = CompilationTestUtils.assertContainsConstructor(unionTypeClass,
                identTwoClass);
        Object unionTypeOne = unionTypeIdentOneConstructor.newInstance(identOneValue);
        Object unionTypeTwo = unionTypeIdentTwoConstructor.newInstance(identTwoValue);

        Method getIdentityOne = unionTypeClass.getDeclaredMethod("getIdentOne");
        Object actualIdentityOne = getIdentityOne.invoke(unionTypeOne);
        assertEquals(identOneValue, actualIdentityOne);

        Method getIdentityTwo = unionTypeClass.getDeclaredMethod("getIdentTwo");
        Object actualIdentityTwo = getIdentityTwo.invoke(unionTypeTwo);
        assertEquals(identTwoValue, actualIdentityTwo);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
