/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import java.util.ServiceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeFactory;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;

abstract class AbstractYangLibraryTest {
    private static final BindingRuntimeGenerator BINDING_RUNTIME_GENERATOR =
        ServiceLoader.load(BindingRuntimeGenerator.class).findFirst().orElseThrow();
    private static final YangParserFactory YANG_PARSER_FACTORY = ServiceLoader.load(YangParserFactory.class).findFirst()
        .orElseThrow();
    private static final BindingCodecTreeFactory CODEC_FACTORY = ServiceLoader.load(BindingCodecTreeFactory.class)
        .findFirst().orElseThrow();

    static final BindingRuntimeContext RUNTIME_CONTEXT = BindingRuntimeHelpers.createRuntimeContext();
    static final BindingCodecTree CODEC_TREE = CODEC_FACTORY.create(RUNTIME_CONTEXT);

    YangLibrarySupport yangLib;

    @BeforeEach
    public void before() throws YangParserException {
        yangLib = new YangLibrarySupport(YANG_PARSER_FACTORY, BINDING_RUNTIME_GENERATOR, CODEC_FACTORY);
    }
}
