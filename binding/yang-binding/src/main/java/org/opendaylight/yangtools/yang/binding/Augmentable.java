/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Augmentable (extensible) object which could carry additional data defined by a third-party extension, without
 * introducing conflict between various extension.
 *
 * <p>
 * This interface uses extended version of ExtensibleInterface pattern which also adds marker interface for
 * augmentations (extensions) - {@link Augmentable}.
 *
 * @param <T>
 *            Base class which should implements this interface and is target
 *            for augmentation.
 * @author Tony Tkacik
 */
public interface Augmentable<T> {
    /**
     * Returns instance of augmentation.
     *
     * @param augmentationType Type of augmentation to be returned.
     * @param <E$$> Type capture for augmentation type
     * @return instance of augmentation.
     */
    // E$$ is an identifier which cannot be generated from models.
    @SuppressWarnings("checkstyle:methodTypeParameterName")
    <E$$ extends Augmentation<T>> @Nullable E$$ augmentation(Class<E$$> augmentationType);
}
