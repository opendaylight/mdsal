/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public sealed interface DataObjectArchetype extends Archetype.WithStatement
        permits AugmentationArchetype, InputArchetype, InstanceNotificationArchetype, KeyAwareArchetype,
                NotificationArchetype, OutputArchetype {
    @Override
    default Class<JavaConstruct.Interface> construct() {
        return JavaConstruct.Interface.class;
    }

    List<DataObjectField<?>> fields();
}