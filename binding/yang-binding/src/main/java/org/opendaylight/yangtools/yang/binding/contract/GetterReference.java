/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.contract;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A reference to a DTO getter, captured as a lambda expression. This reference forms a part of the addressing space
 * implied by YANG's instance-identifier and therefore is an {@link Serializable} to fulfill part of the Identifier
 * interface. Unfortunately we cannot reasonably implement the entire Identifier, as that also implies {@code hashCode},
 * {@code equals} and {@code toString} -- defeating the idea of a functional interface.
 *
 * <p>
 * Each instance of this interface is expected to be a lambda expression, which is an important factor in run-time
 * reflection.
 *
 * @param <P> Parent type
 * @param <C> Child type
 */
public sealed interface GetterReference<P, C> extends Serializable permits ObjectGetterReference, RootGetterReference {

    C extractFrom(@NonNull P parent);
}
