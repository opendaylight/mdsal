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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * {@link DOMSchemaService} backed by a {@code Supplier<EffectiveModelContext>}
 * (and potentially a {@link YangTextSourceExtension}) which are known to be fixed
 * and never change schemas.
 *
 * @author Michael Vorburger.ch
 */
@Beta
@NonNullByDefault
public record FixedDOMSchemaService(
        Supplier<EffectiveModelContext> modelContextSupplier,
        @Nullable YangTextSourceExtension extension) implements DOMSchemaService {
    public FixedDOMSchemaService {
        requireNonNull(modelContextSupplier);
    }

    public FixedDOMSchemaService(final Supplier<EffectiveModelContext> modelContextSupplier) {
        this(modelContextSupplier, null);
    }

    public FixedDOMSchemaService(final EffectiveModelContext effectiveModel) {
        this(() -> effectiveModel, null);
        requireNonNull(effectiveModel);
    }

    @Override
    public List<Extension> supportedExtensions() {
        final var local = extension;
        return local == null ? List.of() : List.of(local);
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
