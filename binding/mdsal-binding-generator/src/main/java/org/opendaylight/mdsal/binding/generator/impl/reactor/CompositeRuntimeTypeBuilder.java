/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

abstract class CompositeRuntimeTypeBuilder<S extends EffectiveStatement<?, ?>, R extends CompositeRuntimeType> {
    private final List<AugmentRuntimeType> augmentTypes = new ArrayList<>();
    private final List<RuntimeType> childTypes = new ArrayList<>();
    private final @NonNull S statement;

    CompositeRuntimeTypeBuilder(final S statement) {
        this.statement = requireNonNull(statement);
    }

    final @NonNull S statement() {
        return statement;
    }

    final @NonNull List<CaseRuntimeType> getCaseChilden() {
        return childTypes.stream()
            .map(child -> {
                verify(child instanceof CaseRuntimeType, "Unexpected child %s in %s", child, statement);
                return (CaseRuntimeType) child;
            })
            .collect(Collectors.toUnmodifiableList());
    }

    final @NonNull R buildExternal(final @NonNull GeneratedType generatedType,
            final @NonNull AbstractCompositeGenerator<S, R> generator) {
        final var referencingAugments = generator.augments().stream()
            .map(AbstractAugmentGenerator::getInternalRuntimeType)
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList());
        return referencingAugments.isEmpty() ? build(generatedType, statement, childTypes, augmentTypes)
            : build(generatedType, statement, childTypes, augmentTypes, referencingAugments);
    }

    final @NonNull R buildInternal(final @NonNull GeneratedType generatedType) {
        return build(generatedType, statement, childTypes, augmentTypes);
    }

    abstract @NonNull R build(GeneratedType type, S statement, List<RuntimeType> children,
        List<AugmentRuntimeType> augments);

    abstract @NonNull R build(GeneratedType type, S statement, List<RuntimeType> children,
        List<AugmentRuntimeType> augments, ImmutableList<AugmentRuntimeType> referencingAugments);

    CompositeRuntimeTypeBuilder<S, R> fillTypes(final ChildLookup lookup,
            final AbstractCompositeGenerator<S, R> generator) {
        // Figure out which augments are valid in target statement and record their RuntimeTypes.
        // We will pass the latter to create method. We will use the former to perform replacement lookups instead
        // of 'this.augments'. That is necessary because 'this.augments' holds all augments targeting the GeneratedType,
        // hence equivalent augmentations from differing places would match our lookup and the reverse search would be
        // lead astray.
        //
        // Augments targeting 'choice' statement are handled by a separate class and need to be skipped here
        if (!(generator instanceof ChoiceGenerator)) {
            for (var augment : generator.augments()) {
                final var augmentRuntimeType = augment.runtimeTypeIn(lookup, statement);
                if (augmentRuntimeType != null) {
                    augmentTypes.add(augmentRuntimeType);
                }
            }
        }

        // Now construct RuntimeTypes for each schema tree child of stmt
        for (var stmt : statement.effectiveSubstatements()) {
            if (stmt instanceof SchemaTreeEffectiveStatement) {
                final var child = (SchemaTreeEffectiveStatement<?>) stmt;
                final var qname = child.getIdentifier();

                // Try valid augments first: they should be empty most of the time and filter all the cases where we
                // would not find the streamChild among our local and grouping statements. Note that unlike all others,
                // such matches are not considered to be children in Binding DataObject tree, they are only considered
                // such in the schema tree.
                if (isAugmentedChild(lookup, qname)) {
                    continue;
                }

                final var childRuntimeType = findChildRuntimeType(lookup, generator, child);
                if (childRuntimeType != null) {
                    childTypes.add(childRuntimeType);
                }
            }
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    final <X extends SchemaTreeEffectiveStatement<?>> @Nullable RuntimeType findChildRuntimeType(
            final @NonNull ChildLookup lookup, final AbstractCompositeGenerator<?, ?> parent, final @NonNull X stmt) {
        final var qname = stmt.getIdentifier();
        // First try our local items without adjustment ...
        @SuppressWarnings("rawtypes")
        AbstractExplicitGenerator childGen = findChild(parent, qname);
        if (childGen == null) {
            // No luck, let's see if any of the groupings can find it
            for (GroupingGenerator grouping : parent.groupings()) {
                final var gen = grouping.findSchemaTreeGenerator(
                    qname.bindTo(grouping.statement().argument().getModule()));
                if (gen != null) {
                    return findChildRuntimeType(lookup.inGrouping(qname, grouping), grouping, stmt);
                }
            }

            // Finally attempt to find adjusted QName: this has to succeed
            final var adjusted = lookup.adjustQName(qname);
            childGen = verifyNotNull(findChild(parent, adjusted),
                "Failed to find %s as %s in %s", stmt, adjusted, this);
        }

        return childGen.createInternalRuntimeType(lookup, stmt);
    }

    boolean isAugmentedChild(final ChildLookup lookup, final QName qname) {
        // Note we are dealing with two different kinds of augments and they behave differently with respect
        // to namespaces. Top-level augments do not make an adjustment, while uses-augments do.
        for (var augment : augmentTypes) {
            if (augment.schemaTreeChild(qname) != null) {
                return true;
            }

//            // FIXME: is probably not right
//            final var arg = augment.statement().argument();
//            if (arg instanceof SchemaNodeIdentifier.Descendant) {
//                final var adjusted = lookup.adjustQName(qname);
//                if (augment.schemaTreeChild(adjusted) != null) {
//                    return true;
//                }
//            }
        }
        return false;
    }

    private static @Nullable AbstractExplicitGenerator<?, ?> findChild(final AbstractCompositeGenerator<?, ?> parent,
            final QName qname) {
        for (var child : parent) {
            if (child instanceof AbstractExplicitGenerator) {
                final AbstractExplicitGenerator<?, ?> gen = (AbstractExplicitGenerator<?, ?>) child;
                final EffectiveStatement<?, ?> stmt = gen.statement();
                if (stmt instanceof SchemaTreeEffectiveStatement && qname.equals(stmt.argument())) {
                    return gen;
                }
            }
        }
        return null;
    }
}
