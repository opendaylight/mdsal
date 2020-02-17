/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * Wraps combination of <code>packageName</code> and <code>name</code> to the object representation.
 */
@Beta
public final class DefaultType extends AbstractBaseType {
    private DefaultType(final JavaTypeName identifier) {
        super(identifier);
    }

    public static @NonNull DefaultType of(final JavaTypeName identifier) {
        return new DefaultType(identifier);
    }

    public static @NonNull DefaultType of(final Identifiable<JavaTypeName> type) {
        return of(type.getIdentifier());
    }

    public static @NonNull Type of(final Class<?> type) {
        return of(JavaTypeName.create(type));
    }
}
