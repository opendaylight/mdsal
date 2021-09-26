/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.osgi;

import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
import org.w3c.dom.Element;

// FIXME: should be a record with JDK17+
final class BundleField implements Immutable {
    final String className;
    final String fieldName;
    final String typeName;

    BundleField(final Element field) {
        typeName = field.getAttribute("type");
        className = field.getAttribute("package") + "." + field.getAttribute("class");
        fieldName = field.getAttribute("name");
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName, className, fieldName);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BundleField)) {
            return false;
        }
        final var other = (BundleField) obj;
        return typeName.equals(other.typeName) && className.equals(other.className)
            && fieldName.equals(other.fieldName);
    }
}
