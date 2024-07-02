/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_BAR_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_FOO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.top;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataTreeChangeListenerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * This testsuite tests explanation for data change scope and data modifications which were described in
 * https://lists.opendaylight.org/pipermail/controller-dev/2014-July/005541.html.
 */
public class ListInsertionDataChangeListenerTest extends AbstractDataTreeChangeListenerTest {
    private static final InstanceIdentifier<Top> TOP = InstanceIdentifier.create(Top.class);
    private static final InstanceIdentifier<TopLevelList> WILDCARDED = TOP.child(TopLevelList.class);
    private static final InstanceIdentifier<TopLevelList> TOP_FOO = TOP.child(TopLevelList.class, TOP_FOO_KEY);
    private static final InstanceIdentifier<TopLevelList> TOP_BAR = TOP.child(TopLevelList.class, TOP_BAR_KEY);

    @Override
    protected Set<YangModuleInfo> getModuleInfos() {
        return Set.of(BindingRuntimeHelpers.getYangModuleInfo(Top.class));
    }

    @Before
    public void setupWithDataBroker() {
        WriteTransaction initialTx = getDataBroker().newWriteOnlyTransaction();
        initialTx.put(CONFIGURATION, TOP, top(topLevelList(TOP_FOO_KEY)));
        assertCommit(initialTx.commit());
    }

    @Test
    public void replaceTopNodeSubtreeListeners() {
        final TopLevelList topBar = topLevelList(TOP_BAR_KEY);
        final Top top = top(topBar);
        final TopLevelList topFoo = topLevelList(TOP_FOO_KEY);

        // Listener for TOP element
        try (var topListener = createCollector(CONFIGURATION, TOP)) {
            // Listener for all list items. This one should see Foo item deleted and Bar item added.
            try (var allListener = createCollector(CONFIGURATION, WILDCARDED)) {
                // Listener for all Foo item. This one should see only Foo item deleted.
                try (var fooListener = createCollector(CONFIGURATION, TOP_FOO)) {
                    // Listener for bar list items.
                    try (var barListener = createCollector(CONFIGURATION, TOP_BAR)) {
                        final var writeTx = getDataBroker().newWriteOnlyTransaction();
                        writeTx.put(CONFIGURATION, TOP, top);
                        assertCommit(writeTx.commit());

                        barListener.verifyModifications(added(TOP_BAR, topBar));
                    }
                    fooListener.verifyModifications(added(TOP_FOO, topFoo), deleted(TOP_FOO, topFoo));
                }
                allListener.verifyModifications(
                    added(TOP_FOO, topFoo),
                    added(TOP_BAR, topBar),
                    deleted(TOP_FOO, topFoo));
            }
            topListener.verifyModifications(
                added(TOP, top(topLevelList(TOP_FOO_KEY))),
                replaced(TOP, top(topFoo), top));
        }
    }

    @Test
    public void mergeTopNodeSubtreeListeners() {
        final var topBar = topLevelList(TOP_BAR_KEY);
        final var topFoo = topLevelList(TOP_FOO_KEY);

        try (var topListener = createCollector(CONFIGURATION, TOP)) {
            try (var allListener = createCollector(CONFIGURATION, WILDCARDED)) {
                try (var fooListener = createCollector(CONFIGURATION, TOP_FOO)) {
                    try (var barListener = createCollector(CONFIGURATION, TOP_BAR)) {
                        final var writeTx = getDataBroker().newWriteOnlyTransaction();
                        writeTx.merge(CONFIGURATION, TOP, top(topLevelList(TOP_BAR_KEY)));
                        assertCommit(writeTx.commit());

                        barListener.verifyModifications(added(TOP_BAR, topBar));
                    }
                    fooListener.verifyModifications(added(TOP_FOO, topFoo));
                }
                allListener.verifyModifications(added(TOP_FOO, topFoo), added(TOP_BAR, topBar));
            }
            topListener.verifyModifications(
                added(TOP, top(topLevelList(TOP_FOO_KEY))), topSubtreeModified(topFoo, topBar));
        }
    }

    @Test
    public void putTopBarNodeSubtreeListeners() {
        final var topBar = topLevelList(TOP_BAR_KEY);
        final var topFoo = topLevelList(TOP_FOO_KEY);

        try (var topListener = createCollector(CONFIGURATION, TOP)) {
            try (var allListener = createCollector(CONFIGURATION, WILDCARDED)) {
                try (var fooListener = createCollector(CONFIGURATION, TOP_FOO)) {
                    try (var barListener = createCollector(CONFIGURATION, TOP_BAR)) {
                        var writeTx = getDataBroker().newWriteOnlyTransaction();
                        writeTx.put(CONFIGURATION, TOP_BAR, topLevelList(TOP_BAR_KEY));
                        assertCommit(writeTx.commit());

                        barListener.verifyModifications(added(TOP_BAR, topBar));
                    }
                    fooListener.verifyModifications(added(TOP_FOO, topFoo));
                }
                allListener.verifyModifications(added(TOP_FOO, topFoo), added(TOP_BAR, topBar));
            }
            topListener.verifyModifications(
                added(TOP, top(topLevelList(TOP_FOO_KEY))), topSubtreeModified(topFoo, topBar));
        }
    }

    @Test
    public void mergeTopBarNodeSubtreeListeners() {
        final var topBar = topLevelList(TOP_BAR_KEY);
        final var topFoo = topLevelList(TOP_FOO_KEY);

        try (var topListener = createCollector(CONFIGURATION, TOP)) {
            try (var allListener = createCollector(CONFIGURATION, WILDCARDED)) {
                try (var fooListener = createCollector(CONFIGURATION, TOP_FOO)) {
                    try (var barListener = createCollector(CONFIGURATION, TOP_BAR)) {
                        final var writeTx = getDataBroker().newWriteOnlyTransaction();
                        writeTx.merge(CONFIGURATION, TOP_BAR, topLevelList(TOP_BAR_KEY));
                        assertCommit(writeTx.commit());

                        barListener.verifyModifications(added(TOP_BAR, topBar));
                    }
                    fooListener.verifyModifications(added(TOP_FOO, topFoo));
                }
                allListener.verifyModifications(added(TOP_FOO, topFoo), added(TOP_BAR, topBar));
            }
            topListener.verifyModifications(
                added(TOP, top(topLevelList(TOP_FOO_KEY))), topSubtreeModified(topFoo, topBar));
        }
    }

    private static Matcher<Top> topSubtreeModified(final TopLevelList topFoo, final TopLevelList topBar) {
        return match(ModificationType.SUBTREE_MODIFIED, TOP,
            (DataMatcher<Top>) dataBefore -> Objects.equals(top(topFoo), dataBefore),
            dataAfter -> {
                var expList = new HashSet<>(top(topBar, topFoo).nonnullTopLevelList().values());
                var actualList = dataAfter.nonnullTopLevelList().values().stream()
                        .map(list -> new TopLevelListBuilder(list).build()).collect(Collectors.toSet());
                return expList.equals(actualList);
            });
    }
}
