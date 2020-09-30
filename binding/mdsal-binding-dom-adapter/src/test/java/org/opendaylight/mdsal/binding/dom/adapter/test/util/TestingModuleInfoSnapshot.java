/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test.util;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public final class TestingModuleInfoSnapshot implements ModuleInfoSnapshot {
    public static final TestingModuleInfoSnapshot INSTANCE = new TestingModuleInfoSnapshot();

    private TestingModuleInfoSnapshot() {
        // Hidden on purpose
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(SourceIdentifier sourceIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<T> loadClass(String fullyQualifiedName) throws ClassNotFoundException {
        return (Class<T>) ClassLoaderUtils.loadClassWithTCCL(fullyQualifiedName);
    }
}
