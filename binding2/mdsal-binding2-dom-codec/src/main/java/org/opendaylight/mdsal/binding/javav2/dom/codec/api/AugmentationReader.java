/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.api;

import com.google.common.annotations.Beta;
import java.lang.reflect.InvocationHandler;
import java.util.Map;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;

/**
 * Interface which should be implemented by proxy {@link InvocationHandler} to
 * obtain augmentations from proxy implementations of {@link Augmentable}
 * object.
 *
 * <p>
 * If implemented proxy does not implement this interface, its augmentations are
 * not properly serialized / deserialized.
 */
@Beta
public interface AugmentationReader {

    /**
     * Get augmentations.
     *
     * @param obj
     *            - object implemented this interface
     * @return augmentations
     */
    Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(Object obj);
}