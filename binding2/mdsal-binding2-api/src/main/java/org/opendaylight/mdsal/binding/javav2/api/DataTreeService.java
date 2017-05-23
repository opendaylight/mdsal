/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

@Beta
public interface DataTreeService extends DataTreeProducerFactory, BindingService {

    @Nonnull
    <T extends DataTreeListener> ListenerRegistration<T> registerListener(@Nonnull T listener,
        @Nonnull Collection<DataTreeIdentifier<?>> subtrees, boolean allowRxMerges,
        @Nonnull Collection<DataTreeProducer> producers) throws DataTreeLoopException;
}
