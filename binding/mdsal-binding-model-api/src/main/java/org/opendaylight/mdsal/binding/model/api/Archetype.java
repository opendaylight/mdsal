/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.immutables.value.Generated;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.opendaylight.mdsal.binding.model.api.impl.archetype.KeyBuilder;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;


/**
 * Transitional interface to for expressing proper {@link GeneratedType} archetype.
 */
// FIXME: Remove this interface when we eliminate GeneratedTransferObject and direct GeneratedType builders
@Beta
@Value.Style(
    strictBuilder = true,
    visibility = ImplementationVisibility.PRIVATE,
    packageGenerated = "*.impl.archetype",
    allowedClasspathAnnotations = { SuppressWarnings.class, Generated.class })
public sealed interface Archetype extends GeneratedType {

    @NonNull JavaConstruct construct();

    sealed interface ClassArchetype extends Archetype {
        @Override
        JavaConstruct.Class construct();
    }

    sealed interface InterfaceArchetype extends Archetype {
        @Override
        JavaConstruct.Interface construct();
    }

    sealed interface WithStatement {

        EffectiveStatement<?, ?> statement();
    }

    non-sealed interface Builder extends ClassArchetype {

        InterfaceArchetype target();
    }

    non-sealed interface Action extends InterfaceArchetype, WithStatement {
        @Override
        ActionEffectiveStatement statement();
    }

    sealed interface Enum extends Archetype, WithStatement {
        @Override
        JavaConstruct.Enum construct();

        non-sealed interface Type extends Enum {
            @Override
            TypeEffectiveStatement<?> statement();
        }

        non-sealed interface Typedef extends Enum {
            @Override
            TypedefEffectiveStatement statement();
        }
    }

    non-sealed interface Feature extends ClassArchetype, WithStatement {
        @Override
        FeatureEffectiveStatement statement();
    }

    non-sealed interface GlobalNotification extends InterfaceArchetype, WithStatement {
        @Override
        NotificationEffectiveStatement statement();
    }

    non-sealed interface InstanceNotification extends InterfaceArchetype, WithStatement {
        @Override
        NotificationEffectiveStatement statement();
    }

    non-sealed interface Rpc extends InterfaceArchetype, WithStatement {
        @Override
        RpcEffectiveStatement statement();
    }

    sealed interface OperationContainer extends InterfaceArchetype, WithStatement {
        non-sealed interface Input extends OperationContainer {
            @Override
            InputEffectiveStatement statement();
        }

        non-sealed interface Output extends OperationContainer {
            @Override
            OutputEffectiveStatement statement();
        }
    }

    non-sealed interface Augmentation extends InterfaceArchetype, WithStatement {
        @Override
        AugmentEffectiveStatement statement();
    }

    non-sealed interface Grouping extends InterfaceArchetype, WithStatement {
        @Override
        GroupingEffectiveStatement statement();
    }

    non-sealed interface Map extends InterfaceArchetype, WithStatement {
        @Override
        ListEffectiveStatement statement();

        // Note: forward reference, hence full type
        @NonNull Key key();
    }

    @Value.Immutable
    // FIXME: Archetype with JavaConstruct.Record
    non-sealed interface Key extends ClassArchetype, WithStatement {
        @Override
        KeyEffectiveStatement statement();

        // Note: back reference, hence plain Type
        @NonNull Type map();

        static Builder builder() {
            return new Builder();
        }

        final class Builder extends KeyBuilder {
            Builder() {
                // Hidden on purpose
            }
        }
    }

    sealed interface OpaqueObject extends InterfaceArchetype, WithStatement {
        @Override
        DataTreeEffectiveStatement<?> statement();

        non-sealed interface Anydata extends OpaqueObject {
            @Override
            AnydataEffectiveStatement statement();
        }

        non-sealed interface Anyxml extends OpaqueObject {
            @Override
            AnyxmlEffectiveStatement statement();
        }
    }

    sealed interface TypeObject extends ClassArchetype, WithStatement {
        non-sealed interface Type extends TypeObject {
            @Override
            TypeEffectiveStatement<?> statement();
        }

        non-sealed interface Typedef extends TypeObject {
            @Override
            TypedefEffectiveStatement statement();
        }
    }
}
