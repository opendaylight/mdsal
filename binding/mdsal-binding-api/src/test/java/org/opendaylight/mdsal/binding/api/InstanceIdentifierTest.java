/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.mdsal.binding.api.mock.FooChild;
import org.opendaylight.mdsal.binding.api.mock.FooRoot;
import org.opendaylight.mdsal.binding.api.mock.InstantiatedFoo;
import org.opendaylight.mdsal.binding.api.mock.Node;
import org.opendaylight.mdsal.binding.api.mock.NodeAugmentation;
import org.opendaylight.mdsal.binding.api.mock.NodeChild;
import org.opendaylight.mdsal.binding.api.mock.NodeChildKey;
import org.opendaylight.mdsal.binding.api.mock.NodeKey;
import org.opendaylight.mdsal.binding.api.mock.Nodes;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.powermock.reflect.Whitebox;

public class InstanceIdentifierTest {

    @Test
    public void constructWithPredicates() {
        final InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();

        assertNotNull(nodes);
        assertEquals(Nodes.class, nodes.getTargetType());

        final InstanceWildcard<Node> node = nodes.builder().wildcardChild(Node.class).build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.containsWildcard(node));
    }

    @Test
    public void fluentConstruction() {
        final InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();
        final KeyedInstanceIdentifier<Node, NodeKey> node =
                InstanceIdentifier.builder(Nodes.class)
                        .child(Node.class,new NodeKey(10))
                        .build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }

    @Test
    public void negativeContains() {
        final InstanceIdentifier<FooChild> fooChild =
                InstanceIdentifier.builder(Nodes.class).child(InstantiatedFoo.class).child(FooChild.class).build();

        final InstanceIdentifier<Node> nodeTen = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).build();
        final InstanceIdentifier<Node> nodeOne = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(1)).build();
        final InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();

        assertFalse(fooChild.contains(nodeTen));
        assertFalse(nodeTen.contains(nodes));

        assertFalse(nodeOne.contains(nodes));
        assertTrue(nodes.contains(nodeOne));

        assertTrue(nodes.contains(nodeOne));
        assertFalse(nodeOne.contains(nodes));
    }

    @Test
    public void containsWildcarded() {
        final InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();
        assertThrows(IllegalArgumentException.class, () -> InstanceIdentifier.builder(Nodes.class)
                .child(Node.class).build());
        final InstanceWildcard<Node> wildcarded = InstanceIdentifier.builder(Nodes.class)
                .wildcardChild(Node.class)
                .build();
        assertThrows(IllegalArgumentException.class, () -> InstanceIdentifier.builder(Nodes.class)
                .child(Node.class)
                .child(NodeChild.class).build());
        final KeyedInstanceWildcard<NodeChild, NodeChildKey> wildcardedChildren =
                InstanceIdentifier.builder(Nodes.class)
                .wildcardChild(Node.class)
                .child(NodeChild.class, new NodeChildKey(75))
                .build();

        assertTrue(wildcarded instanceof InstanceWildcard<?>);
        assertTrue(wildcardedChildren instanceof KeyedInstanceWildcard<?,?>);
        assertEquals(75, wildcardedChildren.getKey().getId());

        final KeyedInstanceIdentifier<Node, NodeKey> nodeTen = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class,new NodeKey(10))
                .build();
        final KeyedInstanceIdentifier<Node, NodeKey> nodeOne = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class,new NodeKey(1))
                .build();

        assertTrue(nodeTen instanceof KeyedInstanceIdentifier<?, ?>);
        assertTrue(nodeOne instanceof KeyedInstanceIdentifier<?,?>);
        assertTrue(nodes.contains(nodeOne));
        assertTrue(wildcarded.containsWildcard(nodeOne));
        assertTrue(wildcarded.containsWildcard(nodeTen));
        assertFalse(InstanceIdentifier.builder(Nodes.class)
                .child(InstantiatedFoo.class).build().containsWildcard(wildcarded));

        assertThrows(IllegalArgumentException.class, () -> InstanceIdentifier.builder(Nodes.class)
                .child(Node.class,new NodeKey(10)).child(NodeChild.class).build());
        final InstanceWildcard<NodeChild> nodeTenChildWildcarded = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).wildcardChild(NodeChild.class).build();

        assertTrue(nodeTenChildWildcarded instanceof InstanceWildcard<?>);

        final InstanceIdentifier<NodeChild> nodeTenChild = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).child(NodeChild.class, new NodeChildKey(10)).build();
        final InstanceIdentifier<NodeChild> nodeOneChild = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(1)).child(NodeChild.class, new NodeChildKey(1)).build();

        assertFalse(nodeTenChildWildcarded.containsWildcard(nodeOneChild));
        assertTrue(nodeTenChildWildcarded.containsWildcard(nodeTenChild));
    }

    @Test
    public void basicTests() {
        final InstanceIdentifier<FooRoot> instanceIdentifier1 = InstanceIdentifier.create(FooRoot.class);
        final InstanceIdentifier<FooRoot> instanceIdentifier2 = InstanceIdentifier.create(FooRoot.class);
        final InstanceWildcard<NodeChild> instanceIdentifier3 = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(10)).wildcardChild(NodeChild.class).build();
        final Object object = new Object();

        assertNotNull(instanceIdentifier1);
        assertNotEquals(object, instanceIdentifier1);
        assertEquals(instanceIdentifier1, instanceIdentifier2);

        Whitebox.setInternalState(instanceIdentifier2, "pathArguments", instanceIdentifier1.pathArguments);

        assertEquals(instanceIdentifier1, instanceIdentifier2);
        assertNotEquals(instanceIdentifier1, instanceIdentifier3);

        final InstanceWildcard<Node> instanceIdentifier5 = InstanceIdentifier.create(Nodes.class)
                .wildcardChild(Node.class);
        Whitebox.setInternalState(instanceIdentifier5, "hash", instanceIdentifier1.hashCode());

        assertNotNull(InstanceIdentifier.unsafeOf(ImmutableList.copyOf(instanceIdentifier1.getPathArguments())));
        assertNotNull(InstanceIdentifier.create(Nodes.class).wildcardChild(Node.class));
        assertNotNull(InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(5)));
        assertNotNull(instanceIdentifier5.augmentation(NodeAugmentation.class));

        final InstanceIdentifierBuilder instanceIdentifierBuilder = instanceIdentifier1.builder();
        assertEquals(instanceIdentifier1.hashCode(), instanceIdentifierBuilder.hashCode());
        assertNotNull(instanceIdentifierBuilder.augmentation(InstantiatedFoo.class));
        assertNotNull(instanceIdentifierBuilder.build());
    }

    @Test
    public void firstIdentifierOfTest() {
        final InstanceIdentifier<Node> instanceIdentifier =
                InstanceIdentifier.builder(Nodes.class).child(Node.class,new NodeKey(10)).build();
        final InstanceIdentifier<Nodes> nodesIdentifier = instanceIdentifier.firstIdentifierOf(Nodes.class);
        assertNotNull(nodesIdentifier);
        final InstanceIdentifier<DataObject> dataObjectIdentifier =
                instanceIdentifier.firstIdentifierOf(DataObject.class);
        assertNull(dataObjectIdentifier);
    }

    @Test
    public void firstKeyOfTest() {
        final InstanceIdentifier<Node> instanceIdentifier =
                InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        final InstanceIdentifier<FooRoot> instanceIdentifier1 = InstanceIdentifier.create(FooRoot.class);
        assertNotNull(instanceIdentifier.firstKeyOf(Node.class));
        assertNull(instanceIdentifier1.firstKeyOf(Node.class));
    }

    @Test
    public void keyOfTest() {
        final Identifier<?> identifier = mock(Identifier.class);
        assertEquals(identifier, InstanceIdentifier.keyOf(
                new KeyedInstanceIdentifier(Identifiable.class, ImmutableList.of(), 0, identifier)));
    }

    @Test
    public void serializationTest() throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);

        final InstanceIdentifier<FooRoot> instanceIdentifier = InstanceIdentifier.create(FooRoot.class);
        outputStream.writeObject(instanceIdentifier);
        outputStream.flush();
        outputStream.close();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
        final InstanceIdentifier<?> deserialized = (InstanceIdentifier<?>) inputStream.readObject();

        assertEquals(instanceIdentifier, deserialized);
    }

    @Test
    public void equalsTest() {
        final InstanceIdentifierBuilder<FooRoot> builder1 =  InstanceIdentifier.create(FooRoot.class).builder();
        final InstanceIdentifierBuilder<FooRoot> builder2 =  InstanceIdentifier.create(FooRoot.class).builder();
        final InstanceIdentifierBuilder<Nodes> builder3 =  InstanceIdentifier.create(Nodes.class).builder();
        final InstanceIdentifierBuilder<Nodes> builder4 =  InstanceIdentifier.create(Nodes.class).builder();
        final Object obj = new Object();

        assertEquals(builder1, builder2);
        assertEquals(builder2, builder1);
        assertEquals(builder3, builder4);

        assertNotEquals(builder3, builder1);
        assertNotEquals(builder3, null);
        assertNotEquals(builder4, null);
        assertNotEquals(builder1, obj);

        builder3.child(Node.class, new NodeKey(10));
        assertNotEquals(builder3, builder4);
        assertNotEquals(builder4, builder3);

        builder4.child(Node.class, new NodeKey(20));
        assertNotEquals(builder3, builder4);
        assertNotEquals(builder4, builder3);

        final InstanceIdentifierBuilder<Nodes> iib1 = new InstanceIdentifierBuilderImpl<>(null, null, 31);
        final InstanceIdentifierBuilder<Nodes> iib2 = new InstanceIdentifierBuilderImpl<>(null, null, 31);
        assertEquals(iib1, iib2);
        assertEquals(iib2, iib1);
    }

    @Test
    public void hashCodeTest() {
        final InstanceIdentifierBuilder<FooRoot> builder1 =  InstanceIdentifier.create(FooRoot.class).builder();
        final InstanceIdentifierBuilder<FooRoot> builder2 =  InstanceIdentifier.create(FooRoot.class).builder();
        final InstanceIdentifierBuilder<Nodes> builder3 =  InstanceIdentifier.create(Nodes.class).builder();
        final InstanceIdentifierBuilder<Nodes> builder4 =  InstanceIdentifier.create(Nodes.class).builder();
        final Object obj = new Object();

        assertTrue(builder1.hashCode() == builder2.hashCode());
        assertTrue(builder1.hashCode() != builder3.hashCode());
        assertTrue(builder3.hashCode() == builder4.hashCode());
        assertTrue(builder2.hashCode() != builder4.hashCode());
        assertTrue(builder1.hashCode() != obj.hashCode());

        builder3.child(Node.class, new NodeKey(10));

        assertTrue(builder3.hashCode() != builder4.hashCode());
    }

    @Test
    public void verifyTargetTest() {
        final InstanceIdentifier<Nodes> nodeId = InstanceIdentifier.create(Nodes.class);
        assertSame(nodeId, nodeId.verifyTarget(Nodes.class));
        assertThrows(VerifyException.class, () -> nodeId.verifyTarget(Node.class));
    }

    @Test
    public void legacyConversionTest() {
        final var wildcard1 = InstanceIdentifier.builder(Nodes.class).wildcardChild(Node.class).build();
        final var wildcard2 = InstanceIdentifier.builder(Nodes.class).wildcardChild(Node.class)
                        .child(NodeChild.class, new NodeChildKey(11)).build();
        final var identifier1 = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();

        /*here we want to check if both methods return the same InstanceIdentifier*/
        final var legacyIdentifier1 = identifier1.toLegacy();
        assertTrue(legacyIdentifier1 instanceof org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier<?,?>);
        final var keyedLegacyIdentifier1 = identifier1.toLegacy();
        assertEquals(legacyIdentifier1, keyedLegacyIdentifier1);

        /*here we want to check if the backward conversion for non-keyed identifier yields the same identifier*/
        assertThrows(IllegalArgumentException.class, () -> InstanceWildcard.ofLegacy(legacyIdentifier1));
        final var identifier2 = KeyedInstanceIdentifier.ofLegacy(keyedLegacyIdentifier1);
        assertEquals(identifier1, identifier2);

        /*here we want to check the same for wildcards*/
        final var legacyIdentifier2 = wildcard1.toLegacy();
        assertEquals(wildcard1, InstanceWildcard.ofLegacy(legacyIdentifier2));
        assertThrows(IllegalArgumentException.class, () -> InstanceIdentifier.ofLegacy(legacyIdentifier2));

        /*
        * through a leak in the logic of legacy InstanceIdentifier, there exists no such thing as legacy
        * KeyedInstanceIdentifier that is wildcarded. Such state is represented by an InstanceIdentifier
        * whose last path argument is IdentifiableItem and is wildcarded. Hence, the obscure assertFalse.*/
        final var legacyIdentifier3 = wildcard2.toLegacy();
        assertTrue(legacyIdentifier3
                instanceof org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier<?,?>);
        assertEquals(wildcard2, InstanceWildcard.ofLegacy(legacyIdentifier3));
    }
}
