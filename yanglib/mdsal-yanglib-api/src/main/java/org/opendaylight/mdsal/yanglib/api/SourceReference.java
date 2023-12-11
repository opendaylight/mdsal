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
import com.google.common.collect.ImmutableList;
import java.net.URL;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * A reference to a YANG source, potentially containing a location hint.
 */
@Beta
@NonNullByDefault
public final class SourceReference extends AbstractSimpleIdentifiable<SourceIdentifier> implements Immutable {
    // List vs. Set vs. Collection here is a class design decision based on use. We expect these objects to be used
    // frequently, but for a short time. Locations may end up being ignored by SchemaContextResolver, hence it does not
    // make sense to ensure a Set here just to de-duplicate URLs.
    //
    // This transports their iteration order, hence we accept Collection construction argument, but keep the immutable
    // copy as a List
    private final ImmutableList<URL> locations;

    private SourceReference(final SourceIdentifier identifier, final ImmutableList<URL> locations) {
        super(identifier);
        this.locations = requireNonNull(locations);
    }

    public static SourceReference of(final SourceIdentifier identifier) {
        return new SourceReference(identifier, ImmutableList.of());
    }

    public static SourceReference of(final SourceIdentifier identifier, final URL location) {
        return new SourceReference(identifier, ImmutableList.of(location));
    }

    public static SourceReference of(final SourceIdentifier identifier, final Collection<URL> locations) {
        return new SourceReference(identifier, ImmutableList.copyOf(locations));
    }

    public ImmutableList<URL> getLocations() {
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
