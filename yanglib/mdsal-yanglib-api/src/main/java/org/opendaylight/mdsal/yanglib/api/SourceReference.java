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
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * A reference to a YANG source, potentially containing a location hint.
 */
@Beta
@NonNullByDefault
public final class SourceReference extends AbstractIdentifiable<SourceIdentifier> implements Immutable {
    private final ImmutableSet<URI> locations;

    private SourceReference(final SourceIdentifier identifier, final ImmutableSet<URI> locations) {
        super(identifier);
        this.locations = requireNonNull(locations);
    }

    public static SourceReference of(final SourceIdentifier identifier) {
        return new SourceReference(identifier, ImmutableSet.of());
    }

    public static SourceReference of(final SourceIdentifier identifier, final URI location) {
        return new SourceReference(identifier, ImmutableSet.of(location));
    }

    public static SourceReference of(final SourceIdentifier identifier, final List<URI> locations) {
        return new SourceReference(identifier, ImmutableSet.copyOf(locations));
    }

    public ImmutableSet<URI> getLocations() {
        return locations;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        super.addToStringAttributes(toStringHelper);
        if (!locations.isEmpty()) {
            toStringHelper.add("locations", locations);
        }
        return toStringHelper;
    }
}
