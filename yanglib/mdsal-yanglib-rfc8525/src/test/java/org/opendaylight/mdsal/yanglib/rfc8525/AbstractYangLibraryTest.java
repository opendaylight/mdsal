/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import org.junit.Before;
import org.junit.BeforeClass;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.impl.DefaultBindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.impl.DefaultYangParserFactory;

abstract class AbstractYangLibraryTest {
    private static final BindingRuntimeGenerator BINDING_RUNTIME_GENERATOR = new DefaultBindingRuntimeGenerator();
    private static final YangParserFactory YANG_PARSER_FACTORY = new DefaultYangParserFactory();
    private static final BindingCodecTreeFactory CODEC_FACTORY = new DefaultBindingCodecTreeFactory();

    static BindingRuntimeContext runtimeContext;
    static BindingCodecTree codecTree;

    YangLibrarySupport yangLib;

    @BeforeClass
    public static void beforeClass() {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
        codecTree = CODEC_FACTORY.create(runtimeContext);
    }

    @Before
    public void before() throws YangParserException {
        yangLib = new YangLibrarySupport(YANG_PARSER_FACTORY, BINDING_RUNTIME_GENERATOR, CODEC_FACTORY);
    }
}
