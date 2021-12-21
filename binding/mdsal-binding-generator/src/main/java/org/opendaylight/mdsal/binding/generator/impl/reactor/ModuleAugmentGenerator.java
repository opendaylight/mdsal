/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verifyNotNull;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

/**
 * Generator corresponding to a {@code augment} statement used as a child of a {@code module} statement.
 */
final class ModuleAugmentGenerator extends AbstractAugmentGenerator {
    ModuleAugmentGenerator(final AugmentEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void loadTargetGenerator() {
        throw new UnsupportedOperationException();
    }

    void linkAugmentationTarget(final GeneratorContext context) {
        final SchemaNodeIdentifier path = statement().argument();
        final ModuleGenerator module = context.resolveModule(path.firstNodeIdentifier().getModule());

        // This is not quite straightforward. 'path' works on top of schema tree, which is instantiated view. Since we
        // do not generate duplicate instantiations along 'uses' path, findSchemaTreeGenerator() will satisfy our
        // request by returning a child of the source 'grouping'.
        //
        // When that happens, our subsequent lookups need to adjust the namespace being looked up to the grouping's
        // namespace... except for the case when the step is actually an augmentation, in which case we must not make
        // that adjustment.
        AbstractExplicitGenerator<?> target = module;
        QNameModule instNamespace = null;
        QNameModule grpNamespace = null;

        for (QName qname : path.getNodeIdentifiers()) {
            final QName next = qname.getModule().equals(instNamespace) ? qname.bindTo(verifyNotNull(grpNamespace))
                : qname;
            final AbstractExplicitGenerator<?> found = verifyNotNull(target.findSchemaTreeGenerator(qname),
                "Failed to find %s as %s in %s", qname, next, target);

            final QNameModule foundNamespace = found.getQName().getModule();
            if (!foundNamespace.equals(next.getModule())) {
                instNamespace = qname.getModule();
                grpNamespace = foundNamespace;
            }

            target = found;
        }

        setTargetGenerator(target);
    }
}
