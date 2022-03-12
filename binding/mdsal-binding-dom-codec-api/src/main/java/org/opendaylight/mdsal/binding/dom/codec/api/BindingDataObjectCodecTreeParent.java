/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Common interface for entities which can supply a {@link BindingDataObjectCodecTreeNode} based on Binding DataObject
 * class instance.
 */
@Beta
public interface BindingDataObjectCodecTreeParent {
    /**
     * Returns child context as if it was walked by {@link BindingStreamEventWriter}. This means that to enter case,
     * one must issue {@code getChild(ChoiceClass).getChild(CaseClass)}.
     *
     * @param <E> Stream child DataObject type
     * @param childClass Child class by Binding Stream navigation
     * @return Context of child
     * @throws IllegalArgumentException If supplied child class is not valid in specified context.
     */
    <E extends DataObject> @NonNull BindingDataObjectCodecTreeNode<E> streamChild(@NonNull Class<E> childClass);
}
