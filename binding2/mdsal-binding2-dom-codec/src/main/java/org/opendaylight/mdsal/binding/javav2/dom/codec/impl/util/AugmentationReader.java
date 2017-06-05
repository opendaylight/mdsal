/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.util;

import java.util.Map;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;

/**
 * Interface which sould be implemented by proxy {@link java.lang.reflect.InvocationHandler}
 * to obtain augmentations from proxy implementations of
 * {@link org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable}
 * object.
 *
 * If implemented proxy does not implement this interface, its augmentations are not
 * properly serialized / deserialized.
 */
public interface AugmentationReader {

    Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(Object obj);
}
