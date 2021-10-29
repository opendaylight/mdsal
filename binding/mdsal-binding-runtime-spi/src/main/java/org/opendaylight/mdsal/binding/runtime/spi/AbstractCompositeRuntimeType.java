/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeTypeContainer;
import org.opendaylight.yangtools.yang.common.QName;

abstract class AbstractCompositeRuntimeType extends AbstractRuntimeType implements RuntimeTypeContainer {
    AbstractCompositeRuntimeType(final GeneratedType bindingType) {
        super(bindingType);
    }

    @Override
    public final RuntimeType schemaTreeChild(final QName qname) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public final RuntimeType bindingChild(final JavaTypeName typeName) {
        // FIXME: implement this
        throw new UnsupportedOperationException();

//        @Override
//        public final DataSchemaNode findChildSchemaDefinition(final DataNodeContainer parentSchema,
//                final QNameModule parentNamespace, final Class<?> childClass) {
//            final DataSchemaNode origDef = getSchemaDefinition(childClass);
//            if (origDef == null) {
//                // Weird, the child does not have an associated definition
//                return null;
//            }
//
//            // Direct instantiation or use in same module in which grouping was defined.
//            final QName origName = origDef.getQName();
//            final DataSchemaNode sameName = parentSchema.dataChildByName(origName);
//            if (sameName != null) {
//                // Check if it is:
//                // - exactly same schema node, or
//                // - instantiated node was added via uses statement and is instantiation of same grouping
//                if (origDef.equals(sameName) || origDef.equals(getRootOriginalIfPossible(sameName))) {
//                    return sameName;
//                }
//
//                // Node has same name, but clearly is different
//                return null;
//            }
//
//            // We are looking for instantiation via uses in other module
//            final DataSchemaNode potential = parentSchema.dataChildByName(origName.bindTo(parentNamespace));
//            // We check if it is really instantiated from same definition as class was derived
//            if (potential != null && origDef.equals(getRootOriginalIfPossible(potential))) {
//                return potential;
//            }
//            return null;
//        }
    }

//    private static @Nullable SchemaNode getRootOriginalIfPossible(final SchemaNode data) {
//        SchemaNode previous = null;
//        SchemaNode next = originalNodeOf(data);
//        while (next != null) {
//            previous = next;
//            next = originalNodeOf(next);
//        }
//        return previous;
//    }
}
