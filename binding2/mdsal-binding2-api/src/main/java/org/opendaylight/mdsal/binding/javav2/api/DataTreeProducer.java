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

@Beta
public interface DataTreeProducer extends DataTreeProducerFactory, AutoCloseable {

    @Nonnull
    CursorAwareWriteTransaction createTransaction(boolean isolated);

    @Override
    @Nonnull
    DataTreeProducer createProducer(@Nonnull Collection<DataTreeIdentifier<?>> subtrees);

    @Override
    void close() throws DataTreeProducerException;
}
