/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;

/**
 * Generator corresponding to a {@code augment} statement used as a child of a {@code uses} statement.
 */
final class UsesAugmentGenerator extends AbstractAugmentGenerator {
    private final UsesEffectiveStatement uses;

    private GroupingGenerator grouping;

    UsesAugmentGenerator(final AugmentEffectiveStatement statement, final AbstractCompositeGenerator<?> parent,
            final UsesEffectiveStatement uses) {
        super(statement, parent);
        this.uses = requireNonNull(uses);
    }

    void linkGroupingDependency(final UsesEffectiveStatement checkUses, final GroupingGenerator resolvedGrouping) {
        if (uses.equals(checkUses)) {
            verify(grouping == null, "Attempted to relink %s from %s to %s", this, grouping, resolvedGrouping);
            grouping = requireNonNull(resolvedGrouping);
        }
    }

    @Override
    void loadTargetGenerator() {
        final GroupingGenerator grp = verifyNotNull(grouping, "No grouping linked in %s", this);

        /*
         *  Here we are going in the opposite direction of RFC7950, section 7.13:
         *
         *        The effect of a "uses" reference to a grouping is that the nodes
         *        defined by the grouping are copied into the current schema tree and
         *        are then updated according to the "refine" and "augment" statements.
         *
         *  Our argument is composed of QNames in the current schema tree's namespace, but the grouping may have been
         *  defined in a different module -- and therefore it knows those children under its namespace. Adjust the path
         *  we are searching if that happens to be the case.
         */
        QNameModule namespace = grp.statement().argument().getModule();
        AbstractExplicitGenerator<?> current = grp;

        for (QName next : statement().argument().getNodeIdentifiers()) {
            final QName qname = next.bindTo(namespace);
            current = verifyNotNull(current.findSchemaTreeGenerator(qname),
                "Failed to find %s as %s in %s", next, qname, current);

            // Returned generator's namespace might be different from we used as the lookup. This happens when the
            // generator is inherited from a grouping -- see AbstractCompositeGenerator.findInferredGenerator().
            namespace = current.getQName().getModule();
        }

        setTargetGenerator(current);
    }
}
