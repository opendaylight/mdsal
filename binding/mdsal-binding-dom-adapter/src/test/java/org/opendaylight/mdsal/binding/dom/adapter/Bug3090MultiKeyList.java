/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataTreeChangeListenerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.opendaylight.test.bug._3090.rev160101.Root;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.opendaylight.test.bug._3090.rev160101.RootBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.opendaylight.test.bug._3090.rev160101.root.ListInRoot;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.opendaylight.test.bug._3090.rev160101.root.ListInRootBuilder;
import org.opendaylight.yangtools.binding.YangModuleInfo;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Bug3090MultiKeyList extends AbstractDataTreeChangeListenerTest {
    private static final InstanceIdentifier<Root> ROOT_PATH = InstanceIdentifier.create(Root.class);

    @Override
    protected Set<YangModuleInfo> getModuleInfos() throws Exception {
        return Set.of(BindingRuntimeHelpers.getYangModuleInfo(Root.class));
    }

    @Test
    public void listWithMultiKeyTest() {
        final var listInRoots = new ArrayList<ListInRoot>();
        for (int i = 0; i < 10; i++) {
            listInRoots.add(new ListInRootBuilder()
                .setLeafA("leaf a" + i)
                .setLeafC("leaf c" + i)
                .setLeafB("leaf b" + i)
                .build()
            );
        }

        final var root = new RootBuilder().setListInRoot(BindingMap.of(listInRoots)).build();

        try (var collector = createCollector(LogicalDatastoreType.CONFIGURATION, ROOT_PATH)) {
            collector.verifyModifications();

            final var readWriteTransaction = getDataBroker().newReadWriteTransaction();
            readWriteTransaction.put(LogicalDatastoreType.CONFIGURATION, ROOT_PATH, root);
            assertCommit(readWriteTransaction.commit());

            collector.verifyModifications(match(ModificationType.WRITE, ROOT_PATH, Objects::isNull,
                (DataMatcher<Root>) dataAfter -> checkData(root, dataAfter)));
        }
    }

    private static boolean checkData(final Root expected, final Root actual) {
        if (actual == null) {
            return false;
        }

        var expListInRoot = new HashSet<>(expected.nonnullListInRoot().values());
        var actualListInRoot = actual.nonnullListInRoot().values().stream()
                .map(list -> new ListInRootBuilder(list).build()).collect(Collectors.toSet());
        return expListInRoot.equals(actualListInRoot);
    }
}
