/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.osgi.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.util.concurrent.CheckedFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.osgi.YangTextAwareBindingRuntimeContext;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

final class AtomicYangTextAwareBindingRuntimeContext implements YangTextAwareBindingRuntimeContext {
    private final AtomicReference<BindingRuntimeContext> ref = new AtomicReference<>();
    private final SchemaSourceProvider<YangTextSchemaSource> sourceProvider;
    private final ClassLoadingStrategy strategy;

    AtomicYangTextAwareBindingRuntimeContext(final ClassLoadingStrategy strategy,
        final SchemaSourceProvider<YangTextSchemaSource> sourceProvider) {
        this.sourceProvider = checkNotNull(sourceProvider);
        this.strategy = checkNotNull(strategy);
    }

    @Override
    public CheckedFuture<? extends YangTextSchemaSource, SchemaSourceException> getSource(
            final SourceIdentifier sourceIdentifier) {
        return sourceProvider.getSource(sourceIdentifier);
    }

    @Override
    public BindingRuntimeContext getBindingRuntimeContext() {
        final BindingRuntimeContext ret = ref.get();
        checkState(ret != null, "Runtime context is not initialized yet");
        return ret;
    }

    void updateBindingRuntimeContext(final SchemaContext schemaContext) {
        ref.set(verifyNotNull(BindingRuntimeContext.create(strategy, schemaContext)));
    }
}
