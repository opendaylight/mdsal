/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibrary;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.ModuleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.ModuleSetKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.YangIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;

class YangLibrarySupportTest extends AbstractYangLibraryTest {
    @Test
    void testFormatSchema() {
        final var codec = CODEC_TREE.getDataObjectCodec(InstanceIdentifier.create(YangLibrary.class));

        final var nonLegacyContent = yangLib.newContentBuilder()
                .defaultContext(RUNTIME_CONTEXT.modelContext()).formatYangLibraryContent();
        final var yangLibrary = codec.deserialize(nonLegacyContent);

        final var modulesSets = yangLibrary.nonnullModuleSet();
        assertNotNull(modulesSets);
        assertEquals(1, modulesSets.size());
        final var moduleSet = modulesSets.get(new ModuleSetKey("ODL_modules"));
        assertNotNull(moduleSet);
        assertEquals(BindingMap.of(
            createModule("ietf-yang-library", "urn:ietf:params:xml:ns:yang:ietf-yang-library", "2019-01-04"),
            createModule("ietf-inet-types", "urn:ietf:params:xml:ns:yang:ietf-inet-types", "2013-07-15"),
            createModule("ietf-datastores", "urn:ietf:params:xml:ns:yang:ietf-datastores", "2018-02-14"),
            createModule("ietf-yang-types", "urn:ietf:params:xml:ns:yang:ietf-yang-types", "2013-07-15")),
            moduleSet.getModule());
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