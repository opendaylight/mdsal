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
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;

import com.google.common.collect.ImmutableList;
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
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.Container;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.ContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.Keyed;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.KeyedBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.KeyedKey;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.Unkeyed;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.UnkeyedBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;

public class Mdsal298Test extends AbstractDataBrokerTest {
    private static final InstanceIdentifier<Container> CONTAINER = InstanceIdentifier.create(Container.class);
    private static final DataTreeIdentifier<Container> CONTAINER_TID = DataTreeIdentifier.create(CONFIGURATION,
        CONTAINER);

    @Test
    public void testKeyedDataTreeModification() throws InterruptedException, ExecutionException {
        final Container cont = new ContainerBuilder()
                .setKeyed(ImmutableList.of(
                    new KeyedBuilder().setFoo("foo").setKey(new KeyedKey("foo")).build(),
                    new KeyedBuilder().setFoo("bar").setKey(new KeyedKey("bar")).build()))
                .build();

        final WriteTransaction tx = getDataBroker().newWriteOnlyTransaction();
        tx.put(CONFIGURATION, CONTAINER, cont);
        tx.submit().get();

        final DataTreeChangeListener<Container> listener = mock(DataTreeChangeListener.class);
        doNothing().when(listener).onDataTreeChanged(any(Collection.class));
        getDataBroker().registerDataTreeChangeListener(CONTAINER_TID, listener);

        final ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(listener).onDataTreeChanged(captor.capture());

        final Collection<DataTreeModification<Container>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<Container> change = capture.iterator().next();
        assertEquals(CONTAINER_TID, change.getRootPath());
        final DataObjectModification<Container> changedContainer = change.getRootNode();
        assertEquals(ModificationType.WRITE, changedContainer.getModificationType());
        assertEquals(new Item<>(Container.class), changedContainer.getIdentifier());

        final Container containerAfter = changedContainer.getDataAfter();
        assertEquals(cont, containerAfter);

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
        final Container cont = new ContainerBuilder()
                .setUnkeyed(ImmutableList.of(
                    new UnkeyedBuilder().setFoo("foo").build(),
                    new UnkeyedBuilder().setFoo("bar").build()))
                .build();

        final WriteTransaction tx = getDataBroker().newWriteOnlyTransaction();
        tx.put(CONFIGURATION, CONTAINER, cont);
        tx.submit().get();

        final DataTreeChangeListener<Container> listener = mock(DataTreeChangeListener.class);
        doNothing().when(listener).onDataTreeChanged(any(Collection.class));
        getDataBroker().registerDataTreeChangeListener(CONTAINER_TID, listener);

        final ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(listener).onDataTreeChanged(captor.capture());

        final Collection<DataTreeModification<Container>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<Container> change = capture.iterator().next();
        assertEquals(CONTAINER_TID, change.getRootPath());
        final DataObjectModification<Container> changedContainer = change.getRootNode();
        assertEquals(ModificationType.WRITE, changedContainer.getModificationType());
        assertEquals(new Item<>(Container.class), changedContainer.getIdentifier());

        final Container containerAfter = changedContainer.getDataAfter();
        assertEquals(cont, containerAfter);

        final Collection<DataObjectModification<?>> changedChildren = changedContainer.getModifiedChildren();
        assertEquals(2, changedChildren.size());

        final Iterator<DataObjectModification<?>> it = changedChildren.iterator();
        final DataObjectModification<?> changedChild1 = it.next();
        assertEquals(ModificationType.WRITE, changedChild1.getModificationType());
        assertEquals(Collections.emptyList(), changedChild1.getModifiedChildren());
        final Unkeyed child1After = (Unkeyed) changedChild1.getDataAfter();
        assertEquals("foo", child1After.getFoo());

        final DataObjectModification<?> changedChild2 = it.next();
        assertEquals(ModificationType.WRITE, changedChild2.getModificationType());
        assertEquals(Collections.emptyList(), changedChild2.getModifiedChildren());
        final Unkeyed child2After = (Unkeyed) changedChild2.getDataAfter();
        assertEquals("bar", child2After.getFoo());
    }

}
