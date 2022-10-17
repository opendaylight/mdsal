/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

/**
 * Strongly-typed representation of a datastore (configuration or operational).
 */
@Beta
// FIXME Base this on ietf-datastores.yang (RFC 8342)
public abstract sealed class Datastore {
    /**
     * Class representing the configuration datastore.
     */
    public static final class Configuration extends Datastore {
        private Configuration() {
            super(LogicalDatastoreType.CONFIGURATION);
        }
    }

    /**
     * Class representing the operational datastore.
     */
    public static final class Operational extends Datastore {
        private Operational() {
            super(LogicalDatastoreType.OPERATIONAL);
        }
    }

    public static final Operational OPERATIONAL = new Operational();
    public static final Configuration CONFIGURATION = new Configuration();

    private final LogicalDatastoreType type;

    private Datastore(final LogicalDatastoreType type) {
        this.type = requireNonNull(type);
    }

    /**
     * Returns the logical datastore type corresponding to thisclass.
     *
     * @return The corresponding logical datastore type.
     */
    public LogicalDatastoreType type() {
        return type;
    }

    /**
     * Returns the Datastore corresponding to the given logical datastore type.
     *
     * @param type The logical datastore type to convert.
     * @return The corresponding Datastore
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static Datastore ofType(final LogicalDatastoreType type) {
        return switch (type) {
            case CONFIGURATION -> CONFIGURATION;
            case OPERATIONAL -> OPERATIONAL;
        };
    }
}
