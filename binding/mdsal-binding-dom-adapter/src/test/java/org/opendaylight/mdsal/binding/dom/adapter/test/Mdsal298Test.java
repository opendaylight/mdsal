/*
 * Copyright (c) 2018 Pantheon Technologies, s.ro.. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.Container;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.ContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.Keyed;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.KeyedBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.KeyedKey;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.Unkeyed;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.UnkeyedBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class Mdsal298Test extends AbstractDataBrokerTest {
    private static final InstanceIdentifier<Container> CONTAINER = InstanceIdentifier.create(Container.class);
    private static final DataTreeIdentifier<Container> CONTAINER_TID = DataTreeIdentifier.create(CONFIGURATION,
        CONTAINER);
    private static final NodeIdentifier CONTAINER_NID = new NodeIdentifier(Container.QNAME);
    private static final QName FOO_QNAME = QName.create(Container.QNAME, "foo");

    @Test
    public void testKeyedDataTreeModification() throws InterruptedException, ExecutionException {
        final DataTreeChangeListener<Container> listener = assertWrittenContainer();

        final DOMDataTreeWriteTransaction domTx = getDomBroker().newWriteOnlyTransaction();
        domTx.put(CONFIGURATION, YangInstanceIdentifier.create(CONTAINER_NID).node(Keyed.QNAME),
            Builders.orderedMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(Keyed.QNAME))
            .addChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(Keyed.QNAME, ImmutableMap.of(FOO_QNAME, "foo")))
                .addChild(ImmutableNodes.leafNode(FOO_QNAME, "foo"))
                .build())
            .addChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(Keyed.QNAME, ImmutableMap.of(FOO_QNAME, "bar")))
                .addChild(ImmutableNodes.leafNode(FOO_QNAME, "bar"))
                .build())
            .build());
        domTx.submit().get();

        final ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(listener).onDataTreeChanged(captor.capture());
        Collection<DataTreeModification<Container>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<Container> change = capture.iterator().next();
        assertEquals(CONTAINER_TID, change.getRootPath());
        final DataObjectModification<Container> changedContainer = change.getRootNode();
        assertEquals(new Item<>(Container.class), changedContainer.getIdentifier());
        assertEquals(ModificationType.SUBTREE_MODIFIED, changedContainer.getModificationType());

        final Container containerAfter = changedContainer.getDataAfter();
        assertEquals(new ContainerBuilder()
            .setKeyed(ImmutableList.of(
                new KeyedBuilder().setFoo("foo").setKey(new KeyedKey("foo")).build(),
                new KeyedBuilder().setFoo("bar").setKey(new KeyedKey("bar")).build()))
            .build(), containerAfter);

        final Collection<DataObjectModification<?>> changedChildren = changedContainer.getModifiedChildren();
        assertEquals(2, changedChildren.size());

        final Iterator<DataObjectModification<?>> it = changedChildren.iterator();
        final DataObjectModification<?> changedChild1 = it.next();
        assertEquals(ModificationType.WRITE, changedChild1.getModificationType());
        assertEquals(Collections.emptyList(), changedChild1.getModifiedChildren());
        final Keyed child1After = (Keyed) changedChild1.getDataAfter();
        assertEquals("foo", child1After.getFoo());

        final DataObjectModification<?> changedChild2 = it.next();
        assertEquals(ModificationType.WRITE, changedChild2.getModificationType());
        assertEquals(Collections.emptyList(), changedChild2.getModifiedChildren());
        final Keyed child2After = (Keyed) changedChild2.getDataAfter();
        assertEquals("bar", child2After.getFoo());
    }

    @Test
    public void testUnkeyedDataTreeModification() throws InterruptedException, ExecutionException {
        final DataTreeChangeListener<Container> listener = assertWrittenContainer();

        final DOMDataTreeWriteTransaction domTx = getDomBroker().newWriteOnlyTransaction();
        domTx.put(CONFIGURATION, YangInstanceIdentifier.create(CONTAINER_NID).node(Unkeyed.QNAME),
            Builders.unkeyedListBuilder()
            .withNodeIdentifier(new NodeIdentifier(Unkeyed.QNAME))
            .withChild(Builders.unkeyedListEntryBuilder()
                .withNodeIdentifier(new NodeIdentifier(Unkeyed.QNAME))
                .addChild(ImmutableNodes.leafNode(FOO_QNAME, "foo"))
                .build())
            .withChild(Builders.unkeyedListEntryBuilder()
                .withNodeIdentifier(new NodeIdentifier(Unkeyed.QNAME))
                .addChild(ImmutableNodes.leafNode(FOO_QNAME, "bar"))
                .build())
            .build());
        domTx.submit().get();

        final ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(listener).onDataTreeChanged(captor.capture());
        Collection<DataTreeModification<Container>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<Container> change = capture.iterator().next();
        assertEquals(CONTAINER_TID, change.getRootPath());
        final DataObjectModification<Container> changedContainer = change.getRootNode();
        assertEquals(new Item<>(Container.class), changedContainer.getIdentifier());
        assertEquals(ModificationType.WRITE, changedContainer.getModificationType());

        final Container containerAfter = changedContainer.getDataAfter();
        assertEquals(new ContainerBuilder()
                .setUnkeyed(ImmutableList.of(
                    new UnkeyedBuilder().setFoo("foo").build(),
                    new UnkeyedBuilder().setFoo("bar").build()))
                .build(), containerAfter);

        final Collection<DataObjectModification<?>> changedChildren = changedContainer.getModifiedChildren();
        assertEquals(0, changedChildren.size());
    }

    private DataTreeChangeListener<Container> assertWrittenContainer() throws InterruptedException, ExecutionException {
        final DataTreeChangeListener<Container> listener = mock(DataTreeChangeListener.class);
        doNothing().when(listener).onDataTreeChanged(any(Collection.class));
        getDataBroker().registerDataTreeChangeListener(CONTAINER_TID, listener);

        final DOMDataTreeWriteTransaction domTx = getDomBroker().newWriteOnlyTransaction();
        domTx.put(CONFIGURATION, YangInstanceIdentifier.create(CONTAINER_NID),
            Builders.containerBuilder().withNodeIdentifier(CONTAINER_NID).build());
        domTx.submit().get();

        final ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(listener).onDataTreeChanged(captor.capture());
        Collection<DataTreeModification<Container>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<Container> change = capture.iterator().next();
        assertEquals(CONTAINER_TID, change.getRootPath());
        final DataObjectModification<Container> changedContainer = change.getRootNode();
        assertEquals(ModificationType.WRITE, changedContainer.getModificationType());
        assertEquals(new Item<>(Container.class), changedContainer.getIdentifier());

        final Container containerAfter = changedContainer.getDataAfter();
        assertEquals(new ContainerBuilder().build(), containerAfter);

        // No further modifications should occur
        assertEquals(Collections.emptyList(), changedContainer.getModifiedChildren());

        reset(listener);
        doNothing().when(listener).onDataTreeChanged(any(Collection.class));

        return listener;
    }

}
