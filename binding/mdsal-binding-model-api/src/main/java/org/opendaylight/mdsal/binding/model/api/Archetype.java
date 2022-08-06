/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.errorprone.annotations.DoNotMock;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.immutables.value.Generated;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.opendaylight.mdsal.binding.model.api.impl.archetype.KeyBuilder;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;


/**
 * Transitional interface to for expressing proper {@link GeneratedType} archetype.
 */
// FIXME: Remove this interface when we eliminate GeneratedTransferObject and direct GeneratedType builders
@Beta
@NonNullByDefault
@Value.Style(
    depluralize = true,
    strictBuilder = true,
    visibility = ImplementationVisibility.PRIVATE,
    packageGenerated = "*.impl.archetype",
    allowedClasspathAnnotations = { SuppressWarnings.class, Generated.class })
public sealed interface Archetype extends Type {

    JavaTypeName typeName();

    @Override
    @Deprecated(forRemoval = true)
    default JavaTypeName getIdentifier() {
        return typeName();
    }

    /**
     * Return the underlying {@link JavaConstruct} type.
     *
     * @return the underlying {@link JavaConstruct} type
     */
    // FIXME: this really looks like a java-api-generator concern and we should not carry it here
    Class<? extends JavaConstruct> construct();

    /**
     * The archetype of an interface generated for an {@code action}.
     */
    non-sealed interface Action extends Archetype, WithStatement {
        @Override
        default Class<JavaConstruct.Interface> construct() {
            return JavaConstruct.Interface.class;
        }

        @Override
        ActionEffectiveStatement statement();

        JavaTypeName inputName();

        JavaTypeName outputName();
    }

    non-sealed interface Annotation extends Archetype, WithStatement {
        @Override
        default Class<JavaConstruct.Interface> construct() {
            return JavaConstruct.Interface.class;
        }

        @Override
        AnnotationEffectiveStatement statement();
    }

    /**
     * The archetype of an builder class generated particular {@link #target()}.
     */
    non-sealed interface Builder extends Archetype {
        @Override
        default Class<JavaConstruct.Class> construct() {
            return JavaConstruct.Class.class;
        }

        /**
         * Return target {@link InterfaceArchetype}.
         *
         * @return target {@link InterfaceArchetype}.
         */
        DataObject target();
    }

    non-sealed interface Choice extends Archetype, WithStatement {
        @Override
        default Class<JavaConstruct.Interface> construct() {
            return JavaConstruct.Interface.class;
        }

        @Override
        ChoiceEffectiveStatement statement();

        JavaTypeName parentName();
    }

    sealed interface DataObject extends Archetype, WithStatement, WithFields<DataField<?>> {
        @Override
        default Class<JavaConstruct.Interface> construct() {
            return JavaConstruct.Interface.class;
        }

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

        non-sealed interface KeyAware extends DataObject {
            @Override
            ListEffectiveStatement statement();

            Key key();
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
     * The archetype of a class generated particular {@link FeatureEffectiveStatement}.
     */
    non-sealed interface Feature extends Archetype, WithStatement {
        @Override
        default Class<JavaConstruct.Class> construct() {
            return JavaConstruct.Class.class;
        }

        @Override
        FeatureEffectiveStatement statement();
    }

    non-sealed interface Grouping extends Archetype, WithStatement, WithFields<DataField<?>> {
        @Override
        default Class<JavaConstruct.Interface> construct() {
            return JavaConstruct.Interface.class;
        }

        @Override
        GroupingEffectiveStatement statement();
    }

    @DoNotMock
    @Value.Immutable
    non-sealed interface Key extends Archetype, WithStatement, WithFields<DataField<?>> {
        @Override
        // FIXME: JavaConstruct.Record
        default Class<JavaConstruct.Class> construct() {
            return JavaConstruct.Class.class;
        }

        @Override
        KeyEffectiveStatement statement();

        JavaTypeName keyAwareName();

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
        default Class<JavaConstruct.Interface> construct() {
            return JavaConstruct.Interface.class;
        }

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
        default Class<JavaConstruct.Interface> construct() {
            return JavaConstruct.Interface.class;
        }

        @Override
        RpcEffectiveStatement statement();

        JavaTypeName inputName();

        JavaTypeName outputName();
    }

    sealed interface TypeObject extends Archetype, WithStatement {

        TypeEffectiveStatement<?> effectiveType();

        /**
         * The archetype of an enum generated for a particular {@code type bits} statement statement.
         */
        non-sealed interface Bits extends TypeObject, WithFields<Bits.Field> {
            @Override
            default Class<JavaConstruct.Class> construct() {
                return JavaConstruct.Class.class;
            }

            // corresponds to
            //   statement().streamEffectiveSubstatements(BitEffectiveStatement.class).map( create Field )
            @Override
            List<Field> fields();

            record Field(BitEffectiveStatement statement, String name) implements Archetype.Field {
                private static final Type TYPE = Type.of(boolean.class);

                public Field {
                    requireNonNull(statement);
                    requireNonNull(name);
                }

                @Override
                public Type type() {
                    return TYPE;
                }
            }
        }

        /**
         * The archetype of an enum generated for a particular {@code type enumeration} statement.
         */
        non-sealed interface Enum extends TypeObject {
            @Override
            default Class<JavaConstruct.Enum> construct() {
                return JavaConstruct.Enum.class;
            }

            // corresponds to
            //   statement().streamEffectiveSubstatements(EnumEffectiveStatement.class).map( create Constant )
            List<Constant> constants();

            // FIXME: can we pick 'value' from stmt?
            record Constant(EnumEffectiveStatement stmt, String javaName, int value) {
                public Constant {
                    requireNonNull(stmt);
                    // Note: corresponds to JavaTypeName.simpleName()
                    requireNonNull(javaName);
                }
            }
        }

        /**
         * The archetype of an class generated for a particular {@code type} statement holding a single value.
         */
        non-sealed interface Scalar extends TypeObject {
            @Override
            default Class<JavaConstruct.Class> construct() {
                return JavaConstruct.Class.class;
            }
        }

        /**
         * The archetype of an class generated for a particular {@code type union} statement.
         */
        non-sealed interface Union extends TypeObject, WithFields {
            @Override
            default Class<JavaConstruct.Class> construct() {
                return JavaConstruct.Class.class;
            }

            // corresponds to statement().collectEffectiveSubstatements(TypeEffectiveStatement.class)
            @Override
            List<Field> fields();

            // corresponds to
            //  statement().streamEffectiveSubstatements(TypeEffectiveStatement.class)
            //    .filter( needs to have a nested classes generated )
            default List<TypeObject> generatedTypes() {
                return fields().stream()
                    .map(Field::type)
                    .filter(TypeObject.class::isInstance).map(TypeObject.class::cast)
                    .collect(Collectors.toList());
            }

            record Field(TypeEffectiveStatement<?> statement, String name, Type type) implements Archetype.Field {
                public Field {
                    requireNonNull(statement);
                    requireNonNull(name);
                    requireNonNull(type);
                }
            }
        }
    }

    /**
     * An object which has an associated {@link EffectiveStatement}.
     */
    sealed interface WithStatement {
        /**
         * Return associated {@link EffectiveStatement}.
         *
         * @return associated {@link EffectiveStatement}
         */
        EffectiveStatement<?, ?> statement();
    }

    sealed interface WithFields<T extends Field> {

        List<T> fields();
    }

    sealed interface Field {
        /**
         * Return associated {@link EffectiveStatement}.
         *
         * @return associated {@link EffectiveStatement}
         */
        EffectiveStatement<?, ?> statement();

        String name();

        Type type();
    }

    record DataField<S extends DataTreeEffectiveStatement<?>>(S statement, String name, Type type) implements Field {
        public DataField {
            requireNonNull(statement);
            requireNonNull(name);
            requireNonNull(type);
        }
    }
}
