/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataTreeChangeListenerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.Root;
import org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.RootBuilder;
import org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.root.FoorootBuilder;
import org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.root.fooroot.BarrootBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.binding.util.BindingMap;

public class Bug2562DeserializedUnkeyedListTest extends AbstractDataTreeChangeListenerTest {
    @Override
    protected Set<YangModuleInfo> getModuleInfos() {
        return Set.of(BindingRuntimeHelpers.getYangModuleInfo(Root.class));
    }

    @Test
    public void writeListToList2562Root() {
        final var rootPath = DataObjectIdentifier.builder(Root.class).build();

        final var root = new RootBuilder()
            .setFooroot(List.of(new FoorootBuilder()
                .setBarroot(BindingMap.of(new BarrootBuilder().setType(2).setValue(2).build()))
            .build())).build();

        try (var collector = createCollector(LogicalDatastoreType.CONFIGURATION, rootPath)) {
            collector.verifyModifications();

            final var readWriteTransaction = getDataBroker().newReadWriteTransaction();
            readWriteTransaction.put(LogicalDatastoreType.CONFIGURATION, rootPath, root);
            assertCommit(readWriteTransaction.commit());

            collector.verifyModifications(added(rootPath, root));
        }
    }
}
