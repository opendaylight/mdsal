/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.io.IOException;
import java.util.Set;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

public final class AugmentationNodeContext<D extends DataObject & Augmentation<?>>
        extends DataObjectCodecContext<D, AugmentRuntimeType> {

    private final Set<PathArgument> childPaths;
    private final Set<Class<?>> childTypes;

    AugmentationNodeContext(final DataContainerCodecPrototype<AugmentRuntimeType> prototype) {
        super(prototype);
        this.childPaths = getChildPathArguments();
        this.childTypes = getChildBindingClasses();
    }

    @Override
    public D deserialize(final NormalizedNode data) {
        return createBindingProxy(checkDataArgument(ContainerNode.class, data));
    }

    @Override
    protected Object deserializeObject(final NormalizedNode normalizedNode) {
        return deserialize(normalizedNode);
    }

    /**
     *  Serializes the augmentation data into a standalone normalized node. Due to augmentation requires a parent
     *  container-like node and may include multiple child nodes, the wrapper node will be created.
     *
     * @param data augmentation data
     * @param pathArgument the path argument for wrapper node
     * @return normalized node
     */
    public NormalizedNode serializeToContainerNode(D data, YangInstanceIdentifier.PathArgument pathArgument) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        try {
            domWriter.startContainerNode(new YangInstanceIdentifier.NodeIdentifier(pathArgument.getNodeType()),
                BindingStreamEventWriter.UNKNOWN_SIZE);
            writeAsNormalizedNode(data, domWriter);
            domWriter.endNode();
            return result.getResult();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets path arguments for all child nodes provided by current augmentation.
     *
     * @return set of path arguments
     */
    public Set<PathArgument> getChildPaths() {
        return childPaths;
    }

    /**
     * Gets binging classes for all child elements.
     *
     * @return set of types
     */
    public Set<Class<?>> getChildTypes() {
        return childTypes;
    }
}