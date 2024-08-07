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

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collection;
import java.util.List;
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
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DataTreeChangeListenerTest extends AbstractDataBrokerTest {

    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final DataObjectStep<?> TOP_ARGUMENT = TOP_PATH.getPathArguments().iterator().next();
    private static final InstanceIdentifier<TopLevelList> FOO_PATH = path(TOP_FOO_KEY);
    private static final DataObjectStep<?> FOO_ARGUMENT = Iterables.getLast(FOO_PATH.getPathArguments());
    private static final TopLevelList FOO_DATA = topLevelList(TOP_FOO_KEY, complexUsesAugment(USES_ONE_KEY));
    private static final InstanceIdentifier<TopLevelList> BAR_PATH = path(TOP_BAR_KEY);
    private static final DataObjectStep<?> BAR_ARGUMENT = Iterables.getLast(BAR_PATH.getPathArguments());
    private static final TopLevelList BAR_DATA = topLevelList(TOP_BAR_KEY);
    private static final DataTreeIdentifier<Top> TOP_IDENTIFIER
            = DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, TOP_PATH);

    private static final Top TOP_INITIAL_DATA = top(FOO_DATA);

    private BindingDOMDataBrokerAdapter dataBrokerImpl;

    private static final class EventCapturingListener<T extends DataObject> implements DataTreeChangeListener<T> {
        private SettableFuture<List<DataTreeModification<T>>> futureChanges = SettableFuture.create();

        @Override
        public void onDataTreeChanged(final List<DataTreeModification<T>> changes) {
            futureChanges.set(changes);
        }

        Collection<DataTreeModification<T>> nextEvent() throws Exception {
            final var result = futureChanges.get(200,TimeUnit.MILLISECONDS);
            futureChanges = SettableFuture.create();
            return result;
        }
    }

    @Override
    protected Set<YangModuleInfo> getModuleInfos() {
        return Set.of(
            BindingRuntimeHelpers.getYangModuleInfo(TwoLevelList.class),
            BindingRuntimeHelpers.getYangModuleInfo(TreeComplexUsesAugment.class));
    }

    @Override
    protected void setupWithDataBroker(final DataBroker dataBroker) {
        dataBrokerImpl = (BindingDOMDataBrokerAdapter) dataBroker;
    }

    @Test
    public void testTopLevelListener() throws Exception {
        final EventCapturingListener<Top> listener = new EventCapturingListener<>();
        dataBrokerImpl.registerDataTreeChangeListener(TOP_IDENTIFIER, listener);

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

        dataBrokerImpl.registerDataTreeChangeListener(TOP_IDENTIFIER, listener).close();
    }

    @Test
    public void testWildcardedListListener() throws Exception {
        final EventCapturingListener<TopLevelList> listener = new EventCapturingListener<>();
        final DataTreeIdentifier<TopLevelList> wildcard = DataTreeIdentifier.of(
                LogicalDatastoreType.OPERATIONAL, TOP_PATH.child(TopLevelList.class));
        dataBrokerImpl.registerDataTreeChangeListener(wildcard, listener);

        putTx(TOP_PATH, TOP_INITIAL_DATA).commit().get();

        final DataTreeModification<TopLevelList> fooWriteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(FOO_PATH, fooWriteEvent.getRootPath().path());
        verifyModification(fooWriteEvent.getRootNode(), FOO_ARGUMENT, ModificationType.WRITE);

        putTx(BAR_PATH, BAR_DATA).commit().get();
        final DataTreeModification<TopLevelList> barWriteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(BAR_PATH, barWriteEvent.getRootPath().path());
        verifyModification(barWriteEvent.getRootNode(), BAR_ARGUMENT, ModificationType.WRITE);

        deleteTx(BAR_PATH).commit().get();
        final DataTreeModification<TopLevelList> barDeleteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(BAR_PATH, barDeleteEvent.getRootPath().path());
        verifyModification(barDeleteEvent.getRootNode(), BAR_ARGUMENT, ModificationType.DELETE);
    }

    @Test
    public void testWildcardedListListenerWithPreexistingData() throws Exception {
        putTx(TOP_PATH, TOP_INITIAL_DATA).commit().get();

        final EventCapturingListener<TopLevelList> listener = new EventCapturingListener<>();
        final DataTreeIdentifier<TopLevelList> wildcard = DataTreeIdentifier.of(
                LogicalDatastoreType.OPERATIONAL, TOP_PATH.child(TopLevelList.class));
        dataBrokerImpl.registerDataTreeChangeListener(wildcard, listener);

        final DataTreeModification<TopLevelList> fooWriteEvent = Iterables.getOnlyElement(listener.nextEvent());
        assertEquals(FOO_PATH, fooWriteEvent.getRootPath().path());
        verifyModification(fooWriteEvent.getRootNode(), FOO_ARGUMENT, ModificationType.WRITE);
    }

    private void createAndVerifyTop(final EventCapturingListener<Top> listener) throws Exception {
        putTx(TOP_PATH,TOP_INITIAL_DATA).commit().get();
        final Collection<DataTreeModification<Top>> events = listener.nextEvent();

        assertFalse("Non empty collection should be received.",events.isEmpty());
        final DataTreeModification<Top> initialWrite = Iterables.getOnlyElement(events);
        final DataObjectModification<? extends DataObject> initialNode = initialWrite.getRootNode();
        verifyModification(initialNode,TOP_PATH.getPathArguments().iterator().next(),ModificationType.WRITE);
        assertEquals(TOP_INITIAL_DATA, initialNode.dataAfter());
    }

    private static void verifyModification(final DataObjectModification<? extends DataObject> barWrite,
            final DataObjectStep<?> pathArg, final ModificationType eventType) {
        assertEquals(pathArg.type(), barWrite.dataType());
        assertEquals(eventType,barWrite.modificationType());
        assertEquals(pathArg, barWrite.step());
    }

    private <T extends DataObject> WriteTransaction putTx(final InstanceIdentifier<T> path, final T data) {
        final WriteTransaction tx = dataBrokerImpl.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, path, data);
        return tx;
    }

    private WriteTransaction deleteTx(final InstanceIdentifier<?> path) {
        final WriteTransaction tx = dataBrokerImpl.newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
        return tx;
    }
}
