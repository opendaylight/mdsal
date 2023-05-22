/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
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
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

public class YangLibrarySupportTest extends AbstractYangLibraryTest {
    @Test
    public void testFormatSchema() {
        final var codec = (BindingDataObjectCodecTreeNode<YangLibrary>)
            codecTree.getSubtreeCodec(InstanceIdentifier.create(YangLibrary.class));

        final ContainerNode nonLegacyContent = yangLib.newContentBuilder()
                .defaultContext(runtimeContext.getEffectiveModelContext()).formatYangLibraryContent();
        final YangLibrary yangLibrary = codec.deserialize(nonLegacyContent);

        assertEquals(1, yangLibrary.nonnullModuleSet().size());
        final ModuleSet moduleSet = yangLibrary.nonnullModuleSet().get(new ModuleSetKey("ODL_modules"));
        assertEquals(4, moduleSet.nonnullModule().size());
        assertEquals(moduleSet.getModule(), createControlModules());
    }

    private static Map<ModuleKey, Module> createControlModules() {
        return BindingMap.of(
            createModule("ietf-yang-library", "urn:ietf:params:xml:ns:yang:ietf-yang-library", "2019-01-04"),
            createModule("ietf-inet-types", "urn:ietf:params:xml:ns:yang:ietf-inet-types", "2013-07-15"),
            createModule("ietf-datastores", "urn:ietf:params:xml:ns:yang:ietf-datastores", "2018-02-14"),
            createModule("ietf-yang-types", "urn:ietf:params:xml:ns:yang:ietf-yang-types", "2013-07-15"));
    }

    private static Module createModule(final String name, final String namespace, final String revision) {
        return new ModuleBuilder()
            .setName(new YangIdentifier(name))
                .setNamespace(new Uri(namespace))
                .setRevision(new RevisionIdentifier(revision))
                .setFeature(Set.of())
                .setSubmodule(Map.of())
                .build();
    }
}