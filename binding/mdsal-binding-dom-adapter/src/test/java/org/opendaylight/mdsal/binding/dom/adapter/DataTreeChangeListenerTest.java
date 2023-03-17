/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_BAR_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_FOO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.USES_ONE_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.complexUsesAugment;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.path;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.top;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class DataTreeChangeListenerTest extends AbstractDataBrokerTest {

    private static final InstanceIdentifier<Top> LEGACY_TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final org.opendaylight.mdsal.binding.api.InstanceIdentifier<Top> TOP_PATH =
            org.opendaylight.mdsal.binding.api.InstanceIdentifier.create(Top.class);
    private static final PathArgument TOP_ARGUMENT = LEGACY_TOP_PATH.getPathArguments().iterator().next();
    private static final InstanceIdentifier<TopLevelList> FOO_PATH = path(TOP_FOO_KEY);
    private static final PathArgument FOO_ARGUMENT = Iterables.getLast(FOO_PATH.getPathArguments());
    private static final TopLevelList FOO_DATA = topLevelList(TOP_FOO_KEY, complexUsesAugment(USES_ONE_KEY));
    private static final InstanceIdentifier<TopLevelList> LEGACY_BAR_PATH = path(TOP_BAR_KEY);
    private static final org.opendaylight.mdsal.binding.api.InstanceIdentifier<TopLevelList> BAR_PATH =
            org.opendaylight.mdsal.binding.api.InstanceIdentifier.ofLegacy(LEGACY_BAR_PATH);
    private static final PathArgument BAR_ARGUMENT = Iterables.getLast(LEGACY_BAR_PATH.getPathArguments());
    private static final TopLevelList BAR_DATA = topLevelList(TOP_BAR_KEY);
    private static final DataTreeIdentifier<Top> LEGACY_TOP_IDENTIFIER
            = DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, LEGACY_TOP_PATH);

    private static final Top TOP_INITIAL_DATA = top(FOO_DATA);

    private BindingDOMDataBrokerAdapter dataBrokerImpl;

    private static final class EventCapturingListener<T extends DataObject> implements DataTreeChangeListener<T> {

        private SettableFuture<Collection<DataTreeModification<T>>> futureChanges = SettableFuture.create();

        @Override
        public void onDataTreeChanged(final Collection<DataTreeModification<T>> changes) {
            this.futureChanges.set(changes);

        }

        Collection<DataTreeModification<T>> nextEvent() throws Exception {
            final Collection<DataTreeModification<T>> result = futureChanges.get(200,TimeUnit.MILLISECONDS);
            futureChanges = SettableFuture.create();
            return result;
        }
    }

    @Override
    protected Set<YangModuleInfo> getModuleInfos() throws Exception {
        return ImmutableSet.of(
                BindingReflections.getModuleInfo(TwoLevelList.class),
                BindingReflections.getModuleInfo(TreeComplexUsesAugment.class)
                );
    }

    @Override
    protected void setupWithDataBroker(final DataBroker dataBroker) {
        dataBrokerImpl = (BindingDOMDataBrokerAdapter) dataBroker;
    }

    @Test
    public void testTopLevelListenerLegacy() throws Exception {
        final EventCapturingListener<Top> listener = new EventCapturingListener<>();
        dataBrokerImpl.registerDataTreeChangeListener(LEGACY_TOP_IDENTIFIER, listener);

        createAndVerifyTop(listener);

        putTx(LEGACY_BAR_PATH, BAR_DATA).commit().get();
        final DataObjectModification<Top> afterBarPutEvent
                = Iterables.getOnlyElement(listener.nextEvent()).getRootNode();
        verifyModification(afterBarPutEvent, TOP_ARGUMENT, ModificationType.SUBTREE_MODIFIED);
        final DataObjectModification<TopLevelList> barPutMod = afterBarPutEvent.getModifiedChildListItem(
                TopLevelList.class, TOP_BAR_KEY);
        assertNotNull(barPutMod);
        verifyModification(barPutMod, BAR_ARGUMENT, ModificationType.WRITE);

        deleteTx(LEGACY_BAR_PATH).commit().get();
        final DataObjectModification<Top> afterBarDeleteEvent
                = Iterables.getOnlyElement(listener.nextEvent()).getRootNode();
        verifyModification(afterBarDeleteEvent, TOP_ARGUMENT, ModificationType.SUBTREE_MODIFIED);
        final DataObjectModification<TopLevelList> barDeleteMod = afterBarDeleteEvent.getModifiedChildListItem(
                TopLevelList.class, TOP_BAR_KEY);
        verifyModification(barDeleteMod, BAR_ARGUMENT, ModificationType.DELETE);

        dataBrokerImpl.registerDataTreeChangeListener(LEGACY_TOP_IDENTIFIER, listener).close();
    }

    @Test
    public void testTopLevelListener() throws Exception {
        final EventCapturingListener<Top> listener = new EventCapturingListener<>();
        dataBrokerImpl.registerDataTreeChangeListener(LogicalDatastoreType.OPERATIONAL, TOP_PATH, listener);

        createAndVerifyTop(listener);

        putTx(BAR_PATH, BAR_DATA).commit().get();
        final DataObjectModification<Top> afterBarPutEvent
                = Iterables.getOnlyElement(listener.nextEvent()).getRootNode();
        verifyModification(afterBarPutEvent, TOP_ARGUMENT, ModificationType.SUBTREE_MODIFIED);
        final DataObjectModification<TopLevelList> barPutMod = afterBarPutEvent.getModifiedChildListItem(
                TopLevelList.class, TOP_BAR_KEY);
        assertNotNull(barPutMod);
        verifyModification(barPutMod, BAR_ARGUMENT, ModificationType.WRITE);

        deleteTx(BAR_PATH).commit().get();
        final DataObjectModification<Top> afterBarDeleteEvent
                = Iterables.getOnlyElement(listener.nextEvent()).getRootNode();
        verifyModification(afterBarDeleteEvent, TOP_ARGUMENT, ModificationType.SUBTREE_MODIFIED);
        final DataObjectModification<TopLevelList> barDeleteMod = afterBarDeleteEvent.getModifiedChildListItem(
                TopLevelList.class, TOP_BAR_KEY);
        verifyModification(barDeleteMod, BAR_ARGUMENT, ModificationType.DELETE);

        dataBrokerImpl.registerDataTreeChangeListener(LogicalDatastoreType.OPERATIONAL, TOP_PATH, listener).close();
    }

    @Test
    public void testWildcardedListListenerLegacy() throws Exception {
        final EventCapturingListener<TopLevelList> listener = new EventCapturingListener<>();
        final DataTreeIdentifier<TopLevelList> wildcard = DataTreeIdentifier.create(
                LogicalDatastoreType.OPERATIONAL, LEGACY_TOP_PATH.child(TopLevelList.class));
        dataBrokerImpl.registerDataTreeChangeListener(wildcard, listener);

        putTx(LEGACY_TOP_PATH, TOP_INITIAL_DATA).commit().get();

        final DataTreeModification<TopLevelList> fooWriteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(FOO_PATH, fooWriteEvent.getRootPath().getRootIdentifier());
        verifyModification(fooWriteEvent.getRootNode(), FOO_ARGUMENT, ModificationType.WRITE);

        putTx(LEGACY_BAR_PATH, BAR_DATA).commit().get();
        final DataTreeModification<TopLevelList> barWriteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(LEGACY_BAR_PATH, barWriteEvent.getRootPath().getRootIdentifier());
        verifyModification(barWriteEvent.getRootNode(), BAR_ARGUMENT, ModificationType.WRITE);

        deleteTx(LEGACY_BAR_PATH).commit().get();
        final DataTreeModification<TopLevelList> barDeleteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(LEGACY_BAR_PATH, barDeleteEvent.getRootPath().getRootIdentifier());
        verifyModification(barDeleteEvent.getRootNode(), BAR_ARGUMENT, ModificationType.DELETE);
    }

    @Test
    public void testWildcardedListListener() throws Exception {
        final EventCapturingListener<TopLevelList> listener = new EventCapturingListener<>();
        dataBrokerImpl.registerDataTreeChangeListener(LogicalDatastoreType.OPERATIONAL,
                TOP_PATH.wildcardChild(TopLevelList.class), listener);

        putTx(TOP_PATH, TOP_INITIAL_DATA).commit().get();

        final DataTreeModification<TopLevelList> fooWriteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(FOO_PATH, fooWriteEvent.getRootPath().getRootIdentifier());
        verifyModification(fooWriteEvent.getRootNode(), FOO_ARGUMENT, ModificationType.WRITE);

        putTx(BAR_PATH, BAR_DATA).commit().get();
        final DataTreeModification<TopLevelList> barWriteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(BAR_PATH, org.opendaylight.mdsal.binding.api.InstanceIdentifier.ofLegacy(
                barWriteEvent.getRootPath().getRootIdentifier()));
        verifyModification(barWriteEvent.getRootNode(), BAR_ARGUMENT, ModificationType.WRITE);

        deleteTx(BAR_PATH).commit().get();
        final DataTreeModification<TopLevelList> barDeleteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(BAR_PATH, org.opendaylight.mdsal.binding.api.InstanceIdentifier.ofLegacy(
                barDeleteEvent.getRootPath().getRootIdentifier()));
        verifyModification(barDeleteEvent.getRootNode(), BAR_ARGUMENT, ModificationType.DELETE);
    }

    @Test
    public void testWildcardedListListenerWithPreexistingDataLegacy() throws Exception {
        putTx(LEGACY_TOP_PATH, TOP_INITIAL_DATA).commit().get();

        final EventCapturingListener<TopLevelList> listener = new EventCapturingListener<>();
        final DataTreeIdentifier<TopLevelList> wildcard = DataTreeIdentifier.create(
                LogicalDatastoreType.OPERATIONAL, LEGACY_TOP_PATH.child(TopLevelList.class));
        dataBrokerImpl.registerDataTreeChangeListener(wildcard, listener);

        final DataTreeModification<TopLevelList> fooWriteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(FOO_PATH, fooWriteEvent.getRootPath().getRootIdentifier());
        verifyModification(fooWriteEvent.getRootNode(), FOO_ARGUMENT, ModificationType.WRITE);
    }

    @Test
    public void testWildcardedListListenerWithPreexistingData() throws Exception {
        putTx(TOP_PATH, TOP_INITIAL_DATA).commit().get();

        final EventCapturingListener<TopLevelList> listener = new EventCapturingListener<>();
        dataBrokerImpl.registerDataTreeChangeListener(LogicalDatastoreType.OPERATIONAL,
                TOP_PATH.wildcardChild(TopLevelList.class), listener);

        final DataTreeModification<TopLevelList> fooWriteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(FOO_PATH, fooWriteEvent.getRootPath().getRootIdentifier());
        verifyModification(fooWriteEvent.getRootNode(), FOO_ARGUMENT, ModificationType.WRITE);
    }

    private void createAndVerifyTop(final EventCapturingListener<Top> listener) throws Exception {
        putTx(LEGACY_TOP_PATH,TOP_INITIAL_DATA).commit().get();
        final Collection<DataTreeModification<Top>> events = listener.nextEvent();

        assertFalse("Non empty collection should be received.",events.isEmpty());
        final DataTreeModification<Top> initialWrite = Iterables.getOnlyElement(events);
        final DataObjectModification<? extends DataObject> initialNode = initialWrite.getRootNode();
        verifyModification(initialNode, LEGACY_TOP_PATH.getPathArguments().iterator().next(),ModificationType.WRITE);
        assertEquals(TOP_INITIAL_DATA, initialNode.getDataAfter());
    }

    private static void verifyModification(final DataObjectModification<? extends DataObject> barWrite,
            final PathArgument pathArg, final ModificationType eventType) {
        assertEquals(pathArg.getType(), barWrite.getDataType());
        assertEquals(eventType,barWrite.getModificationType());
        assertEquals(pathArg, barWrite.getIdentifier());
    }

    private <T extends DataObject> WriteTransaction putTx(final InstanceIdentifier<T> path, final T data) {
        final WriteTransaction tx = dataBrokerImpl.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, path, data);
        return tx;
    }

    private <T extends DataObject> WriteTransaction putTx(final
        org.opendaylight.mdsal.binding.api.InstanceIdentifier<T> path, final T data) {
        final WriteTransaction tx = dataBrokerImpl.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, path, data);
        return tx;
    }

    private WriteTransaction deleteTx(final InstanceIdentifier<?> path) {
        final WriteTransaction tx = dataBrokerImpl.newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
        return tx;
    }

    private WriteTransaction deleteTx(final org.opendaylight.mdsal.binding.api.InstanceIdentifier<?> path) {
        final WriteTransaction tx = dataBrokerImpl.newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
        return tx;
    }
}
