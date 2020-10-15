/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.impl.DefaultBindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibrary;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.ModuleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.ModuleKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.ModuleSet;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.ModuleSetKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.YangIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;

public class YangLibrarySupportTest {

    private static final BindingRuntimeGenerator BINDING_RUNTIME_GENERATOR = new DefaultBindingRuntimeGenerator();

    private static final YangParserFactory YANG_PARSER_FACTORY = new YangParserFactoryImpl();

    private YangLibrarySupport yangLib;
    private BindingRuntimeContext runtimeContext;
    private BindingCodecTree codecTree;

    @Before
    public void setUp() throws Exception {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
        final DefaultBindingCodecTreeFactory codecFactory = new DefaultBindingCodecTreeFactory();
        yangLib = new YangLibrarySupport(YANG_PARSER_FACTORY, BINDING_RUNTIME_GENERATOR,
                codecFactory);
        codecTree = codecFactory.create(runtimeContext);
    }

    @Test
    public void testFormatSchema() {
        final BindingDataObjectCodecTreeNode<YangLibrary> codec =
                codecTree.getSubtreeCodec(InstanceIdentifier.create(YangLibrary.class));

        final ContainerNode nonLegacyContent = yangLib.newContentBuilder()
                .defaultContext(runtimeContext.getEffectiveModelContext()).formatYangLibraryContent();
        final YangLibrary yangLibrary = codec.deserialize(nonLegacyContent);

        assertEquals(1, yangLibrary.nonnullModuleSet().size());
        final ModuleSet moduleSet = yangLibrary.nonnullModuleSet().get(new ModuleSetKey("ODL_modules"));
        assertEquals(4, moduleSet.nonnullModule().size());
        assertEquals(moduleSet.getModule(), createControlModules());
    }

    private Map<ModuleKey, Module> createControlModules() {
        final Map<ModuleKey, Module> modules = new HashMap<>();
        modules.put(new ModuleKey(new YangIdentifier("ietf-yang-library")),
                createModule("ietf-yang-library", "urn:ietf:params:xml:ns:yang:ietf-yang-library", "2019-01-04"));
        modules.put(new ModuleKey(new YangIdentifier("ietf-inet-types")),
                createModule("ietf-inet-types", "urn:ietf:params:xml:ns:yang:ietf-inet-types", "2013-07-15"));
        modules.put(new ModuleKey(new YangIdentifier("ietf-datastores")),
                createModule("ietf-datastores", "urn:ietf:params:xml:ns:yang:ietf-datastores", "2018-02-14"));
        modules.put(new ModuleKey(new YangIdentifier("ietf-yang-types")),
                createModule("ietf-yang-types", "urn:ietf:params:xml:ns:yang:ietf-yang-types", "2013-07-15"));
        return modules;
    }

    private Module createModule(final String name, final String namespace, final String revision) {
        return new ModuleBuilder().setName(new YangIdentifier(name))
                .setNamespace(new Uri(namespace))
                .setRevision(new RevisionIdentifier(revision))
                .setFeature(Collections.emptyList())
                .setSubmodule(Collections.emptyMap())
                .build();
    }
}