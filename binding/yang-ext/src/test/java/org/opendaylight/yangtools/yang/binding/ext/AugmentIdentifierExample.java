/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.ext;

import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;

class AugmentIdentifierExample {
    /**
     * A generated object, in this case {@code container foo}.
     */
    interface Foo extends DataObject, Augmentable<Foo> {
        @Override
        default Class<Foo> implementedInterface() {
            return Foo.class;
        }
    }

    /**
     * Having this annotation allows annotation processors see the faux-name used for eliciting the naming convention.
     * {@code ReplacementNodeIdentifier} is the product of multiple possible node-identifier values, such as:
     * <ul>
     *   <li>ReplacementClassName</li>
     *   <li>replacement-ClassName</li>
     *   <li>replacement-className</li>
     *   <li>replacement.ClassName</li>
     *   <li>...</li>
     * </ul>
     * This particular use documents this name comons from
     * <pre>(@code augment-identifier replacement-class-name"}</pre>
    */
    @AugmentIdentifier("replacement-class-name")
    interface ReplacementClassName extends Augmentation<Foo> {
        @Override
        default Class<ReplacementClassName> implementedInterface() {
            return ReplacementClassName.class;
        }
    }
}
