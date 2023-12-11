/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.dom.api {
    exports org.opendaylight.mdsal.dom.api;
    exports org.opendaylight.mdsal.dom.api.query;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.data.api;
    requires transitive org.opendaylight.yangtools.yang.data.tree.api;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.yang.model.spi;
    requires transitive org.opendaylight.yangtools.yang.repo.spi;
    requires transitive org.opendaylight.mdsal.common.api;
    requires transitive org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.util;

    // Annotations
    requires static com.github.spotbugs.annotations;
    requires static transitive org.checkerframework.checker.qual;
    requires static transitive org.eclipse.jdt.annotation;
}
