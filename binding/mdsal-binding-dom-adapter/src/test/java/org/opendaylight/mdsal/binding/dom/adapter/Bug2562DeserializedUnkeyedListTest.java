/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataTreeChangeListenerTest;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.Root;
import org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.RootBuilder;
import org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.root.FoorootBuilder;
import org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.root.fooroot.BarrootBuilder;
import org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.root.fooroot.BarrootKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class Bug2562DeserializedUnkeyedListTest extends AbstractDataTreeChangeListenerTest {
    private static final InstanceIdentifier<Root> ROOT_PATH = InstanceIdentifier.create(Root.class);

    @Override
    protected Set<YangModuleInfo> getModuleInfos() throws Exception {
        return Set.of(BindingReflections.getModuleInfo(Root.class));
    }

    @Test
    public void writeListToList2562Root() {
        final var barRoot = new BarrootBuilder().setType(2).setValue(2).withKey(new BarrootKey(2)).build();
        final var fooRoot = new FoorootBuilder().setBarroot(Map.of(barRoot.key(), barRoot)).build();
        final var root = new RootBuilder().setFooroot(List.of(fooRoot)).build();

        try (var collector = createCollector(LogicalDatastoreType.CONFIGURATION, ROOT_PATH)) {
            final var readWriteTransaction = getDataBroker().newReadWriteTransaction();
            readWriteTransaction.put(LogicalDatastoreType.CONFIGURATION, ROOT_PATH, root);
            assertCommit(readWriteTransaction.commit());

            collector.assertModifications(added(ROOT_PATH, root));
        }
    }
}
