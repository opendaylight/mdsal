/*
 * Copyright (c) 2018 Pantheon Technologies, s.ro.. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.AddressableCont;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.AddressableContBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.Container;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.ContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.UnaddressableCont;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.UnaddressableContBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.WithChoice;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.WithChoiceBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.addressable.cont.AddressableChild;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.addressable.cont.AddressableChildBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.Keyed;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.KeyedBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.KeyedKey;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.Unkeyed;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.UnkeyedBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.with.choice.Foo;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.with.choice.foo.addressable._case.Addressable;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.with.choice.foo.addressable._case.AddressableBuilder;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
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
    private static final QName BAZ_QNAME = QName.create(UnaddressableCont.QNAME, "baz");

    private static final InstanceIdentifier<WithChoice> CHOICE_CONTAINER = InstanceIdentifier.create(WithChoice.class);
    private static final DataTreeIdentifier<WithChoice> CHOICE_CONTAINER_TID = DataTreeIdentifier.create(CONFIGURATION,
        CHOICE_CONTAINER);
    private static final NodeIdentifier CHOICE_CONTAINER_NID = new NodeIdentifier(WithChoice.QNAME);
    private static final NodeIdentifier CHOICE_NID = new NodeIdentifier(Foo.QNAME);
    private static final InstanceIdentifier<Addressable> ADDRESSABLE_CASE = CHOICE_CONTAINER
            .child((Class)Addressable.class);

    private static final InstanceIdentifier<AddressableCont> ADDRESSABLE_CONTAINER =
            InstanceIdentifier.create(AddressableCont.class);
    private static final DataTreeIdentifier<AddressableCont> ADDRESSABLE_CONTAINER_TID = DataTreeIdentifier.create(
        CONFIGURATION, ADDRESSABLE_CONTAINER);
    private static final NodeIdentifier ADDRESSABLE_CONTAINER_NID = new NodeIdentifier(AddressableCont.QNAME);

    private static final InstanceIdentifier<UnaddressableCont> UNADDRESSABLE_CONTAINER =
            InstanceIdentifier.create(UnaddressableCont.class);
    private static final DataTreeIdentifier<UnaddressableCont> UNADDRESSABLE_CONTAINER_TID = DataTreeIdentifier.create(
        CONFIGURATION, UNADDRESSABLE_CONTAINER);
    private static final NodeIdentifier UNADDRESSABLE_CONTAINER_NID = new NodeIdentifier(UnaddressableCont.QNAME);

    @Test
    public void testKeyedDataTreeModification() throws InterruptedException, ExecutionException {
        final DataTreeChangeListener<Container> listener = assertWrittenContainer(Container.QNAME, Container.class,
            new ContainerBuilder().build());

        final DOMDataTreeWriteTransaction domTx = getDomBroker().newWriteOnlyTransaction();
        domTx.put(CONFIGURATION, YangInstanceIdentifier.create(CONTAINER_NID).node(Keyed.QNAME),
            Builders.orderedMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(Keyed.QNAME))
            .addChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(Keyed.QNAME, FOO_QNAME, "foo"))
                .addChild(ImmutableNodes.leafNode(FOO_QNAME, "foo"))
                .build())
            .addChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(Keyed.QNAME, FOO_QNAME, "bar"))
                .addChild(ImmutableNodes.leafNode(FOO_QNAME, "bar"))
                .build())
            .build());
        domTx.commit().get();

        final var captor = captor(Container.class);
        verify(listener).onDataTreeChanged(captor.capture());
        List<DataTreeModification<Container>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<Container> change = capture.iterator().next();
        assertEquals(CONTAINER_TID, change.getRootPath());
        final DataObjectModification<Container> changedContainer = change.getRootNode();
        assertEquals(Item.of(Container.class), changedContainer.getIdentifier());
        assertEquals(ModificationType.SUBTREE_MODIFIED, changedContainer.getModificationType());

        final Container containerAfter = changedContainer.getDataAfter();
        assertEquals(new ContainerBuilder()
            .setKeyed(List.of(
                new KeyedBuilder().setFoo("foo").withKey(new KeyedKey("foo")).build(),
                new KeyedBuilder().setFoo("bar").withKey(new KeyedKey("bar")).build()))
            .build(), containerAfter);

        final Collection<? extends DataObjectModification<?>> changedChildren = changedContainer.getModifiedChildren();
        assertEquals(2, changedChildren.size());

        final Iterator<? extends DataObjectModification<?>> it = changedChildren.iterator();
        final DataObjectModification<?> changedChild1 = it.next();
        assertEquals(ModificationType.WRITE, changedChild1.getModificationType());
        assertEquals(List.of(), changedChild1.getModifiedChildren());
        final Keyed child1After = (Keyed) changedChild1.getDataAfter();
        assertEquals("foo", child1After.getFoo());

        final DataObjectModification<?> changedChild2 = it.next();
        assertEquals(ModificationType.WRITE, changedChild2.getModificationType());
        assertEquals(List.of(), changedChild2.getModifiedChildren());
        final Keyed child2After = (Keyed) changedChild2.getDataAfter();
        assertEquals("bar", child2After.getFoo());
    }

    @Test
    public void testUnkeyedDataTreeModification() throws InterruptedException, ExecutionException {
        final DataTreeChangeListener<Container> listener = assertWrittenContainer(Container.QNAME, Container.class,
            new ContainerBuilder().build());

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
        domTx.commit().get();

        final var captor = captor(Container.class);
        verify(listener).onDataTreeChanged(captor.capture());
        List<DataTreeModification<Container>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<Container> change = capture.iterator().next();
        assertEquals(CONTAINER_TID, change.getRootPath());
        final DataObjectModification<Container> changedContainer = change.getRootNode();
        assertEquals(Item.of(Container.class), changedContainer.getIdentifier());
        assertEquals(ModificationType.WRITE, changedContainer.getModificationType());

        final Container containerAfter = changedContainer.getDataAfter();
        assertEquals(new ContainerBuilder()
                .setUnkeyed(List.of(
                    new UnkeyedBuilder().setFoo("foo").build(),
                    new UnkeyedBuilder().setFoo("bar").build()))
                .build(), containerAfter);

        final Collection<? extends DataObjectModification<?>> changedChildren = changedContainer.getModifiedChildren();
        assertEquals(0, changedChildren.size());
    }

    @Test
    public void testChoiceDataTreeModificationAddressable() throws InterruptedException, ExecutionException {
        final DataTreeChangeListener<WithChoice> listener = assertWrittenWithChoice();

        doNothing().when(listener).onDataTreeChanged(anyList());

        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, ADDRESSABLE_CASE, new AddressableBuilder().build());
        writeTx.commit().get();

        final var captor = captor(WithChoice.class);
        verify(listener).onDataTreeChanged(captor.capture());
        List<DataTreeModification<WithChoice>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<WithChoice> choiceChange = capture.iterator().next();
        assertEquals(CHOICE_CONTAINER_TID, choiceChange.getRootPath());
        final DataObjectModification<WithChoice> changedContainer = choiceChange.getRootNode();
        assertEquals(ModificationType.SUBTREE_MODIFIED, changedContainer.getModificationType());
        assertEquals(Item.of(WithChoice.class), changedContainer.getIdentifier());

        final Collection<? extends DataObjectModification<?>> choiceChildren = changedContainer.getModifiedChildren();
        assertEquals(1, choiceChildren.size());

        final DataObjectModification<Addressable> changedCase = (DataObjectModification<Addressable>) choiceChildren
                .iterator().next();
        assertEquals(ModificationType.WRITE, changedCase.getModificationType());
        assertEquals(Item.of(Addressable.class), changedCase.getIdentifier());
        assertEquals(new AddressableBuilder().build(), changedCase.getDataAfter());
    }

    @Test
    public void testDataTreeModificationAddressable() throws InterruptedException, ExecutionException {
        final DataTreeChangeListener<AddressableCont> listener = assertWrittenContainer(AddressableCont.QNAME,
            AddressableCont.class, new AddressableContBuilder().build());

        doNothing().when(listener).onDataTreeChanged(anyList());

        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, ADDRESSABLE_CONTAINER.child(AddressableChild.class),
            new AddressableChildBuilder().build());
        writeTx.commit().get();

        final var captor = captor(AddressableCont.class);
        verify(listener).onDataTreeChanged(captor.capture());
        List<DataTreeModification<AddressableCont>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<AddressableCont> contChange = capture.iterator().next();
        assertEquals(ADDRESSABLE_CONTAINER_TID, contChange.getRootPath());
        final DataObjectModification<AddressableCont> changedContainer = contChange.getRootNode();
        assertEquals(ModificationType.SUBTREE_MODIFIED, changedContainer.getModificationType());
        assertEquals(Item.of(AddressableCont.class), changedContainer.getIdentifier());

        final Collection<? extends DataObjectModification<?>> contChildren = changedContainer.getModifiedChildren();
        assertEquals(1, contChildren.size());

        final DataObjectModification<Addressable> changedChild = (DataObjectModification<Addressable>) contChildren
                .iterator().next();
        assertEquals(ModificationType.WRITE, changedChild.getModificationType());
        assertEquals(Item.of(AddressableChild.class), changedChild.getIdentifier());
        assertEquals(new AddressableChildBuilder().build(), changedChild.getDataAfter());
    }

    @Test
    public void testDataTreeModificationUnaddressable() throws InterruptedException, ExecutionException {
        final DataTreeChangeListener<UnaddressableCont> listener = assertWrittenContainer(UnaddressableCont.QNAME,
            UnaddressableCont.class, new UnaddressableContBuilder().build());

        doNothing().when(listener).onDataTreeChanged(anyList());

        final DOMDataTreeWriteTransaction domTx = getDomBroker().newWriteOnlyTransaction();
        domTx.put(CONFIGURATION, YangInstanceIdentifier.create(UNADDRESSABLE_CONTAINER_NID)
            .node(QName.create(UnaddressableCont.QNAME, "baz")),
            ImmutableNodes.leafNode(BAZ_QNAME, "baz"));
        domTx.commit().get();

        final var captor = captor(UnaddressableCont.class);
        verify(listener).onDataTreeChanged(captor.capture());
        List<DataTreeModification<UnaddressableCont>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<UnaddressableCont> contChange = capture.iterator().next();
        assertEquals(UNADDRESSABLE_CONTAINER_TID, contChange.getRootPath());
        final DataObjectModification<UnaddressableCont> changedContainer = contChange.getRootNode();
        assertEquals(ModificationType.WRITE, changedContainer.getModificationType());
        assertEquals(Item.of(UnaddressableCont.class), changedContainer.getIdentifier());

        final Collection<? extends DataObjectModification<?>> contChildren = changedContainer.getModifiedChildren();
        assertEquals(0, contChildren.size());
    }

    @Test
    public void testChoiceDataTreeModificationUnaddressable() throws InterruptedException, ExecutionException {
        final DataTreeChangeListener<WithChoice> listener = assertWrittenWithChoice();

        doNothing().when(listener).onDataTreeChanged(anyList());

        final DOMDataTreeWriteTransaction domTx = getDomBroker().newWriteOnlyTransaction();
        domTx.put(CONFIGURATION, YangInstanceIdentifier.create(CHOICE_CONTAINER_NID).node(Foo.QNAME),
            Builders.choiceBuilder()
            .withNodeIdentifier(new NodeIdentifier(Foo.QNAME))
            .withChild(Builders.leafSetBuilder()
                .withNodeIdentifier(new NodeIdentifier(QName.create(Foo.QNAME, "unaddressable")))
                .withChildValue("foo")
                .build())
            .build());
        domTx.commit().get();

        final var captor = captor(WithChoice.class);
        verify(listener).onDataTreeChanged(captor.capture());
        List<DataTreeModification<WithChoice>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<WithChoice> choiceChange = capture.iterator().next();
        assertEquals(CHOICE_CONTAINER_TID, choiceChange.getRootPath());
        final DataObjectModification<WithChoice> changedContainer = choiceChange.getRootNode();

        // Should be write
        assertEquals(ModificationType.WRITE, changedContainer.getModificationType());
        assertEquals(Item.of(WithChoice.class), changedContainer.getIdentifier());

        final Collection<? extends DataObjectModification<?>> choiceChildren = changedContainer.getModifiedChildren();
        assertEquals(0, choiceChildren.size());
    }

    private <T extends ChildOf<? extends DataRoot>> DataTreeChangeListener<T> assertWrittenContainer(final QName qname,
            final Class<T> bindingClass, final T expected)
            throws InterruptedException, ExecutionException {
        final DataTreeChangeListener<T> listener = mock(DataTreeChangeListener.class);
        doNothing().when(listener).onDataTreeChanged(anyList());

        final DataTreeIdentifier<T> dti = DataTreeIdentifier.create(CONFIGURATION,
            InstanceIdentifier.create(bindingClass));
        getDataBroker().registerDataTreeChangeListener(dti, listener);

        final DOMDataTreeWriteTransaction domTx = getDomBroker().newWriteOnlyTransaction();
        domTx.put(CONFIGURATION, YangInstanceIdentifier.create(new NodeIdentifier(qname)),
            ImmutableNodes.containerNode(qname));
        domTx.commit().get();

        final var captor = captor(bindingClass);
        verify(listener).onDataTreeChanged(captor.capture());
        List<DataTreeModification<T>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<T> change = capture.iterator().next();
        assertEquals(dti, change.getRootPath());
        final DataObjectModification<T> changedContainer = change.getRootNode();
        assertEquals(ModificationType.WRITE, changedContainer.getModificationType());
        assertEquals(Item.of(bindingClass), changedContainer.getIdentifier());

        final T containerAfter = changedContainer.getDataAfter();
        assertEquals(expected, containerAfter);

        // No further modifications should occur
        assertEquals(List.of(), changedContainer.getModifiedChildren());

        reset(listener);
        doNothing().when(listener).onDataTreeChanged(anyList());
        return listener;
    }

    private DataTreeChangeListener<WithChoice> assertWrittenWithChoice() throws InterruptedException,
            ExecutionException {
        final DataTreeChangeListener<WithChoice> listener = mock(DataTreeChangeListener.class);
        doNothing().when(listener).onDataTreeChanged(anyList());
        getDataBroker().registerDataTreeChangeListener(CHOICE_CONTAINER_TID, listener);

        final DOMDataTreeWriteTransaction domTx = getDomBroker().newWriteOnlyTransaction();
        domTx.put(CONFIGURATION, YangInstanceIdentifier.create(CHOICE_CONTAINER_NID),
            Builders.containerBuilder()
            .withNodeIdentifier(CHOICE_CONTAINER_NID)
            .withChild(Builders.choiceBuilder().withNodeIdentifier(CHOICE_NID).build())
            .build());
        domTx.commit().get();

        final var captor = captor(WithChoice.class);
        verify(listener).onDataTreeChanged(captor.capture());
        List<DataTreeModification<WithChoice>> capture = captor.getValue();
        assertEquals(1, capture.size());

        final DataTreeModification<WithChoice> change = capture.iterator().next();
        assertEquals(CHOICE_CONTAINER_TID, change.getRootPath());
        final DataObjectModification<WithChoice> changedContainer = change.getRootNode();
        assertEquals(ModificationType.WRITE, changedContainer.getModificationType());
        assertEquals(Item.of(WithChoice.class), changedContainer.getIdentifier());

        final WithChoice containerAfter = changedContainer.getDataAfter();
        assertEquals(new WithChoiceBuilder().build(), containerAfter);

        // No further modifications should occur
        assertEquals(List.of(), changedContainer.getModifiedChildren());

        reset(listener);
        doNothing().when(listener).onDataTreeChanged(anyList());

        return listener;
    }

    @SuppressWarnings("unchecked")
    private static <T extends DataObject> ArgumentCaptor<List<DataTreeModification<T>>> captor(final Class<T> clazz) {
        return ArgumentCaptor.forClass(List.class);
    }
}
