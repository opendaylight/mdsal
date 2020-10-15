/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.CommonLeafs.Revision;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.Module.ConformanceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.ModuleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.ModuleKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.YangIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;

public class LegacyYangLibraryFormatTest {

    private static final BindingRuntimeGenerator BINDING_RUNTIME_GENERATOR = new DefaultBindingRuntimeGenerator();

    private static final YangParserFactory YANG_PARSER_FACTORY = new YangParserFactoryImpl();

    private BindingRuntimeContext runtimeContext;
    private BindingCodecTree codecTree;
    private YangLibrarySupport yangLib;

    @Before
    public void setUp() throws YangParserException {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
        final DefaultBindingCodecTreeFactory codecFactory = new DefaultBindingCodecTreeFactory();
        yangLib = new YangLibrarySupport(YANG_PARSER_FACTORY, BINDING_RUNTIME_GENERATOR,
                codecFactory);
        codecTree = codecFactory.create(runtimeContext);
    }

    @Test
    public void testLegacyFormat() {
        final BindingDataObjectCodecTreeNode<ModulesState> legacyCodec =
                codecTree.getSubtreeCodec(InstanceIdentifier.create(ModulesState.class));

        final Optional<ContainerNode> legacyContent = yangLib.newContentBuilder()
            .defaultContext(runtimeContext.getEffectiveModelContext())
            .includeLegacy()
            .formatYangLibraryLegacyContent();

        assertTrue(legacyContent.isPresent());

        final ModulesState modulesState = legacyCodec.deserialize(legacyContent.orElseThrow());

        assertEquals(4, modulesState.nonnullModule().size());
        assertEquals(createControlModules(), modulesState.getModule());
    }

    private Map<ModuleKey, Module> createControlModules() {
        final Map<ModuleKey, Module> modules = new HashMap<>();
        modules.put(new ModuleKey(new YangIdentifier("ietf-yang-library"),
                        new Revision(new RevisionIdentifier("2019-01-04"))),
                createModule("ietf-yang-library", "urn:ietf:params:xml:ns:yang:ietf-yang-library", "2019-01-04"));
        modules.put(new ModuleKey(new YangIdentifier("ietf-inet-types"),
                        new Revision(new RevisionIdentifier("2013-07-15"))),
                createModule("ietf-inet-types", "urn:ietf:params:xml:ns:yang:ietf-inet-types", "2013-07-15"));
        modules.put(new ModuleKey(new YangIdentifier("ietf-datastores"),
                        new Revision(new RevisionIdentifier("2018-02-14"))),
                createModule("ietf-datastores", "urn:ietf:params:xml:ns:yang:ietf-datastores", "2018-02-14"));
        modules.put(new ModuleKey(new YangIdentifier("ietf-yang-types"),
                        new Revision(new RevisionIdentifier("2013-07-15"))),
                createModule("ietf-yang-types", "urn:ietf:params:xml:ns:yang:ietf-yang-types", "2013-07-15"));
        return modules;
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