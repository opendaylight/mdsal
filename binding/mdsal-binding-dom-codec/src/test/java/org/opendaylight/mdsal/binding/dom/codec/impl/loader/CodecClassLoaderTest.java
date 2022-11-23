/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.dom.codec.impl.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.mdsal.binding.dom.codec.impl.loader.CodecClassLoader.ClassGenerator;
import org.opendaylight.mdsal.binding.dom.codec.impl.loader.CodecClassLoader.ClassNameBuilder;
import org.opendaylight.mdsal.binding.dom.codec.impl.loader.CodecClassLoader.GeneratorResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.Top;

public class CodecClassLoaderTest {

    private static ClassGenerator CLASS_GENERATOR = (loader, fqcn, bindingInterface) ->
            GeneratorResult.of(new ByteBuddy()
                    .subclass(Object.class)
                    .name(fqcn)
                    .method(ElementMatchers.isToString())
                    .intercept(FixedValue.value("test"))
                    .make());

    private static final Class<?> BINDING_INTERFACE = Top.class;

    private CodecClassLoader codecClassLoader;

    @BeforeEach
    void setup() {
        codecClassLoader = new RootCodecClassLoader();
    }

    @ParameterizedTest(name = "Generate class within namespace: {0}")
    @MethodSource("generateClassWithinNamespaceArgs")
    void generateClassWithinNamespace(final ClassNameBuilder classNameBuilder, final String expectedClassName) {
        final Class<?> generated = codecClassLoader.generateClass(BINDING_INTERFACE, classNameBuilder, CLASS_GENERATOR);
        assertNotNull(generated);
        assertEquals(expectedClassName, generated.getName());

        final Class<?> stored = codecClassLoader.getGeneratedClass(BINDING_INTERFACE, classNameBuilder);
        assertEquals(generated, stored);
    }

    private static Stream<Arguments> generateClassWithinNamespaceArgs() {
        final String common = "urn.opendaylight.yang.union.test.rev220428.Top";
        return Stream.of(
                Arguments.of(ClassNameBuilder.CODEC_IMPL,
                        "org.opendaylight.mdsal.gen.codec.v1." + common + "$$$CodecImpl"),
                Arguments.of(ClassNameBuilder.STREAMER,
                        "org.opendaylight.mdsal.gen.streamer.v1." + common + "$$$Streamer"),
                Arguments.of(ClassNameBuilder.EVENT_AWARE,
                        "org.opendaylight.mdsal.gen.event.v1." + common + "$$$EventInstantAware")
        );
    }

}
