/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.processor;

import static java.util.Objects.requireNonNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Reference;

/**
 * A site of a {@link Reference} to a dataobject.
 */
@NonNullByDefault
final class ReferenceSite {
    final Element annotatedElement;
    final TypeElement referencedDataObject;

    ReferenceSite(final Element annotatedElement, final TypeElement referencedDataObject) {
        this.annotatedElement = requireNonNull(annotatedElement);
        this.referencedDataObject = requireNonNull(referencedDataObject);
    }

    @Override
    public String toString() {
        return "ReferenceSite[ " + annotatedElement + " needs " + referencedDataObject + "]";
    }
}
