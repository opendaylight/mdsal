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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.mdsal.yanglib.util.YanglibContextBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.CommonLeafs.Revision;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.Module.ConformanceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.ModuleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.YangIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;

public class LegacyYangLibraryFormatTest {

    private static final YangParserFactory YANG_PARSER_FACTORY = new YangParserFactoryImpl();

    private BindingRuntimeContext runtimeContext;
    private BindingCodecTree codecTree;
    private YangLibrarySupport yangLib;

    @Before
    public void setUp() throws Exception {
        final EffectiveModelContext modelContext =
                YanglibContextBuilder.buildContext(YANG_PARSER_FACTORY, ModulesState.class);
        runtimeContext = BindingRuntimeContext.create(new SimpleStrategy(), modelContext);
        final BindingNormalizedNodeCodecRegistry codecRegistry = new BindingNormalizedNodeCodecRegistry(runtimeContext);
        codecTree = codecRegistry.create(runtimeContext);
        yangLib = new YangLibrarySupport(YANG_PARSER_FACTORY);
    }

    @Test
    public void testLegacyFormat() {
        final BindingDataObjectCodecTreeNode<ModulesState> legacyCodec =
                codecTree.getSubtreeCodec(InstanceIdentifier.create(ModulesState.class));

        final Optional<ContainerNode> legacyContent = yangLib.newContentBuilder()
            .defaultContext((EffectiveModelContext)runtimeContext.getSchemaContext())
            .includeLegacy()
            .formatYangLibraryLegacyContent();

        assertTrue(legacyContent.isPresent());

        final ModulesState modulesState = legacyCodec.deserialize(legacyContent.orElseThrow());

        assertEquals(4, modulesState.nonnullModule().size());
        // migrate modulesState from codec generated class to proper binding for checking
        final List<Module> valuesToCheck = modulesState.getModule().stream()
                .map(module -> createModule(module.getName().getValue(), module.getNamespace().getValue(),
                        module.getRevision().getRevisionIdentifier().getValue()))
                .collect(Collectors.toList());
        assertTrue(createControlModules().containsAll(valuesToCheck));
    }

    private List<Module> createControlModules() {
        return List.of(
                createModule("ietf-yang-library", "urn:ietf:params:xml:ns:yang:ietf-yang-library", "2019-01-04"),
                createModule("ietf-inet-types", "urn:ietf:params:xml:ns:yang:ietf-inet-types", "2013-07-15"),
                createModule("ietf-datastores", "urn:ietf:params:xml:ns:yang:ietf-datastores", "2018-02-14"),
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