/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.impl;

import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.leafBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.mapBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.mapEntryBuilder;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.ThreadSafe;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.yanglib.ModulesStateFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.ModuleList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.Module.ConformanceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.module.Submodule;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
@MetaInfServices
@ThreadSafe
public final class ImmutableModulesStateFactory implements Immutable, ModulesStateFactory {
    private static final NodeIdentifier MODULES_STATE = NodeIdentifier.create(ModulesState.QNAME);
    private static final NodeIdentifier MODULE_LIST = NodeIdentifier.create(ModuleList.QNAME);
    private static final NodeIdentifier MODULE_SET_ID = NodeIdentifier.create(
        QName.create(ModulesState.QNAME, "module-set-id"));
    private static final NodeIdentifier MODULE_NAME = NodeIdentifier.create(Module.QNAME);
    private static final NodeIdentifier MODULE_CONFORMANCE_TYPE = NodeIdentifier.create(
        QName.create(Module.QNAME, "conformance-type"));
    private static final NodeIdentifier MODULE_NAMESPACE = NodeIdentifier.create(
        QName.create(Module.QNAME, "namespace"));
    private static final NodeIdentifier MODULE_REVISION = NodeIdentifier.create(
        QName.create(Module.QNAME, "revision"));

    // FIXME: Conformance type is always set to Implement value, but it should it really be like this?
    private static final LeafNode<?> CONFORMANCE_IMPLEMENT = leafBuilder().withNodeIdentifier(MODULE_CONFORMANCE_TYPE)
            .withValue(ConformanceType.Implement.getName()).build();

    private final AtomicInteger moduleSetId = new AtomicInteger();

    @Override
    public ContainerNode createModulesState(final SchemaContext schemaContext) {
        // FIXME: lookup SchemaNode for modules-state, etc.
        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> builder = containerBuilder()
                .withNodeIdentifier(MODULES_STATE)
                .withChild(leafBuilder()
                    .withNodeIdentifier(MODULE_SET_ID)
                    .withValue(String.valueOf(moduleSetId.getAndIncrement()))
                    .build());

        final Collection<org.opendaylight.yangtools.yang.model.api.Module> modules = schemaContext.getModules();
        if (!modules.isEmpty()) {
            final CollectionNodeBuilder<MapEntryNode, MapNode> list = mapBuilder().withNodeIdentifier(MODULE_LIST);
            modules.stream().map(ImmutableModulesStateFactory::createModule).forEach(list::withChild);
            builder.withChild(list.build());
        }

        return builder.build();
    }

    private static MapEntryNode createModule(final org.opendaylight.yangtools.yang.model.api.Module module) {
        final DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> builder =
                entryBuilder(module, ModuleList.QNAME)
                .withChild(leafBuilder()
                    .withNodeIdentifier(MODULE_NAMESPACE)
                    .withValue(module.getNamespace().toString())
                    .build())
                .withChild(CONFORMANCE_IMPLEMENT);

        final Collection<org.opendaylight.yangtools.yang.model.api.Module> submodules = module.getSubmodules();
        if (!submodules.isEmpty()) {
            final CollectionNodeBuilder<MapEntryNode, MapNode> list = mapBuilder().withNodeIdentifier(MODULE_LIST);
            submodules.stream().map(ImmutableModulesStateFactory::createSubModule).forEach(list::withChild);
            builder.withChild(list.build());

        }

        // FIXME: Add also deviations and features lists to module entries
        return builder.build();
    }

    private static MapEntryNode createSubModule(final org.opendaylight.yangtools.yang.model.api.Module module) {
        return entryBuilder(module, Submodule.QNAME).build();
    }

    private static DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> entryBuilder(
            final org.opendaylight.yangtools.yang.model.api.Module module, final QName listName) {
        final LeafNode<?> name = leafBuilder().withNodeIdentifier(MODULE_NAME).withValue(module.getName()).build();
        final LeafNode<?> revision = leafBuilder().withNodeIdentifier(MODULE_REVISION)
                .withValue(module.getRevision().map(Revision::toString).orElse("")).build();

        return mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(listName,
                    ImmutableMap.of(MODULE_NAME.getNodeType(), name, MODULE_REVISION.getNodeType(), revision)))
                .withChild(name)
                .withChild(revision);
    }
}
