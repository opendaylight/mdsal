/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;

@Beta
public final class ImmutableChoiceRuntimeType extends AbstractCompositeRuntimeType<ChoiceEffectiveStatement>
        implements ChoiceRuntimeType {
    public ImmutableChoiceRuntimeType(final GeneratedType bindingType, final ChoiceEffectiveStatement schema,
            final Map<RuntimeType<?, ?>, EffectiveStatement<?, ?>> children) {
        super(bindingType, schema, children);
    }

    @Override
    public CaseRuntimeType bindingCaseChild(final JavaTypeName typeName) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
//        final DataSchemaNode origSchema = getSchemaDefinition(childClass);
//        checkArgument(origSchema instanceof CaseSchemaNode, "Supplied schema %s is not case.", origSchema);
//
//        /* FIXME: Make sure that if there are multiple augmentations of same
//         * named case, with same structure we treat it as equals
//         * this is due property of Binding specification and copy builders
//         * that user may be unaware that he is using incorrect case
//         * which was generated for choice inside grouping.
//         */
//        return findInstantiatedCase(schema, (CaseSchemaNode) origSchema);
    }

//    private static @NonNull Optional<CaseSchemaNode> findInstantiatedCase(final ChoiceSchemaNode instantiatedChoice,
//            final CaseSchemaNode originalDefinition) {
//        CaseSchemaNode potential = instantiatedChoice.findCase(originalDefinition.getQName()).orElse(null);
//        if (originalDefinition.equals(potential)) {
//            return Optional.of(potential);
//        }
//        if (potential != null) {
//            SchemaNode potentialRoot = originalNodeOf(potential);
//            if (originalDefinition.equals(potentialRoot)) {
//                return Optional.of(potential);
//            }
//        }
//
//        // We try to find case by name, then lookup its root definition
//        // and compare it with original definition
//        // This solves case, if choice was inside grouping
//        // which was used in different module and thus namespaces are
//        // different, but local names are still same.
//        //
//        // Still we need to check equality of definition, because local name is not
//        // sufficient to uniquelly determine equality of cases
//        //
//        for (CaseSchemaNode caze : instantiatedChoice.findCaseNodes(originalDefinition.getQName().getLocalName())) {
//            if (originalDefinition.equals(originalNodeOf(caze))) {
//                return Optional.of(caze);
//            }
//        }
//        return Optional.empty();
//    }
}
