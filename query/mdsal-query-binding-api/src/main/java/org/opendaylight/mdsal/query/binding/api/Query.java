/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * A query handle. A query execution results in a {@link QueryResult}, which is composed of zero or more objects of the
 * same type. Implementations of this interface are expected to be effectively-immutable and therefore thread-safe and
 * reusable.
 *
 * @param <T> Result object type
 */
@Beta
public interface Query<T extends DataObject> extends Immutable {

}
