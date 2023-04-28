/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.dom.broker {
    exports org.opendaylight.mdsal.dom.broker;

    requires transitive org.opendaylight.mdsal.common.api;
    requires transitive org.opendaylight.mdsal.dom.api;
    requires transitive org.opendaylight.mdsal.dom.spi;
    requires com.google.common;
    requires org.slf4j;

    // Annotations
    requires static transitive java.annotation;
    requires static transitive javax.inject;
    requires static transitive org.kohsuke.metainf_services;
    requires static transitive org.osgi.service.component.annotations;
    requires static org.osgi.service.metatype.annotations;
}