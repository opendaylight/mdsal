/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for {@link TypedefGenerator} and {@link AbstractTypeAwareGenerator}. This encompasses three
 * different statements with two different semantics:
 * <ul>
 *   <li>{@link TypedefGenerator}s always result in a generated {@link TypeObject}, even if the semantics is exactly
 *       the same as its base type. This aligns with {@code typedef} defining a new type.<li>
 *   <li>{@link LeafGenerator}s and {@link LeafListGenerator}s, on the other hand, do not generate a {@link TypeObject}
 *       unless absolutely necassary. This aligns with {@code leaf} and {@code leaf-list} being mapped onto a property
 *       of its parent type.<li>
 * </ul>
 *
 * <p>
 * To throw a bit of confusion into the mix, there are two exceptions to those rules:
 * <ul>
 *   <li>
 *     {@code identityref} definitions never result in a type definition being emitted. The reason for this has to do
 *     with identity type mapping as well as history of our codebase.
 *
 *     <p>
 *     The problem at hand is inconsistency between the fact that identity is mapped to a {@link Class}, which is also
 *     returned from leaves which specify it like this:
 *     <pre>
 *       <code>
 *         identity iden;
 *
 *         container foo {
 *           leaf foo {
 *             type identityref {
 *               base iden;
 *             }
 *           }
 *         }
 *       </code>
 *     </pre>
 *     which results in fine-looking
 *     <pre>
 *       <code>
 *         interface Foo {
 *           Class&lt;? extends Iden&gt; getFoo();
 *         }
 *       </code>
 *     </pre>
 *
 *     <p>
 *     This gets more dicey if we decide to extend the previous snippet to also include:
 *     <pre>
 *       <code>
 *         typedef bar-ref {
 *           type identityref {
 *             base iden;
 *           }
 *         }
 *
 *         container bar {
 *           leaf bar {
 *             type bar-ref;
 *           }
 *         }
 *       </code>
 *     </pre>
 *
 *     <p>
 *     Now we have competing requirements: {@code typedef} would like us to use encapsulation to capture the defined
 *     type, while {@code getBar()} wants us to retain shape with getFoo(), as it should not matter how the
 *     {@code identityref} was formed. We need to pick between:
 *     <ol>
 *       <li>
 *         <pre>
 *           <code>
 *             public class BarRef extends ScalarTypeObject&lt;Class&lt;? extends Iden&gt;&gt; {
 *               Class&lt;? extends Iden&gt; getValue() {
 *                 // ...
 *               }
 *             }
 *
 *             interface Bar {
 *               BarRef getBar();
 *             }
 *           </code>
 *         </pre>
 *       </li>
 *       <li>
 *         <pre>
 *           <code>
 *             interface Bar {
 *               Class&lt;? extends Iden&gt; getBar();
 *             }
 *           </code>
 *         </pre>
 *       </li>
 *     </ol>
 *
 *     <p>
 *     Here the second option is more user-friendly, as the type system works along the lines of <b>reference</b>
 *     semantics, treating and {@code Bar.getBar()} and {@code Foo.getFoo()} as equivalent. The first option would
 *     force users to go through explicit encapsulation, for no added benefit as the {@code typedef} cannot possibly add
 *     anything useful to the actual type semantics.
 *   </li>
 *   <li>
 *     {@code leafref} definitions never result in a type definition being emitted. The reasons for this are similar to
 *     {@code identityref}, but have an additional twist: a {@leafref} can target a relative path, which may only be
 *     resolved at a particular instantiation.
 *
 *     <p>
 *     Take the example of the following model:
 *     <pre>
 *       <code>
 *         grouping grp {
 *           typedef ref {
 *             type leafref {
 *               path ../xyzzy;
 *             }
 *           }
 *
 *           leaf foo {
 *             type ref;
 *           }
 *         }
 *
 *         container bar {
             uses grp;
 *
 *           leaf xyzzy {
 *             type string;
 *           }
 *         }
 *
 *         container baz {
 *           uses grp;
 *
 *           leaf xyzzy {
 *             type int32;
 *           }
 *         }
 *       </code>
 *     </pre>
 *
 *     <p>
 *     The {@code typedef ref} points to outside of the grouping, and hence the type of {@code leaf foo} is polymorphic:
 *     the definition in {@code grouping grp} needs to use {@code Object}, whereas the instantiations in
 *     {@code container bar} and {@code container baz} need to use {@code String} and {@link Integer} respectively.
 *     Expressing the resulting interface contracts requires return type specialization and run-time checks. An
 *     intermediate class generated for the typedef would end up being a hindrance without any benefit.
 *   <li>
 * </ul>
 *
 * <p>
 * At the end of the day, the mechanic translation rules are giving way to correctly mapping the semantics -- which in
 * both of the exception cases boil down to tracking type indirection. Intermediate constructs involved in tracking
 * type indirection in YANG constructs is therefore explicitly excluded from the generated Java code, but the Binding
 * Specification still takes them into account when determining types as outlined above.
 */
abstract class AbstractTypeObjectGenerator<T extends EffectiveStatement<?, ?>> extends AbstractDependentGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTypeObjectGenerator.class);

    private final TypeEffectiveStatement<?> type;

    private TypedefGenerator baseGen;
    private TypeReference refType;

    AbstractTypeObjectGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        type = statement().findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
    }

    @Override
    final void linkDependencies(final GeneratorContext context) {
        final QName typeName = type.argument();
        if (!YangConstants.RFC6020_YANG_MODULE.equals(typeName.getModule())) {
            baseGen = context.resolveTypedef(typeName);
            baseGen.addDerivedGenerator(this);
        }
    }

    final void bindTypeDefinition(final GeneratorContext context) {
        if (baseGen != null) {
            // We have registered with baseGen, it will push the type to us
            return;
        }

        if (TypeDefinitions.LEAFREF.equals(type.argument())) {
            refType = TypeReference.leafRef(context.resolveLeafref(
                type.findFirstEffectiveSubstatementArgument(PathEffectiveStatement.class).orElseThrow()));
        } else if (TypeDefinitions.IDENTITYREF.equals(type.argument())) {
            refType = TypeReference.identityRef(type.streamEffectiveSubstatements(BaseEffectiveStatement.class)
                .map(BaseEffectiveStatement::argument)
                .map(context::resolveIdentity)
                .collect(Collectors.toUnmodifiableList()));
        }
        LOG.info("Resolved base {} to generator {}", type, refType);
        bindDerivedGenerators(refType);
    }

    final void bindTypeDefinition(final @Nullable TypeReference reference) {
        refType = reference;
        LOG.info("Resolved derived {} to generator {}", type, refType);
    }

    abstract void bindDerivedGenerators(@Nullable TypeReference reference);

    @Override
    boolean producesType() {
        return refType == null;
    }

    @Override
    final GeneratedTransferObject createTypeImpl(final TypeBuilderFactory builderFactory) {
        return baseGen == null ? createRootType(builderFactory) :
            createDerivedType(builderFactory, baseGen.createTypeImpl(builderFactory));
    }

    abstract @NonNull GeneratedTransferObject createRootType(@NonNull TypeBuilderFactory builderFactory);

    abstract @NonNull GeneratedTransferObject createDerivedType(@NonNull TypeBuilderFactory builderFactory,
        @NonNull GeneratedTransferObject baseType);
}
