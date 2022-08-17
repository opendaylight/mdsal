/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.BASE_PKG;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.NS_FOO;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.BitsTypeObject;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Test correct code generation.
 */
public class TypedefCompilationTest extends BaseCompilationTest {
    private static final String VAL = "_value";
    private static final String GET_VAL = "getValue";
    private static final String UNITS = "_UNITS";

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("typedef");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("typedef");
        generateTestSources("/compilation/typedef", sourcesOutputDir);

        final File parent = new File(sourcesOutputDir, NS_FOO);
        assertTrue(new File(parent, "BitsExt.java").exists());
        assertTrue(new File(parent, "BitsExtRestricted.java").exists());
        assertTrue(new File(parent, "ByteTypeLong.java").exists());
        assertTrue(new File(parent, "ByteTypeIntArray.java").exists());
        assertTrue(new File(parent, "Int32Ext1.java").exists());
        assertTrue(new File(parent, "Int32Ext2.java").exists());
        assertTrue(new File(parent, "MyDecimalType.java").exists());
        assertTrue(new File(parent, "StringExt1.java").exists());
        assertTrue(new File(parent, "StringExt2.java").exists());
        assertTrue(new File(parent, "StringExt3.java").exists());
        assertTrue(new File(parent, "UnionExt1.java").exists());
        assertTrue(new File(parent, "UnionExt2.java").exists());
        assertTrue(new File(parent, "UnionExt3.java").exists());
        assertTrue(new File(parent, "UnionExt4.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 34);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        String pkg = BASE_PKG + ".urn.opendaylight.foo.rev131008";
        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        final Class<?> bitsExtClass = Class.forName(pkg + ".BitsExt", true, loader);
        final Class<?> bitsExtClassRestricted = Class.forName(pkg + ".BitsExtRestricted", true, loader);
        final Class<?> byteTypeLongClass = Class.forName(pkg + ".ByteTypeLong", true, loader);
        final Class<?> byteTypeIntArrayClass = Class.forName(pkg + ".ByteTypeIntArray", true, loader);
        final Class<?> int32Ext1Class = Class.forName(pkg + ".Int32Ext1", true, loader);
        final Class<?> int32Ext2Class = Class.forName(pkg + ".Int32Ext2", true, loader);
        final Class<?> myDecimalTypeClass = Class.forName(pkg + ".MyDecimalType", true, loader);
        final Class<?> myDecimalType2Class = Class.forName(pkg + ".MyDecimalType2", true, loader);
        final Class<?> stringExt1Class = Class.forName(pkg + ".StringExt1", true, loader);
        final Class<?> stringExt2Class = Class.forName(pkg + ".StringExt2", true, loader);
        final Class<?> stringExt3Class = Class.forName(pkg + ".StringExt3", true, loader);
        final Class<?> unionExt1Class = Class.forName(pkg + ".UnionExt1", true, loader);
        final Class<?> unionExt2Class = Class.forName(pkg + ".UnionExt2", true, loader);
        final Class<?> unionExt3Class = Class.forName(pkg + ".UnionExt3", true, loader);
        final Class<?> unionExt4Class = Class.forName(pkg + ".UnionExt4", true, loader);

        // typedef bits-ext
        assertFalse(bitsExtClass.isInterface());
        CompilationTestUtils.assertContainsField(bitsExtClass, "_bits", int.class);
        CompilationTestUtils.assertContainsFieldWithValue(bitsExtClass, "serialVersionUID", Long.TYPE,
                4838737988022869910L);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_bits", int.class);
        CompilationTestUtils.assertImplementsIfc(bitsExtClass, BitsTypeObject.class);
        CompilationTestUtils.assertContainsConstructor(bitsExtClass, bitsExtClass);
        CompilationTestUtils.assertContainsDefaultMethods(bitsExtClass);
        assertEquals(3, bitsExtClass.getDeclaredFields().length);
        assertEquals(2, bitsExtClass.getConstructors().length);
        assertEquals(16, bitsExtClass.getDeclaredMethods().length);

        Class<?> classBuilder = CompilationTestUtils.assertContainsBuilderClass(bitsExtClass, loader);
        Method builder = CompilationTestUtils.assertContainsMethod(bitsExtClass, classBuilder, "builder");
        Constructor<?> expectedConstructor = CompilationTestUtils.assertContainsConstructor(bitsExtClass,
                classBuilder);
        boolean[] expectedGetValueOutput = new boolean[]{false, false, false, false, false, true, false};
        Object builderObj = builder.invoke(null);
        assertEquals(9, classBuilder.getDeclaredMethods().length);
        classBuilder.getMethod("setSfmof", boolean.class).invoke(builderObj, true);
        Object obj = expectedConstructor.newInstance(builderObj);
        Method getValue = CompilationTestUtils.assertContainsMethod(bitsExtClass, boolean[].class, "getValue");
        assertEquals(Arrays.toString(expectedGetValueOutput), Arrays.toString((boolean[]) getValue.invoke(obj)));
        Method stringValue = CompilationTestUtils.assertContainsMethod(bitsExtClass, List.class, "stringValue");
        List<String> expectedBits = List.of("pc", "bpc", "dpc", "lbpc", "spc", "sfmof", "sfapc");
        assertEquals(expectedBits, stringValue.invoke(obj));
        Method validValues = CompilationTestUtils.assertContainsMethod(bitsExtClass, ImmutableSet.class,
                "validValues");
        assertEquals(ImmutableSet.copyOf(expectedBits), validValues.invoke(obj));
        Method valueOf = CompilationTestUtils.assertContainsMethod(bitsExtClass, bitsExtClass, "valueOf", List.class);
        assertEquals(obj, valueOf.invoke(obj, List.of("sfmof")));

        // typedef bits-ext-restricted
        expectedBits = Lists.newArrayList("pc", "bpc", "dpc");
        expectedGetValueOutput = new boolean[]{false, true, false};
        CompilationTestUtils.assertExtendsClass(bitsExtClassRestricted, bitsExtClass);
        CompilationTestUtils.assertImplementsIfc(bitsExtClassRestricted, BitsTypeObject.class);
        CompilationTestUtils.assertContainsFieldWithValue(bitsExtClassRestricted, "serialVersionUID", Long.TYPE,
                6365564322513459519L);
        CompilationTestUtils.assertContainsFieldWithValue(bitsExtClassRestricted, "VALID_BITS", ImmutableSet.class,
                ImmutableSet.copyOf(expectedBits));
        assertEquals(2, bitsExtClassRestricted.getDeclaredFields().length);
        assertEquals(3, bitsExtClassRestricted.getConstructors().length);
        assertEquals(8, bitsExtClassRestricted.getDeclaredMethods().length);
        classBuilder = CompilationTestUtils.assertContainsBuilderClass(bitsExtClassRestricted, loader);
        builder = CompilationTestUtils.assertContainsMethod(bitsExtClassRestricted, classBuilder, "builder");
        builderObj = builder.invoke(null);
        getValue = CompilationTestUtils.assertContainsMethod(bitsExtClassRestricted, boolean[].class, "getValue");
        validValues = CompilationTestUtils.assertContainsMethod(bitsExtClassRestricted, ImmutableSet.class,
                "validValues");
        valueOf = CompilationTestUtils.assertContainsMethod(bitsExtClassRestricted, bitsExtClassRestricted,
                "valueOf", List.class);
        stringValue = CompilationTestUtils.assertContainsMethod(bitsExtClassRestricted, List.class, "stringValue");
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(bitsExtClassRestricted, classBuilder);
        classBuilder.getMethod("setBpc", boolean.class).invoke(builderObj, true);
        obj = expectedConstructor.newInstance(builderObj);
        assertEquals(10, classBuilder.getDeclaredMethods().length);
        assertEquals(Arrays.toString(expectedGetValueOutput), Arrays.toString((boolean[]) getValue.invoke(obj)));
        assertEquals(ImmutableSet.copyOf(expectedBits), validValues.invoke(obj));
        assertEquals(obj, valueOf.invoke(obj, List.of("bpc")));
        assertEquals(expectedBits, stringValue.invoke(obj));

        // typedef byte-type-long
        int bitsSize = 40;
        List<String> expectedValidBits = new ArrayList<>();
        boolean[] expectedBooleans = new boolean[bitsSize];
        expectedBooleans[37] = true;
        IntStream.range(0, bitsSize).forEach(idx -> expectedValidBits.add("bit" + idx));

        CompilationTestUtils.assertImplementsIfc(byteTypeLongClass, BitsTypeObject.class);
        CompilationTestUtils.assertContainsField(byteTypeLongClass, "_bits", long.class);
        CompilationTestUtils.assertContainsFieldWithValue(byteTypeLongClass, "serialVersionUID", Long.TYPE,
                1108719943834914164L);
        CompilationTestUtils.assertContainsFieldWithValue(byteTypeLongClass, "VALID_BITS", ImmutableSet.class,
                ImmutableSet.copyOf(expectedValidBits));
        CompilationTestUtils.assertContainsConstructor(byteTypeLongClass, byteTypeLongClass);
        CompilationTestUtils.assertContainsDefaultMethods(byteTypeLongClass);
        assertFalse(byteTypeLongClass.isInterface());
        assertEquals(3, byteTypeLongClass.getDeclaredFields().length);
        assertEquals(2, byteTypeLongClass.getConstructors().length);
        assertEquals(49, byteTypeLongClass.getDeclaredMethods().length);
        classBuilder = CompilationTestUtils.assertContainsBuilderClass(byteTypeLongClass, loader);
        builder = CompilationTestUtils.assertContainsMethod(byteTypeLongClass, classBuilder, "builder");
        builderObj = builder.invoke(null);
        getValue = CompilationTestUtils.assertContainsMethod(byteTypeLongClass, boolean[].class, "getValue");
        validValues = CompilationTestUtils.assertContainsMethod(byteTypeLongClass, ImmutableSet.class, "validValues");
        valueOf = CompilationTestUtils.assertContainsMethod(byteTypeLongClass, byteTypeLongClass,"valueOf",
                List.class);
        stringValue = CompilationTestUtils.assertContainsMethod(byteTypeLongClass, List.class, "stringValue");
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(byteTypeLongClass, classBuilder);
        assertEquals(42, classBuilder.getDeclaredMethods().length);
        classBuilder.getMethod("setBit37", boolean.class).invoke(builderObj, true);
        obj = expectedConstructor.newInstance(builderObj);
        assertEquals(obj, valueOf.invoke(obj, List.of("bit37")));
        assertEquals(ImmutableSet.copyOf(expectedValidBits), validValues.invoke(obj));
        assertEquals(expectedValidBits, stringValue.invoke(obj));
        assertEquals(Arrays.toString(expectedBooleans), Arrays.toString((boolean[]) getValue.invoke(obj)));

        // typedef byte-type-int-array
        bitsSize = 70;
        expectedBooleans = new boolean[bitsSize];
        expectedBooleans[69] = true;
        expectedValidBits.clear();
        IntStream.range(0, bitsSize).forEach(idx -> expectedValidBits.add("bit" + idx));
        CompilationTestUtils.assertImplementsIfc(byteTypeIntArrayClass, BitsTypeObject.class);
        CompilationTestUtils.assertContainsField(byteTypeIntArrayClass, "_bits", int[].class);
        CompilationTestUtils.assertContainsFieldWithValue(byteTypeIntArrayClass, "serialVersionUID", Long.TYPE,
                4048474356046169047L);
        CompilationTestUtils.assertContainsFieldWithValue(byteTypeIntArrayClass, "VALID_BITS", ImmutableSet.class,
                ImmutableSet.copyOf(expectedValidBits));
        CompilationTestUtils.assertContainsConstructor(byteTypeIntArrayClass, byteTypeIntArrayClass);
        CompilationTestUtils.assertContainsDefaultMethods(byteTypeIntArrayClass);
        assertFalse(byteTypeIntArrayClass.isInterface());
        assertEquals(3, byteTypeIntArrayClass.getDeclaredFields().length);
        assertEquals(2, byteTypeIntArrayClass.getConstructors().length);
        assertEquals(79, byteTypeIntArrayClass.getDeclaredMethods().length);
        classBuilder = CompilationTestUtils.assertContainsBuilderClass(byteTypeIntArrayClass, loader);
        builder = CompilationTestUtils.assertContainsMethod(byteTypeIntArrayClass, classBuilder, "builder");
        builderObj = builder.invoke(null);
        getValue = CompilationTestUtils.assertContainsMethod(byteTypeIntArrayClass, boolean[].class, "getValue");
        validValues = CompilationTestUtils.assertContainsMethod(byteTypeIntArrayClass, ImmutableSet.class,
                "validValues");
        valueOf = CompilationTestUtils.assertContainsMethod(byteTypeIntArrayClass, byteTypeIntArrayClass,"valueOf",
                List.class);
        stringValue = CompilationTestUtils.assertContainsMethod(byteTypeIntArrayClass, List.class, "stringValue");
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(byteTypeIntArrayClass, classBuilder);
        assertEquals(72, classBuilder.getDeclaredMethods().length);
        classBuilder.getMethod("setBit69", boolean.class).invoke(builderObj, true);
        obj = expectedConstructor.newInstance(builderObj);
        assertEquals(obj, valueOf.invoke(obj, List.of("bit69")));
        assertEquals(ImmutableSet.copyOf(expectedValidBits), validValues.invoke(obj));
        assertEquals(expectedValidBits, stringValue.invoke(obj));
        assertEquals(Arrays.toString(expectedBooleans), Arrays.toString((boolean[]) getValue.invoke(obj)));

        // typedef int32-ext1
        assertFalse(int32Ext1Class.isInterface());
        CompilationTestUtils.assertContainsField(int32Ext1Class, VAL, Integer.class);
        CompilationTestUtils.assertContainsFieldWithValue(int32Ext1Class, "serialVersionUID", Long.TYPE,
            5351634010010233292L, Integer.class);
        assertEquals(2, int32Ext1Class.getDeclaredFields().length);

        expectedConstructor = CompilationTestUtils.assertContainsConstructor(int32Ext1Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(int32Ext1Class, int32Ext1Class);
        assertEquals(2, int32Ext1Class.getConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(int32Ext1Class);
        CompilationTestUtils.assertContainsMethod(int32Ext1Class, Integer.class, GET_VAL);
        assertEquals(7, int32Ext1Class.getDeclaredMethods().length);

        List<Range<Integer>> rangeConstraints = new ArrayList<>();
        rangeConstraints.add(Range.closed(2, 2147483647));
        Object arg = 1;
        String expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, rangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        Method defInst = CompilationTestUtils.assertContainsMethod(int32Ext1Class, int32Ext1Class, "getDefaultInstance",
                String.class);
        obj = expectedConstructor.newInstance(159);
        assertEquals(obj, defInst.invoke(null, "159"));

        // typedef int32-ext2
        assertFalse(int32Ext2Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(int32Ext2Class, UNITS, String.class, "mile", Integer.class);
        CompilationTestUtils.assertContainsFieldWithValue(int32Ext2Class, "serialVersionUID", Long.TYPE,
            317831889060130988L, Integer.class);
        assertEquals(2, int32Ext2Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(int32Ext2Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(int32Ext2Class, int32Ext2Class);
        CompilationTestUtils.assertContainsConstructor(int32Ext2Class, int32Ext1Class);
        assertEquals(3, int32Ext2Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(int32Ext2Class, String.class, "toString");
        defInst = CompilationTestUtils.assertContainsMethod(int32Ext2Class, int32Ext2Class, "getDefaultInstance",
            String.class);
        assertEquals(3, int32Ext2Class.getDeclaredMethods().length);

        rangeConstraints.clear();
        rangeConstraints.add(Range.closed(3, 9));
        rangeConstraints.add(Range.closed(11, 2147483647));
        arg = Integer.valueOf("10");
        expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, rangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance(2147483647);
        assertEquals(obj, defInst.invoke(null, "2147483647"));

        // typedef string-ext1
        assertFalse(stringExt1Class.isInterface());
        CompilationTestUtils.assertContainsField(stringExt1Class, VAL, String.class);
        CompilationTestUtils.assertContainsField(stringExt1Class, "patterns", Pattern.class);
        CompilationTestUtils.assertContainsField(stringExt1Class, "PATTERN_CONSTANTS", List.class);
        CompilationTestUtils.assertContainsFieldWithValue(stringExt1Class, "serialVersionUID", Long.TYPE,
            6943827552297110991L, String.class);
        assertEquals(5, stringExt1Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(stringExt1Class, String.class);
        CompilationTestUtils.assertContainsConstructor(stringExt1Class, stringExt1Class);
        assertEquals(2, stringExt1Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(stringExt1Class, String.class, GET_VAL);
        defInst = CompilationTestUtils.assertContainsMethod(stringExt1Class, stringExt1Class, "getDefaultInstance",
            String.class);
        CompilationTestUtils.assertContainsDefaultMethods(stringExt1Class);
        assertEquals(7, stringExt1Class.getDeclaredMethods().length);

        List<Range<Integer>> lengthConstraints = new ArrayList<>();
        lengthConstraints.add(Range.closed(5, 11));
        arg = "abcd";
        expectedMsg = String.format("Invalid length: %s, expected: %s.", arg, lengthConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);

        obj = expectedConstructor.newInstance("abcde");
        assertEquals(obj, defInst.invoke(null, "abcde"));

        // typedef string-ext2
        assertFalse(stringExt2Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(stringExt2Class, "serialVersionUID", Long.TYPE,
            8100233177432072092L, String.class);
        assertEquals(1, stringExt2Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(stringExt2Class, String.class);
        CompilationTestUtils.assertContainsConstructor(stringExt2Class, stringExt2Class);
        CompilationTestUtils.assertContainsConstructor(stringExt2Class, stringExt1Class);
        assertEquals(3, stringExt2Class.getDeclaredConstructors().length);
        defInst = CompilationTestUtils.assertContainsMethod(stringExt2Class, stringExt2Class, "getDefaultInstance",
            String.class);
        assertEquals(2, stringExt2Class.getDeclaredMethods().length);

        lengthConstraints.clear();
        lengthConstraints.add(Range.closed(6, 10));
        arg = "abcde";
        expectedMsg = String.format("Invalid length: %s, expected: %s.", arg, lengthConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance("abcdef");
        assertEquals(obj, defInst.invoke(null, "abcdef"));

        // typedef string-ext3
        assertFalse(stringExt3Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(stringExt3Class, "serialVersionUID", Long.TYPE,
            -2751063130555484180L, String.class);
        assertEquals(4, stringExt3Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(stringExt3Class, String.class);
        CompilationTestUtils.assertContainsConstructor(stringExt3Class, stringExt3Class);
        CompilationTestUtils.assertContainsConstructor(stringExt3Class, stringExt2Class);
        assertEquals(3, stringExt3Class.getDeclaredConstructors().length);
        defInst = CompilationTestUtils.assertContainsMethod(stringExt3Class, stringExt3Class, "getDefaultInstance",
            String.class);
        assertEquals(1, stringExt3Class.getDeclaredMethods().length);

        obj = expectedConstructor.newInstance("bbbbbb");
        assertEquals(obj, defInst.invoke(null, "bbbbbb"));

        // typedef my-decimal-type
        assertFalse(myDecimalTypeClass.isInterface());
        CompilationTestUtils.assertContainsField(myDecimalTypeClass, VAL, Decimal64.class);
        CompilationTestUtils.assertContainsFieldWithValue(myDecimalTypeClass, "serialVersionUID", Long.TYPE,
            3143735729419861095L, Decimal64.class);
        assertEquals(3, myDecimalTypeClass.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(myDecimalTypeClass, Decimal64.class, "getValue");
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(myDecimalTypeClass, Decimal64.class);
        CompilationTestUtils.assertContainsConstructor(myDecimalTypeClass, myDecimalTypeClass);
        assertEquals(2, myDecimalTypeClass.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(myDecimalTypeClass, Decimal64.class, GET_VAL);
        CompilationTestUtils.assertContainsDefaultMethods(myDecimalTypeClass);
        defInst = CompilationTestUtils.assertContainsMethod(myDecimalTypeClass, myDecimalTypeClass,
            "getDefaultInstance", String.class);
        assertEquals(7, myDecimalTypeClass.getDeclaredMethods().length);

        List<Range<Decimal64>> decimalRangeConstraints = new ArrayList<>();
        decimalRangeConstraints.add(Range.closed(Decimal64.valueOf("1.5"), Decimal64.valueOf("5.5")));
        arg = Decimal64.valueOf("1.4");
        expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, decimalRangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance(Decimal64.valueOf("3.14"));
        assertEquals(obj, defInst.invoke(null, "3.14"));

        // typedef my-decimal-type2
        assertFalse(myDecimalType2Class.isInterface());
        CompilationTestUtils.assertContainsField(myDecimalType2Class, VAL, Decimal64.class);
        CompilationTestUtils.assertContainsFieldWithValue(myDecimalType2Class, "serialVersionUID", Long.TYPE,
            -672265764962082714L, Decimal64.class);
        assertEquals(3, myDecimalType2Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(myDecimalType2Class, Decimal64.class, "getValue");
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(myDecimalType2Class, Decimal64.class);
        CompilationTestUtils.assertContainsConstructor(myDecimalType2Class, myDecimalType2Class);
        assertEquals(2, myDecimalType2Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(myDecimalType2Class, Decimal64.class, GET_VAL);
        CompilationTestUtils.assertContainsDefaultMethods(myDecimalType2Class);
        defInst = CompilationTestUtils.assertContainsMethod(myDecimalType2Class, myDecimalType2Class,
            "getDefaultInstance", String.class);
        assertEquals(7, myDecimalType2Class.getDeclaredMethods().length);

        List<Range<Decimal64>> decimal2RangeConstraints = new ArrayList<>();
        decimal2RangeConstraints.add(Range.closed(Decimal64.valueOf("0.0"), Decimal64.valueOf("1.0")));
        arg = Decimal64.valueOf("1.4");
        expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, decimal2RangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance(Decimal64.valueOf("0.14"));
        assertEquals(obj, defInst.invoke(null, "0.14"));

        // typedef union-ext1
        assertFalse(unionExt1Class.isInterface());
        CompilationTestUtils.assertContainsField(unionExt1Class, "_int16", Short.class);
        CompilationTestUtils.assertContainsField(unionExt1Class, "_int32", Integer.class);
        CompilationTestUtils.assertContainsFieldWithValue(unionExt1Class, "serialVersionUID", Long.TYPE,
            -6955858981055390623L, new Class<?>[] { Short.class }, Short.valueOf("1"));
        assertEquals(3, unionExt1Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(unionExt1Class, Short.class, "getInt16");
        CompilationTestUtils.assertContainsMethod(unionExt1Class, Integer.class, "getInt32");
        CompilationTestUtils.assertContainsConstructor(unionExt1Class, Short.class);
        CompilationTestUtils.assertContainsConstructor(unionExt1Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(unionExt1Class, unionExt1Class);
        assertEquals(3, unionExt1Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(unionExt1Class);

        // typedef union-ext2
        assertFalse(unionExt2Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(unionExt2Class, "serialVersionUID", Long.TYPE,
            -8833407459073585206L, new Class<?>[] { Short.class }, Short.valueOf("1"));
        assertEquals(1, unionExt2Class.getDeclaredFields().length);
        assertEquals(1, unionExt2Class.getDeclaredMethods().length);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, Short.class);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, unionExt2Class);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, unionExt1Class);
        assertEquals(4, unionExt2Class.getDeclaredConstructors().length);

        // typedef union-ext3
        assertFalse(unionExt3Class.isInterface());
        CompilationTestUtils.assertContainsField(unionExt3Class, "_string", String.class);
        CompilationTestUtils.assertContainsField(unionExt3Class, "_unionExt2", unionExt2Class);
        CompilationTestUtils.assertContainsFieldWithValue(unionExt3Class, UNITS, String.class, "object id",
            new Class<?>[] { String.class }, "");
        CompilationTestUtils.assertContainsFieldWithValue(unionExt3Class, "serialVersionUID", Long.TYPE,
            -1558836942803815106L, new Class<?>[] { String.class }, "");
        assertEquals(4, unionExt3Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(unionExt3Class, String.class, "getString");
        CompilationTestUtils.assertContainsMethod(unionExt3Class, unionExt2Class, "getUnionExt2");
        CompilationTestUtils.assertContainsConstructor(unionExt3Class, String.class);
        CompilationTestUtils.assertContainsConstructor(unionExt3Class, unionExt2Class);
        CompilationTestUtils.assertContainsConstructor(unionExt3Class, unionExt3Class);
        assertEquals(3, unionExt3Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(unionExt3Class);

        // typedef union-ext4
        assertFalse(unionExt4Class.isInterface());
        CompilationTestUtils.assertContainsField(unionExt4Class, "_unionExt3", unionExt3Class);
        CompilationTestUtils.assertContainsField(unionExt4Class, "_int32Ext2", int32Ext2Class);
        CompilationTestUtils.assertContainsField(unionExt4Class, "_empty", Empty.class);
        CompilationTestUtils.assertContainsField(unionExt4Class, "_myDecimalType", myDecimalTypeClass);
        CompilationTestUtils.assertContainsFieldWithValue(unionExt4Class, "serialVersionUID", Long.TYPE,
            8089656970520476667L, new Class<?>[] { Boolean.class }, false);
        assertEquals(5, unionExt4Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(unionExt4Class, unionExt3Class, "getUnionExt3");
        CompilationTestUtils.assertContainsMethod(unionExt4Class, int32Ext2Class, "getInt32Ext2");
        CompilationTestUtils.assertContainsMethod(unionExt4Class, Empty.class, "getEmpty");
        CompilationTestUtils.assertContainsMethod(unionExt4Class, myDecimalTypeClass, "getMyDecimalType");
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, unionExt3Class);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, int32Ext2Class);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, Empty.class);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, myDecimalTypeClass);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, unionExt4Class);
        assertEquals(5, unionExt4Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(unionExt4Class);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
