/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc7895;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.impl.DefaultBindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.CommonLeafs.Revision;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.Module.ConformanceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.ModuleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.ModuleKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.YangIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;

public class YangModuleLibrarySupportTest {

    private static final BindingRuntimeGenerator BINDING_RUNTIME_GENERATOR = new DefaultBindingRuntimeGenerator();

    private static final YangParserFactory YANG_PARSER_FACTORY = new YangParserFactoryImpl();

    private BindingRuntimeContext runtimeContext;
    private BindingCodecTree codecTree;
    private YangModuleLibrarySupport yangLib;

    @Before
    public void setUp() throws IOException, YangParserException {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
        final DefaultBindingCodecTreeFactory codecFactory = new DefaultBindingCodecTreeFactory();
        codecTree = codecFactory.create(runtimeContext);

        yangLib = new YangModuleLibrarySupport(YANG_PARSER_FACTORY, BINDING_RUNTIME_GENERATOR, codecFactory);
    }

    @Test
    public void testModulesState() {
        final BindingDataObjectCodecTreeNode<ModulesState> legacyCodec =
                codecTree.getSubtreeCodec(InstanceIdentifier.create(ModulesState.class));
        final ModulesState modulesState = legacyCodec.deserialize(
                yangLib.newContentBuilder().defaultContext(
                        runtimeContext.getEffectiveModelContext()).formatYangLibraryContent());

        assertEquals(3, modulesState.nonnullModule().size());
        assertEquals(createControlModules(), modulesState.getModule());
    }

    private static Map<ModuleKey, Module> createControlModules() {
        return Map.of(
            new ModuleKey(new YangIdentifier("ietf-yang-library"), new Revision(new RevisionIdentifier("2016-06-21"))),
            createModule("ietf-yang-library", "urn:ietf:params:xml:ns:yang:ietf-yang-library", "2016-06-21"),
            new ModuleKey(new YangIdentifier("ietf-inet-types"), new Revision(new RevisionIdentifier("2013-07-15"))),
            createModule("ietf-inet-types", "urn:ietf:params:xml:ns:yang:ietf-inet-types", "2013-07-15"),
            new ModuleKey(new YangIdentifier("ietf-yang-types"), new Revision(new RevisionIdentifier("2013-07-15"))),
            createModule("ietf-yang-types", "urn:ietf:params:xml:ns:yang:ietf-yang-types", "2013-07-15"));
    }

    private static Module createModule(final String name, final String namespace, final String revision) {
        return new ModuleBuilder()
                .setName(new YangIdentifier(name))
                .setNamespace(new Uri(namespace))
                .setRevision(new Revision(new RevisionIdentifier(revision)))
                .setConformanceType(ConformanceType.Implement)
                .setFeature(Collections.emptyList())
                .build();
    }
}