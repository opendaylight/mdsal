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

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataTreeChangeListenerTest;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

/**
 * This testsuite tests explanation for data change scope and data modifications which were described in
 * https://lists.opendaylight.org/pipermail/controller-dev/2014-July/005541.html.
 */
public class ListInsertionDataChangeListenerTest extends AbstractDataTreeChangeListenerTest {
    private static final InstanceIdentifier<Top> LEGACY_TOP = InstanceIdentifier.create(Top.class);
    private static final org.opendaylight.mdsal.binding.api.InstanceIdentifier<Top> TOP =
            org.opendaylight.mdsal.binding.api.InstanceIdentifier.create(Top.class);
    private static final InstanceIdentifier<TopLevelList> WILDCARDED = LEGACY_TOP.child(TopLevelList.class);
    private static final InstanceIdentifier<TopLevelList> TOP_FOO = LEGACY_TOP.child(TopLevelList.class, TOP_FOO_KEY);
    private static final InstanceIdentifier<TopLevelList> TOP_BAR = LEGACY_TOP.child(TopLevelList.class, TOP_BAR_KEY);

    @Override
    protected Set<YangModuleInfo> getModuleInfos() throws Exception {
        return ImmutableSet.of(BindingReflections.getModuleInfo(Top.class));
    }

    @Before
    public void setupWithDataBroker() {
        WriteTransaction initialTx = getDataBroker().newWriteOnlyTransaction();
        initialTx.put(CONFIGURATION, LEGACY_TOP, top(topLevelList(TOP_FOO_KEY)));
        assertCommit(initialTx.commit());
    }

    @Test
    public void replaceTopNodeSubtreeListeners() {
        final TopLevelList topBar = topLevelList(TOP_BAR_KEY);
        final Top top = top(topBar);
        final TopLevelList topFoo = topLevelList(TOP_FOO_KEY);

        // Listener for TOP element
        final TestListener<Top> topListener = createListener(CONFIGURATION, LEGACY_TOP,
                added(LEGACY_TOP, top(topLevelList(TOP_FOO_KEY))), replaced(LEGACY_TOP, top(topFoo), top));

        // Listener for all list items. This one should see Foo item deleted and Bar item added.
        final TestListener<TopLevelList> allListener = createListener(CONFIGURATION, WILDCARDED,
                added(TOP_FOO, topFoo), added(TOP_BAR, topBar), deleted(TOP_FOO, topFoo));

        // Listener for all Foo item. This one should see only Foo item deleted.
        final TestListener<TopLevelList> fooListener = createListener(CONFIGURATION, TOP_FOO,
                added(TOP_FOO, topFoo), deleted(TOP_FOO, topFoo));

        // Listener for bar list items.
        final TestListener<TopLevelList> barListener = createListener(CONFIGURATION, TOP_BAR,
                added(TOP_BAR, topBar));

        ReadWriteTransaction writeTx = getDataBroker().newReadWriteTransaction();
        writeTx.put(CONFIGURATION, LEGACY_TOP, top);
        assertCommit(writeTx.commit());

        topListener.verify();
        allListener.verify();
        fooListener.verify();
        barListener.verify();
    }

    @Test
    public void legacyMergeTopNodeSubtreeListeners() {
        final TopLevelList topBar = topLevelList(TOP_BAR_KEY);
        final TopLevelList topFoo = topLevelList(TOP_FOO_KEY);

        final TestListener<Top> topListener = createListener(CONFIGURATION, LEGACY_TOP,
                added(LEGACY_TOP, top(topLevelList(TOP_FOO_KEY))), topSubtreeModified(topFoo, topBar));
        final TestListener<TopLevelList> allListener = createListener(CONFIGURATION, WILDCARDED,
                added(TOP_FOO, topFoo), added(TOP_BAR, topBar));
        final TestListener<TopLevelList> fooListener = createListener(CONFIGURATION, TOP_FOO,
                added(TOP_FOO, topFoo));
        final TestListener<TopLevelList> barListener = createListener(CONFIGURATION, TOP_BAR,
                added(TOP_BAR, topBar));

        ReadWriteTransaction writeTx = getDataBroker().newReadWriteTransaction();
        writeTx.merge(CONFIGURATION, TOP, top(topLevelList(TOP_BAR_KEY)));
        assertCommit(writeTx.commit());

        topListener.verify();
        allListener.verify();
        fooListener.verify();
        barListener.verify();
    }

    public void mergeTopNodeSubtreeListeners() {
        final TopLevelList topBar = topLevelList(TOP_BAR_KEY);
        final TopLevelList topFoo = topLevelList(TOP_FOO_KEY);

        final TestListener<Top> topListener = createListener(CONFIGURATION, LEGACY_TOP,
                added(LEGACY_TOP, top(topLevelList(TOP_FOO_KEY))), topSubtreeModified(topFoo, topBar));
        final TestListener<TopLevelList> allListener = createListener(CONFIGURATION, WILDCARDED,
                added(TOP_FOO, topFoo), added(TOP_BAR, topBar));
        final TestListener<TopLevelList> fooListener = createListener(CONFIGURATION, TOP_FOO,
                added(TOP_FOO, topFoo));
        final TestListener<TopLevelList> barListener = createListener(CONFIGURATION, TOP_BAR,
                added(TOP_BAR, topBar));

        ReadWriteTransaction writeTx = getDataBroker().newReadWriteTransaction();
        writeTx.merge(CONFIGURATION, LEGACY_TOP, top(topLevelList(TOP_BAR_KEY)));
        assertCommit(writeTx.commit());

        topListener.verify();
        allListener.verify();
        fooListener.verify();
        barListener.verify();
    }

    @Test
    public void putTopBarNodeSubtreeListeners() {
        final TopLevelList topBar = topLevelList(TOP_BAR_KEY);
        final TopLevelList topFoo = topLevelList(TOP_FOO_KEY);

        final TestListener<Top> topListener = createListener(CONFIGURATION, LEGACY_TOP,
                added(LEGACY_TOP, top(topLevelList(TOP_FOO_KEY))), topSubtreeModified(topFoo, topBar));
        final TestListener<TopLevelList> allListener = createListener(CONFIGURATION, WILDCARDED,
                added(TOP_FOO, topFoo), added(TOP_BAR, topBar));
        final TestListener<TopLevelList> fooListener = createListener(CONFIGURATION, TOP_FOO,
                added(TOP_FOO, topFoo));
        final TestListener<TopLevelList> barListener = createListener(CONFIGURATION, TOP_BAR,
                added(TOP_BAR, topBar));

        ReadWriteTransaction writeTx = getDataBroker().newReadWriteTransaction();
        writeTx.put(CONFIGURATION, TOP_BAR, topLevelList(TOP_BAR_KEY));
        assertCommit(writeTx.commit());

        topListener.verify();
        allListener.verify();
        fooListener.verify();
        barListener.verify();
    }

    @Test
    public void mergeTopBarNodeSubtreeListeners() {
        final TopLevelList topBar = topLevelList(TOP_BAR_KEY);
        final TopLevelList topFoo = topLevelList(TOP_FOO_KEY);

        final TestListener<Top> topListener = createListener(CONFIGURATION, LEGACY_TOP,
                added(LEGACY_TOP, top(topLevelList(TOP_FOO_KEY))), topSubtreeModified(topFoo, topBar));
        final TestListener<TopLevelList> allListener = createListener(CONFIGURATION, WILDCARDED,
                added(TOP_FOO, topFoo), added(TOP_BAR, topBar));
        final TestListener<TopLevelList> fooListener = createListener(CONFIGURATION, TOP_FOO,
                added(TOP_FOO, topFoo));
        final TestListener<TopLevelList> barListener = createListener(CONFIGURATION, TOP_BAR,
                added(TOP_BAR, topBar));

        ReadWriteTransaction writeTx = getDataBroker().newReadWriteTransaction();
        writeTx.merge(CONFIGURATION, TOP_BAR, topLevelList(TOP_BAR_KEY));
        assertCommit(writeTx.commit());

        topListener.verify();
        allListener.verify();
        fooListener.verify();
        barListener.verify();
    }

    private static Function<DataTreeModification<Top>, Boolean> topSubtreeModified(final TopLevelList topFoo,
            final TopLevelList topBar) {
        return match(ModificationType.SUBTREE_MODIFIED, LEGACY_TOP,
            (Function<Top, Boolean>) dataBefore -> Objects.equals(top(topFoo), dataBefore),
            dataAfter -> {
                Set<TopLevelList> expList = new HashSet<>(top(topBar, topFoo).getTopLevelList().values());
                Set<TopLevelList> actualList = dataAfter.getTopLevelList().values().stream()
                        .map(list -> new TopLevelListBuilder(list).build()).collect(Collectors.toSet());
                return expList.equals(actualList);
            });
    }
}
