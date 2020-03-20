/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.CheckedBuilder;

/**
 * A specialization of {@link CheckedBuilder}, whose {@code build()} method throws a {@link QueryStructureException}.
 *
 * @param <P> Product of builder
 */
@Beta
public interface StructuralBuilder<P> extends CheckedBuilder<P, QueryStructureException> {

}
