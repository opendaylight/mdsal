/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * @author nite
 *
 */
final class AugmentationModification<A extends Augmentation<?>> implements DataObjectModification<A> {
    private final BindingAugmentationCodecTreeNode<A> codec;

    AugmentationModification(final BindingAugmentationCodecTreeNode<A> codec) {
        this.codec = requireNonNull(codec);
    }

}
