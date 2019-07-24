/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.net.URI;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * A reference to a YANG source, potentially containing a location hint.
 */
@Beta
public final class SourceReference extends AbstractIdentifiable<SourceIdentifier> implements Immutable {
    private final @Nullable URI location;

    SourceReference(final SourceIdentifier identifier, final @Nullable URI location) {
        super(identifier);
        this.location = location;
    }

    public static SourceReference of(final SourceIdentifier identifier) {
        return new SourceReference(identifier, null);
    }

    public static SourceReference of(final SourceIdentifier identifier, final @NonNull URI location) {
        return new SourceReference(identifier, requireNonNull(location));
    }

    public Optional<URI> getLocation() {
        return Optional.ofNullable(location);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper.omitNullValues()).add("location", location);
    }
}
