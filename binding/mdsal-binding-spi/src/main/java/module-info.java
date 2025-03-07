/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * MD-SAL Binding API helper classes.
 */
module org.opendaylight.mdsal.binding.spi {
    exports org.opendaylight.mdsal.binding.spi;

    requires transitive org.opendaylight.mdsal.binding.api;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;
}
