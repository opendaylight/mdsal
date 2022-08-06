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
    depluralize = true,
    strictBuilder = true,
    visibility = ImplementationVisibility.PRIVATE,
    packageGenerated = "*.impl.archetype",
    allowedClasspathAnnotations = { SuppressWarnings.class, Generated.class })
public sealed interface Archetype extends GeneratedType {
    /**
     * Return the underlying {@link JavaConstruct}.
     *
     * @return the underlying {@link JavaConstruct}
     */
    @NonNull JavaConstruct construct();

    /**
     * The archetype of an interface generated for an {@code action}.
     */
    non-sealed interface Action extends Archetype, WithStatement {
        @Override
        JavaConstruct.Interface construct();

        @Override
        ActionEffectiveStatement statement();
    }

    /**
     * The archetype of an builder class generated particular {@link #target()}.
     */
    non-sealed interface Builder extends Archetype {
        @Override
        JavaConstruct.Class construct();

        /**
         * Return target {@link InterfaceArchetype}.
         *
         * @return target {@link InterfaceArchetype}.
         */
        DataObject target();
    }

    sealed interface DataObject extends Archetype, WithStatement {
        @Override
        JavaConstruct.Interface construct();

        non-sealed interface Augmentation extends DataObject {
            @Override
            AugmentEffectiveStatement statement();
        }

        non-sealed interface Input extends DataObject {
            @Override
            InputEffectiveStatement statement();
        }

        non-sealed interface Output extends DataObject {
            @Override
            OutputEffectiveStatement statement();
        }

        non-sealed interface Map extends DataObject {
            @Override
            ListEffectiveStatement statement();

            // Note: forward reference, hence full type
            @NonNull Key key();
        }

        non-sealed interface Notification extends DataObject {
            @Override
            NotificationEffectiveStatement statement();
        }

        non-sealed interface InstanceNotification extends DataObject {
            @Override
            NotificationEffectiveStatement statement();
        }
    }

    /**
     * The archetype of an enum generated particular {@link #target()}.
     */
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

    /**
     * The archetype of a class generated particular {@link #target()}.
     */
    non-sealed interface Feature extends Archetype, WithStatement {
        @Override
        JavaConstruct.Class construct();

        @Override
        FeatureEffectiveStatement statement();
    }

    non-sealed interface Grouping extends Archetype, WithStatement {
        @Override
        JavaConstruct.Interface construct();

        @Override
        GroupingEffectiveStatement statement();
    }

    @Value.Immutable
    non-sealed interface Key extends Archetype, WithStatement {
        @Override
        // FIXME: JavaConstruct.Record
        JavaConstruct.Class construct();

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

    sealed interface OpaqueObject extends Archetype, WithStatement {
        @Override
        JavaConstruct.Interface construct();

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

    non-sealed interface Rpc extends Archetype, WithStatement {
        @Override
        JavaConstruct.Interface construct();

        @Override
        RpcEffectiveStatement statement();
    }

    sealed interface TypeObject extends Archetype, WithStatement {
        @Override
        JavaConstruct.Class construct();

        non-sealed interface Type extends TypeObject {
            @Override
            TypeEffectiveStatement<?> statement();
        }

        non-sealed interface Typedef extends TypeObject {
            @Override
            TypedefEffectiveStatement statement();
        }
    }

    /**
     * An {@link Archetype} which has an associated {@link EffectiveStatement}.
     */
    sealed interface WithStatement {
        /**
         * Return associated {@link EffectiveStatement}.
         *
         * @return associated {@link EffectiveStatement}
         */
        EffectiveStatement<?, ?> statement();
    }
}
