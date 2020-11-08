/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.eos.dom.simple {
    exports org.opendaylight.mdsal.eos.dom.simple.di;

    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.mdsal.eos.dom.api;
    requires org.slf4j;

    provides org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService
        with org.opendaylight.mdsal.eos.dom.simple.SimpleDOMEntityOwnershipService;

    // Annotations
    requires static com.github.spotbugs.annotations;
    requires static org.eclipse.jdt.annotation;
    requires static org.checkerframework.checker.qual;
    requires static javax.inject;
    requires static metainf.services;
    requires static org.osgi.service.component.annotations;
}
