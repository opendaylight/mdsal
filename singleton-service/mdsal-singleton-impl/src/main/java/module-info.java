/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.singleton.impl {
    exports org.opendaylight.mdsal.singleton.impl;

    requires transitive org.opendaylight.mdsal.singleton.api;
    requires transitive org.opendaylight.mdsal.eos.dom.api;
    requires org.opendaylight.mdsal.eos.common.api;
    requires org.opendaylight.yangtools.concepts;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static transitive java.annotation;
    requires static transitive javax.inject;
    requires static org.checkerframework.checker.qual;
    requires static org.osgi.service.component.annotations;
}
