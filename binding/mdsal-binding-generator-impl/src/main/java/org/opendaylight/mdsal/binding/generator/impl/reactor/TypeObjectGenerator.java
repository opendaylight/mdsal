/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;


final class TypeObjectGenerator {
    /**
     * The generator corresponding to our YANG base type. It produces the superclass of our encapsulated type. If it is
     * {@code null}, this generator is the root of the hierarchy.
     */
    private final TypedefGenerator baseGenerator = null;

    /**
     * The generator corresponding to the type we are referencing. It can be one of these values:
     * <ul>
     *   <li>{@code this}, if this is a normal type</li>
     *   <li>{@code null}, if this is a {@code leafref} inside a {@code grouping} and the referenced type cannot be
     *       resolved because the reference points outside of the grouping.</li>
     *   <li>the referenced YANG type's generator</li>
     * </ul>
     */
    private final AbstractTypeObjectGenerator<?> referencedGenerator = null;




}
