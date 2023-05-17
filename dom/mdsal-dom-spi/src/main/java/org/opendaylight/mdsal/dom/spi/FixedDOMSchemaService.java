/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMSchemaServiceExtension;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.yangtools.concepts.NoOpListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * {@link DOMSchemaService} (and {@link DOMYangTextSourceProvider}) implementations backed by a
 * {@link EffectiveModelContextProvider} (and {@link SchemaSourceProvider}) which are known to be fixed and never change
 * schemas.
 *
 * @author Michael Vorburger.ch
 */
@Beta
public class FixedDOMSchemaService extends AbstractDOMSchemaService {
    private static final class WithYangTextSources extends FixedDOMSchemaService implements DOMYangTextSourceProvider {
        private final @NonNull SchemaSourceProvider<YangTextSchemaSource> schemaSourceProvider;

        WithYangTextSources(final EffectiveModelContextProvider schemaContextProvider,
                final SchemaSourceProvider<YangTextSchemaSource> schemaSourceProvider) {
            super(schemaContextProvider);
            this.schemaSourceProvider = requireNonNull(schemaSourceProvider);
        }

        @Override
        public Collection<DOMSchemaServiceExtension> supportedExtensions() {
            return List.of(this);
        }

        @Override
        public ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
            return schemaSourceProvider.getSource(sourceIdentifier);
        }
    }

    private final @NonNull EffectiveModelContextProvider schemaContextProvider;

    private FixedDOMSchemaService(final EffectiveModelContextProvider schemaContextProvider) {
        this.schemaContextProvider = requireNonNull(schemaContextProvider);
    }

    public static @NonNull DOMSchemaService of(final EffectiveModelContext effectiveModel) {
        final EffectiveModelContext checked = requireNonNull(effectiveModel);
        return of(() -> checked);
    }

    public static @NonNull DOMSchemaService of(final EffectiveModelContextProvider schemaContextProvider) {
        return new FixedDOMSchemaService(schemaContextProvider);
    }

    public static @NonNull DOMSchemaService of(final EffectiveModelContextProvider schemaContextProvider,
            final SchemaSourceProvider<YangTextSchemaSource> yangTextSourceProvider) {
        return new WithYangTextSources(schemaContextProvider, requireNonNull(yangTextSourceProvider));
    }

    @Override
    public final EffectiveModelContext getGlobalContext() {
        return schemaContextProvider.getEffectiveModelContext();
    }

    @Override
    public final Registration registerSchemaContextListener(final EffectiveModelContextListener listener) {
        listener.onModelContextUpdated(getGlobalContext());
        return NoOpListenerRegistration.of(listener);
    }
}
