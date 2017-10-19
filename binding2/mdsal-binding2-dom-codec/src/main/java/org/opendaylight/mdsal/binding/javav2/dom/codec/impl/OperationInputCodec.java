/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

/**
 * Marker interface for codecs dealing with operation input being potentially unmapped. We use this interface
 * to mark both {@link UnmappedOperationInputCodec} and ContainerNodeCodecContext, which results in bimorphic
 * invocation in BindingNormalizedNodeCodecRegistry.
 *
 * <p>
 * Without this interface we could end up with megamorphic invocation, as the two implementations cannot share
 * class hierarchy.
 *
 * @param <D>
 *            - Binding representation of data
 */
@Beta
public interface OperationInputCodec<D extends TreeNode> extends BindingNormalizedNodeCodec<D> {

}
