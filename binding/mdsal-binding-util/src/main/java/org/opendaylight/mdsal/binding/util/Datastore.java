/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

/**
 * Strongly-typed representation of a datastore (configuration or operational).
 */
@Beta
// FIXME Base this on ietf-datastores.yang (RFC 8342)
public abstract class Datastore {

    /** Class representing the configuration datastore. */
    public static final Class<Configuration> CONFIGURATION = Configuration.class;

    /** Class representing the operational datastore. */
    public static final Class<Operational> OPERATIONAL = Operational.class;

    public static final class Configuration extends Datastore {

    }

    public static final class Operational extends Datastore {

    }

    /**
     * Returns the logical datastore type corresponding to the given datastore class.
     *
     * @param datastoreClass The datastore class to convert.
     * @return The corresponding logical datastore type.
     * @throws IllegalArgumentException if the provided datastore class isn’t handled.
     */
    public static LogicalDatastoreType toType(Class<? extends Datastore> datastoreClass) {
        if (datastoreClass.equals(Configuration.class)) {
            return LogicalDatastoreType.CONFIGURATION;
        } else if (Operational.class.equals(datastoreClass)) {
            return LogicalDatastoreType.OPERATIONAL;
        } else {
            throw new IllegalArgumentException("Unknown datastore class " + datastoreClass);
        }
    }

    /**
     * Returns the datastore class corresponding to the given logical datastore type.
     * @param datastoreType The logical datastore type to convert.
     * @return The corresponding datastore class.
     * @throws IllegalArgumentException if the provided logical datastore type isn’t handled.
     */
    public static Class<? extends Datastore> toClass(LogicalDatastoreType datastoreType) {
        switch (datastoreType) {
            case CONFIGURATION:
                return CONFIGURATION;
            case OPERATIONAL:
                return OPERATIONAL;
            default:
                throw new IllegalArgumentException("Unknown datastore type " + datastoreType);
        }
    }

    private Datastore() {

    }
}
