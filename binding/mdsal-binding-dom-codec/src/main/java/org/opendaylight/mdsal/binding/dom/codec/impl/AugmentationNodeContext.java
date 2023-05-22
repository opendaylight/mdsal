/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import org.opendaylight.mdsal.binding.dom.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class AugmentationNodeContext<D extends DataObject & Augmentation<?>>
        extends AbstractDataObjectCodecContext<D, AugmentRuntimeType> implements BindingAugmentationCodecTreeNode<D> {
    AugmentationNodeContext(final DataContainerCodecPrototype<AugmentRuntimeType> prototype) {
        super(prototype);
    }

    @Override
    public PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        return null;
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final PathArgument arg) {
        checkArgument(arg == null, "Unexpected argument %s", arg);
        return null;
    }

//    @Override
//    public D deserialize(final NormalizedNode data) {
//        return createBindingProxy(checkDataArgument(ContainerNode.class, data));
//    }

    @Override
    protected Object deserializeObject(final NormalizedNode normalizedNode) {
        return createBindingProxy(checkDataArgument(DataContainerNode.class, normalizedNode));
    }
}
