/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.dom.spi {
    exports org.opendaylight.mdsal.dom.spi;
    exports org.opendaylight.mdsal.dom.spi.query;
    exports org.opendaylight.mdsal.dom.spi.store;

    requires transitive org.opendaylight.mdsal.common.api;
    requires transitive org.opendaylight.mdsal.dom.api;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.data.tree.api;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.yang.repo.api;
    requires com.google.common;
    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.odlext.model.api;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.data.api;
    requires org.opendaylight.yangtools.yang.data.tree.spi;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static org.checkerframework.checker.qual;
}
