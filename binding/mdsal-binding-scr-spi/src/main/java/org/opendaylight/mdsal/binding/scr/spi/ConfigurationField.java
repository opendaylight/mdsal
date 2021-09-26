/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

// FIXME: should be a record with JDK17+
// FIXME: should this also provide a 'Supplier<T> toSupplierIn(ClassLoader)' ?
public final class ConfigurationField implements Immutable {
    public final @NonNull String typeName;
    public final @NonNull String className;
    public final @NonNull String fieldName;

    private ConfigurationField(final String typeName, final String className, final String fieldName) {
        this.typeName = requireNonNull(typeName);
        this.className = requireNonNull(className);
        this.fieldName = requireNonNull(fieldName);
    }

    public static @NonNull ConfigurationField of(final String typeName, final String className,
            final String fieldName) {
        return new ConfigurationField(typeName, className, fieldName);
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
        if (!(obj instanceof ConfigurationField)) {
            return false;
        }
        final var other = (ConfigurationField) obj;
        return typeName.equals(other.typeName) && className.equals(other.className)
            && fieldName.equals(other.fieldName);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", typeName).add("class", className).add("field", fieldName)
            .toString();
    }
}
