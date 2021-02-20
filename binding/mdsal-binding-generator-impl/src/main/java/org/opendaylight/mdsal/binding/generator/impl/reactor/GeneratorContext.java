/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Abstract view on generation tree as viewed by a particular {@link Generator}.
 */
abstract class GeneratorContext {
    /**
     * Resolve a tree-scoped namespace reference. This covers {@code typedef} and {@code grouping} statements, as per
     * bullets 5 and 6 of <a href="https://tools.ietf.org/html/rfc6020#section-6.2.1">RFC6020, section 6.2.1</a>.
     *
     * @param <E> {@link EffectiveStatement} type
     * @param type EffectiveStatement class
     * @param argument Statement argument
     * @return Resolved {@link Generator}
     * @throws NullPointerException if any argument is null
     * @throws IllegalStateException if the generator cannot be found
     */
    abstract <E extends EffectiveStatement<QName, ?>, G extends Generator<E>> @NonNull G resolveTreeScoped(
        @NonNull Class<G> type, @NonNull QName argument);

    final @NonNull TypedefGenerator resolveTypedef(final @NonNull QName qname) {
        return resolveTreeScoped(TypedefGenerator.class, qname);
    }
}
