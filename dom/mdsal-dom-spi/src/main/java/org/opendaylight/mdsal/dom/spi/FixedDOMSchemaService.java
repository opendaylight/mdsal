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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;

/**
 * {@link DOMSchemaService} (and {@link DOMYangTextSourceProvider}) implementations backed by a
 * {@code Supplier<EffectiveModelContext>} (and {@link SchemaSourceProvider}) which are known to be fixed and never
 * change schemas.
 *
 * @author Michael Vorburger.ch
 */
@Beta
@NonNullByDefault
public sealed class FixedDOMSchemaService implements DOMSchemaService {
    private static final class WithYangTextSources extends FixedDOMSchemaService implements DOMYangTextSourceProvider {
        private final SchemaSourceProvider<YangTextSource> schemaSourceProvider;

        WithYangTextSources(final Supplier<EffectiveModelContext> modelContextSupplier,
                final SchemaSourceProvider<YangTextSource> schemaSourceProvider) {
            super(modelContextSupplier);
            this.schemaSourceProvider = requireNonNull(schemaSourceProvider);
        }

        @Override
        public List<Extension> supportedExtensions() {
            return List.of(this);
        }

        @Override
        public ListenableFuture<? extends YangTextSource> getSource(final SourceIdentifier sourceIdentifier) {
            return schemaSourceProvider.getSource(sourceIdentifier);
        }
    }

    private final Supplier<EffectiveModelContext> modelContextSupplier;

    private FixedDOMSchemaService(final Supplier<EffectiveModelContext> modelContextSupplier) {
        this.modelContextSupplier = requireNonNull(modelContextSupplier);
    }

    public static DOMSchemaService of(final EffectiveModelContext effectiveModel) {
        final var checked = requireNonNull(effectiveModel);
        return new FixedDOMSchemaService(() -> checked);
    }

    public static DOMSchemaService of(final Supplier<EffectiveModelContext> modelContextSupplier) {
        return new FixedDOMSchemaService(modelContextSupplier);
    }

    public static DOMSchemaService of(final Supplier<EffectiveModelContext> modelContextSupplier,
            final SchemaSourceProvider<YangTextSource> yangTextSourceProvider) {
        return new WithYangTextSources(modelContextSupplier, requireNonNull(yangTextSourceProvider));
    }

    @Override
    public final EffectiveModelContext getGlobalContext() {
        return modelContextSupplier.get();
    }

    @Override
    public final Registration registerSchemaContextListener(final Consumer<EffectiveModelContext> listener) {
        listener.accept(getGlobalContext());
        return () -> { };
    }
}
