/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.dom.schema.osgi {
    exports org.opendaylight.mdsal.dom.schema.osgi;

    requires transitive com.google.common;
    requires transitive org.opendaylight.mdsal.binding.runtime.api;
    requires org.apache.karaf.features.core;
    requires org.opendaylight.mdsal.binding.runtime.spi;
    requires org.opendaylight.mdsal.dom.spi;
    requires org.opendaylight.yangtools.yang.binding;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.parser.api;
    requires org.osgi.framework;
    requires org.osgi.service.component;
    requires org.osgi.util.tracker;
    requires org.slf4j;

    // Annotations
    requires static org.osgi.service.component.annotations;
}