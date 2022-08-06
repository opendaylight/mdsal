/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNull;
import org.immutables.value.Generated;
import org.immutables.value.Value;


/**
 * Transitional interface to for expressing proper {@link GeneratedType} archetype.
 */
// FIXME: Remove this interface when we eliminate GeneratedTransferObject and direct GeneratedType builders
@Beta
public sealed interface Archetype extends GeneratedType
        permits Archetype.ClassArchetype, Archetype.InterfaceArchetype, Archetype.Enum {
    sealed interface ClassArchetype extends Archetype {
        @Override
        JavaConstruct.Class construct();
    }

    sealed interface InterfaceArchetype extends Archetype {
        @Override
        JavaConstruct.Interface construct();
    }

    non-sealed interface Builder extends ClassArchetype {
        // FIXME: target type, etc.
    }

    non-sealed interface Action extends InterfaceArchetype {

    }

    non-sealed interface Enum extends Archetype {
        @Override
        JavaConstruct.Enum construct();
    }

    non-sealed interface Feature extends ClassArchetype {

    }

    non-sealed interface GlobalNotification extends InterfaceArchetype {

    }

    non-sealed interface InstanceNotification extends InterfaceArchetype {

    }

    non-sealed interface Rpc extends InterfaceArchetype {

    }

    non-sealed interface OperationContainer extends InterfaceArchetype {

    }

    non-sealed interface Augmentation extends InterfaceArchetype {

    }

    non-sealed interface Grouping extends InterfaceArchetype {

    }

    non-sealed interface Map extends InterfaceArchetype {
        // Note: forward reference, hence full type
        @NonNull Key key();
    }

    @Value.Immutable
    @Value.Style(stagedBuilder = true, allowedClasspathAnnotations = {
        SuppressWarnings.class, Generated.class, SuppressFBWarnings.class,
    })

    non-sealed interface Key extends ClassArchetype {
        // Note: back reference, hence plain Type
        @NonNull Type map();
    }

    non-sealed interface OpaqueObject extends ClassArchetype {

    }

    non-sealed interface TypeObject extends ClassArchetype {

    }

    JavaConstruct construct();
}
