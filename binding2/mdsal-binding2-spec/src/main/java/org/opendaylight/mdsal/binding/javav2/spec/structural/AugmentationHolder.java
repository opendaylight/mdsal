/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.structural;

import java.util.Map;

/**
 *
 * Augmentable (extensible) object which could carry additional data defined by
 * third-party extensions, without introducing conflict between various
 * extensions.
 *
 * @param <T>
 *            Base class which should be target
 *            for augmentations.
 */
public interface AugmentationHolder<T> {

    /**
     * Returns map of all augmentations.
     *
     * @return map of all augmentations.
     */
    Map<Class<? extends Augmentation<T>>,Augmentation<T>> augmentations();
}