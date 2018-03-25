/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;

@NonNullByDefault
final class NestedJavaGeneratedType extends AbstractJavaGeneratedType {
    private final AbstractJavaGeneratedType enclosingType;

    NestedJavaGeneratedType(final AbstractJavaGeneratedType enclosingType, final GeneratedType genType) {
        super(genType);
        this.enclosingType = requireNonNull(enclosingType);
    }

    @Override
    String localTypeName(final JavaTypeName type) {
        final Optional<List<String>> optDescendant = findDescandantPath(type);
        if (optDescendant.isPresent()) {
            final Iterator<String> it = optDescendant.get().iterator();
            final StringBuilder sb = new StringBuilder();

            sb.append(it.next());
            while (it.hasNext()) {
                sb.append('.').append(it.next());
            }

            return sb.toString();
        }

        return enclosingType.getName().equals(type) ? enclosingType.getSimpleName() : enclosingType.localTypeName(type);
    }

    @Override
    boolean importCheckedType(final JavaTypeName type) {
        // Defer to enclosing type, which needs to re-run its checks
        return enclosingType.checkAndImportType(type);
    }

    private Optional<List<String>> findDescandantPath(final JavaTypeName type) {
        Optional<JavaTypeName> optEnclosing = type.immediatelyEnclosingClass();
        verify(optEnclosing.isPresent());

        final Deque<String> queue = new ArrayDeque<>();
        queue.addFirst(type.simpleName());
        while (optEnclosing.isPresent()) {
            final JavaTypeName enclosing = optEnclosing.get();
            if (enclosing.equals(getName())) {
                return Optional.of(ImmutableList.copyOf(queue));
            }

            queue.addFirst(enclosing.simpleName());
            optEnclosing = enclosing.immediatelyEnclosingClass();
        }

        return Optional.empty();
    }
}
