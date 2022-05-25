/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.binding.dom.adapter {
    exports org.opendaylight.mdsal.binding.dom.adapter;
    exports org.opendaylight.mdsal.binding.dom.adapter.osgi;
    exports org.opendaylight.mdsal.binding.dom.adapter.spi;

    uses org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;

    requires transitive org.opendaylight.mdsal.binding.api;
    requires transitive org.opendaylight.mdsal.common.api;
    requires transitive org.opendaylight.mdsal.dom.api;
    requires transitive org.opendaylight.mdsal.dom.spi;
    requires org.opendaylight.mdsal.binding.dom.codec.api;
    requires org.opendaylight.mdsal.binding.dom.codec.spi;
    requires org.opendaylight.mdsal.binding.spec.util;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.data.api;
    requires org.opendaylight.yangtools.yang.data.impl;
    requires org.opendaylight.yangtools.yang.model.util;
    requires org.slf4j;

    // OSGi is optional
    requires org.osgi.framework;
    requires org.osgi.service.component;
    requires org.osgi.util.tracker;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static transitive javax.inject;
    requires static org.gaul.modernizer_maven_annotations;
    requires static com.github.spotbugs.annotations;
    requires static org.kohsuke.metainf_services;
    requires static org.osgi.service.component.annotations;

    // FIXME: needed through Guava, let it sort this out
    requires static failureaccess;
}
