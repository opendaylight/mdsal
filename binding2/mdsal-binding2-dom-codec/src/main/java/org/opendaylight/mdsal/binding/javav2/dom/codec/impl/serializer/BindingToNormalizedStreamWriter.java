/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.CaseNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.KeyedListNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.LeafNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.NodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.TreeNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.spec.base.Identifiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.IdentifiableItem;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Stream event writer of Binding v2 representation.
 */
@Beta
public final class BindingToNormalizedStreamWriter
        implements BindingStreamEventWriter, Delegator<NormalizedNodeStreamWriter> {

    private final Deque<NodeCodecContext<?>> schema = new ArrayDeque<>();
    private final NormalizedNodeStreamWriter delegate;
    private final NodeCodecContext<?> rootNodeSchema;

    private BindingToNormalizedStreamWriter(final NodeCodecContext<?> rootNodeSchema,
            final NormalizedNodeStreamWriter delegate) {
        this.rootNodeSchema = Preconditions.checkNotNull(rootNodeSchema);
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    /**
     * Create instance of Binding v2 representation writer.
     *
     * @param schema
     *            - codec schema
     * @param delegate
     *            - DOM writer delegator
     * @return instance of binding writer
     */
    public static BindingToNormalizedStreamWriter create(final NodeCodecContext<?> schema,
            final NormalizedNodeStreamWriter delegate) {
        return new BindingToNormalizedStreamWriter(schema, delegate);
    }

    private void emitSchema(final Object schema) {
        delegate.nextDataSchemaNode((DataSchemaNode) schema);
    }

    /**
     * Retrieves, but does not remove, the head of the queue represented by node
     * codec context.
     *
     * @return head of queue
     */
    NodeCodecContext<?> current() {
        return schema.peek();
    }

    private NodeIdentifier duplicateSchemaEnter() {
        final NodeCodecContext<?> next;
        if (current() == null) {
            // Entry of first node
            next = rootNodeSchema;
        } else {
            next = current();
        }
        this.schema.push(next);
        return (NodeIdentifier) current().getDomPathArgument();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T extends YangInstanceIdentifier.PathArgument> T enter(final Class<?> name, final Class<T> identifier) {
        final NodeCodecContext<?> next;
        if (current() == null) {
            // Entry of first node
            next = rootNodeSchema;
        } else {
            Preconditions.checkArgument((current() instanceof DataContainerCodecContext), "Could not start node %s",
                    name);
            next = ((DataContainerCodecContext) current()).streamChild(name);
        }
        this.schema.push(next);
        return (T) next.getDomPathArgument();
    }

    @SuppressWarnings("rawtypes")
    private <T extends YangInstanceIdentifier.PathArgument> T enter(final String localName, final Class<T> identifier) {
        final NodeCodecContext<?> current = current();
        final NodeCodecContext<?> next = ((TreeNodeCodecContext) current).getLeafChild(localName);
        this.schema.push(next);
        @SuppressWarnings("unchecked")
        final T arg = (T) next.getDomPathArgument();
        return arg;
    }

    @Override
    public NormalizedNodeStreamWriter getDelegate() {
        return delegate;
    }

    @Override
    public void endNode() throws IOException {
        final NodeCodecContext<?> left = schema.pop();
        // NormalizedNode writer does not have entry into case, but into choice
        // so for leaving case, we do not emit endNode.
        if (!(left instanceof CaseNodeCodecContext)) {
            getDelegate().endNode();
        }
    }

    private Map.Entry<NodeIdentifier, Object> serializeLeaf(final String localName, final Object value) {
        Preconditions.checkArgument(current() instanceof TreeNodeCodecContext);

        final TreeNodeCodecContext<?, ?> currentCasted = (TreeNodeCodecContext<?, ?>) current();
        final LeafNodeCodecContext<?> leafContext = currentCasted.getLeafChild(localName);

        final NodeIdentifier domArg = (NodeIdentifier) leafContext.getDomPathArgument();
        final Object domValue = leafContext.getValueCodec().serialize(value);
        emitSchema(leafContext.getSchema());
        return new AbstractMap.SimpleEntry<>(domArg, domValue);
    }

    @Override
    public void leafNode(final String localName, final Object value) throws IOException {
        final Entry<NodeIdentifier, Object> dom = serializeLeaf(localName, value);
        getDelegate().leafNode(dom.getKey(), dom.getValue());
    }

    @Override
    public void startAnyxmlNode(final String name, final Object value) throws IOException {
        final Entry<NodeIdentifier, Object> dom = serializeLeaf(name, value);
        getDelegate().anyxmlNode(dom.getKey(), dom.getValue());
    }

    @Override
    public void leafSetEntryNode(final Object value) throws IOException {
        final LeafNodeCodecContext<?> ctx = (LeafNodeCodecContext<?>) current();
        getDelegate().leafSetEntryNode(ctx.getSchema().getQName(), ctx.getValueCodec().serialize(value));
    }

    @Override
    public void startAugmentationNode(final Class<? extends Augmentation<?>> augmentationType) throws IOException {
        getDelegate().startAugmentationNode(enter(augmentationType, AugmentationIdentifier.class));
    }

    @Override
    public void startCase(final Class<? extends TreeNode> caze, final int childSizeHint) {
        enter(caze, NodeIdentifier.class);
    }

    @Override
    public <T extends TreeNode> void startChoiceNode(final Item<T> choice, final int childSizeHint) throws IOException {
        getDelegate().startChoiceNode(enter(choice.getType(), NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startContainerNode(final Class<? extends TreeNode> object, final int childSizeHint)
            throws IOException {
        getDelegate().startContainerNode(enter(object, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startLeafSet(final String localName, final int childSizeHint) throws IOException {
        final NodeIdentifier id = enter(localName, NodeIdentifier.class);
        emitSchema(current().getSchema());
        getDelegate().startLeafSet(id, childSizeHint);
    }

    @Override
    public void startOrderedLeafSet(final String localName, final int childSizeHint) throws IOException {
        getDelegate().startOrderedLeafSet(enter(localName, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public <I extends TreeNode, T> void startMapEntryNode(final IdentifiableItem<I, T> keyValues,
            final int childSizeHint) throws IOException {
        duplicateSchemaEnter();
        final NodeIdentifierWithPredicates identifier = ((KeyedListNodeCodecContext<?>) current()).serialize(keyValues);
        getDelegate().startMapEntryNode(identifier, childSizeHint);
    }

    @Override
    public <T extends TreeNode & Identifiable<?>> void startMapNode(final Class<T> mapEntryType, final int childSizeHint)
            throws IOException {
        getDelegate().startMapNode(enter(mapEntryType, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public <I extends TreeNode, T> void startOrderedMapNode(final IdentifiableItem<I, T> mapEntryType,
            final int childSizeHint) throws IOException {
        getDelegate().startOrderedMapNode(enter(mapEntryType.getType(), NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startUnkeyedList(final Class<? extends TreeNode> localName, final int childSizeHint)
            throws IOException {
        getDelegate().startUnkeyedList(enter(localName, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startUnkeyedListItem(final int childSizeHint) throws IOException {
        getDelegate().startUnkeyedListItem(duplicateSchemaEnter(), childSizeHint);
    }

    @Override
    public void flush() throws IOException {
        getDelegate().flush();
    }

    @Override
    public void close() throws IOException {
        getDelegate().close();
    }

    @Override
    public void startAnydataNode(final String name, final Object value) throws IOException {
        // TODO will be done when https://bugs.opendaylight.org/show_bug.cgi?id=8516 is completed
    }
}
