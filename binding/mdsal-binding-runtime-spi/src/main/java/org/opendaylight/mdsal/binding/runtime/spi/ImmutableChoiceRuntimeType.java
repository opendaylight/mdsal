/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;

@Beta
public final class ImmutableChoiceRuntimeType extends AbstractCompositeRuntimeType implements ChoiceRuntimeType {
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

}
