/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base;

import com.google.common.annotations.Beta;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 * Type capture of an entity producing NodeCodecContexts.
 */
@Beta
interface NodeContextSupplier extends Supplier<NodeCodecContext<?>> {

    @Override
    @Nonnull
    NodeCodecContext<?> get();
}