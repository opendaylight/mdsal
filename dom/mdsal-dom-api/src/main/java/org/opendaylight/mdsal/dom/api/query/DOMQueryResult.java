/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.query;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * An object holding the results of a {@link DOMQuery} execution.
 */
@Beta
@NonNullByDefault
public interface DOMQueryResult extends Immutable {

    Spliterator<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> spliterator();

    Stream<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> stream();

    Stream<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> parallelStream();

    default List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items() {
        return stream().collect(Collectors.toUnmodifiableList());
    }
}
