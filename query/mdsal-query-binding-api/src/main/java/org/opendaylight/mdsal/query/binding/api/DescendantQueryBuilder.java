/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;

@Beta
public interface DescendantQueryBuilder<T extends DataObject> extends StructuralBuilder<DescendantQuery<T>> {

    <C extends ChildOf<? super T>> @NonNull DescendantQueryBuilder<C> extractChild(Class<C> childClass);

    @NonNull MatchBuilderPath<T, T> matching();
}
