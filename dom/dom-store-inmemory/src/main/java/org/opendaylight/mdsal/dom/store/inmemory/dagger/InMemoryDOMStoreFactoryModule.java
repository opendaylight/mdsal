/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.dagger;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMStoreFactory;
import org.opendaylight.mdsal.dom.store.inmemory.impl.DefaultInMemoryDOMStoreFactory;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeFactory;

/**
 * Module providing reference {@link InMemoryDOMStoreFactory}.
 *
 * @since 17.0.0
 */
@Module
@NonNullByDefault
@SuppressWarnings("exports")
public interface InMemoryDOMStoreFactoryModule {
    @Provides
    @Singleton
    static InMemoryDOMStoreFactory provideInMemoryDOMStoreFactory(final DataTreeFactory dataTreeFactory) {
        return new DefaultInMemoryDOMStoreFactory(dataTreeFactory);
    }
}
