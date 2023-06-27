/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.opendaylight.mdsal.binding.dom.codec.api.CommonDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeTypeContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Base implementation of {@link CommonDataObjectCodecTreeNode}.
 */
abstract sealed class CommonDataObjectCodecContext<D extends DataObject, T extends RuntimeTypeContainer>
        extends DataContainerCodecContext<D, T> implements CommonDataObjectCodecTreeNode<D>
        permits AbstractDataObjectCodecContext, ChoiceCodecContext {
    CommonDataObjectCodecContext(final CommonDataObjectCodecPrototype<T> prototype) {
        super(prototype);
    }
}
