/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.CommonLeafs.Revision;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.Module.ConformanceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.ModuleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.YangIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;

class LegacyYangLibraryFormatTest extends AbstractYangLibraryTest {
    @Test
    void testLegacyFormat() {
        final var legacyCodec = CODEC_TREE.getDataObjectCodec(InstanceIdentifier.create(ModulesState.class));

        final var legacyContent = yangLib.newContentBuilder()
            .defaultContext(RUNTIME_CONTEXT.modelContext())
            .includeLegacy()
            .formatYangLibraryLegacyContent();

        assertTrue(legacyContent.isPresent());

        final var modulesState = legacyCodec.deserialize(legacyContent.orElseThrow());
        assertEquals(BindingMap.of(
            createModule("ietf-yang-library", "urn:ietf:params:xml:ns:yang:ietf-yang-library", "2019-01-04"),
            createModule("ietf-inet-types", "urn:ietf:params:xml:ns:yang:ietf-inet-types", "2013-07-15"),
            createModule("ietf-datastores", "urn:ietf:params:xml:ns:yang:ietf-datastores", "2018-02-14"),
            createModule("ietf-yang-types", "urn:ietf:params:xml:ns:yang:ietf-yang-types", "2013-07-15")),
            modulesState.getModule());
    }

    private static Module createModule(final String name, final String namespace, final String revision) {
        return new ModuleBuilder()
            .setName(new YangIdentifier(name))
            .setNamespace(new Uri(namespace))
            .setRevision(new Revision(new RevisionIdentifier(revision)))
            .setConformanceType(ConformanceType.Implement)
            .setFeature(Set.of())
            .build();
    }
}