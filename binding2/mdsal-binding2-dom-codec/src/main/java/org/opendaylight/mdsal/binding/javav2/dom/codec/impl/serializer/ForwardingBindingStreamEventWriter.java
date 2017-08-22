/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.opendaylight.mdsal.binding.javav2.spec.base.Identifiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.IdentifiableItem;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;

/**
 * Forwarding of event stream writer of Binding v2 representation.
 */
@Beta
abstract class ForwardingBindingStreamEventWriter implements BindingStreamEventWriter {

    protected abstract BindingStreamEventWriter delegate();

    @Override
    public void leafNode(final String localName, final Object value) throws IOException {
        delegate().leafNode(localName, value);
    }

    @Override
    public void startLeafSet(final String localName, final int childSizeHint) throws IOException {
        delegate().startLeafSet(localName, childSizeHint);
    }

    @Override
    public void startOrderedLeafSet(final String localName, final int childSizeHint) throws IOException {
        delegate().startOrderedLeafSet(localName, childSizeHint);
    }

    @Override
    public void leafSetEntryNode(final Object value) throws IOException {
        delegate().leafSetEntryNode(value);
    }

    @Override
    public void startContainerNode(final Class<? extends TreeNode> container, final int childSizeHint)
            throws IOException {
        delegate().startContainerNode(container, childSizeHint);
    }

    @Override
    public void startUnkeyedList(final Class<? extends TreeNode> localName, final int childSizeHint)
            throws IOException {
        delegate().startUnkeyedList(localName, childSizeHint);
    }

    @Override
    public void startUnkeyedListItem(final int childSizeHint) throws IOException {
        delegate().startUnkeyedListItem(childSizeHint);
    }

    @Override
    public <T extends TreeNode & Identifiable<?>> void startMapNode(final Class<T> mapEntryType, final int childSizeHint)
            throws IOException {
        delegate().startMapNode(mapEntryType, childSizeHint);
    }

    @Override
    public <I extends TreeNode, T> void startOrderedMapNode(final IdentifiableItem<I, T> mapEntryType,
            final int childSizeHint) throws IOException {
        delegate().startOrderedMapNode(mapEntryType, childSizeHint);
    }

    @Override
    public <I extends TreeNode, T> void startMapEntryNode(final IdentifiableItem<I, T> keyValues,
            final int childSizeHint) throws IOException {
        delegate().startMapEntryNode(keyValues, childSizeHint);
    }

    @Override
    public <T extends TreeNode> void startChoiceNode(final Item<T> choice, final int childSizeHint) throws IOException {
        delegate().startChoiceNode(choice, childSizeHint);
    }

    @Override
    public void startCase(final Class<? extends TreeNode> caze, final int childSizeHint) throws IOException {
        delegate().startCase(caze, childSizeHint);
    }

    @Override
    public void startAugmentationNode(final Class<? extends Augmentation<?>> augmentationType) throws IOException {
        delegate().startAugmentationNode(augmentationType);
    }

    @Override
    public void startAnyxmlNode(final String name, final Object value) throws IOException {
        delegate().startAnyxmlNode(name, value);
    }

    @Override
    public void startAnydataNode(final String name, final Object value) throws IOException {
        delegate().startAnydataNode(name, value);
    }

    @Override
    public void endNode() throws IOException {
        delegate().endNode();
    }

    @Override
    public void flush() throws IOException {
        delegate().flush();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }

}
