/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * In-memory {@link org.opendaylight.mdsal.dom.spi.store.DOMStore} implementation.
 */
module org.opendaylight.mdsal.dom.store.inmemory {
    exports org.opendaylight.mdsal.dom.store.inmemory;

    requires transitive org.opendaylight.mdsal.dom.spi;
    requires org.opendaylight.yangtools.yang.data.spi;
    requires org.opendaylight.yangtools.yang.data.tree;
    requires org.opendaylight.yangtools.yang.data.tree.spi;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;

    // FIXME: remove this
    requires javax.inject;
}

