/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext;
import org.opendaylight.mdsal.binding.dom.adapter.BindingAdapterFactory;
import org.opendaylight.mdsal.binding.dom.adapter.ConstantAdapterContext;
import org.opendaylight.mdsal.binding.dom.adapter.query.DefaultQueryFactory;
import org.opendaylight.mdsal.binding.dom.adapter.spi.AdapterFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;

module org.opendaylight.mdsal.binding.dom.adapter {
    exports org.opendaylight.mdsal.binding.dom.adapter;
    //exports org.opendaylight.mdsal.binding.dom.adapter.invoke;
    exports org.opendaylight.mdsal.binding.dom.adapter.spi;

    provides AdapterFactory with BindingAdapterFactory;
    provides AdapterContext with ConstantAdapterContext;
    provides QueryFactory with DefaultQueryFactory;

    uses BindingCodecTreeFactory;
    uses BindingDOMCodecFactory;

    requires transitive com.google.common;
    requires transitive javax.inject;
    requires transitive org.kohsuke.metainf_services;
    requires transitive org.opendaylight.mdsal.binding.api;
    requires transitive org.opendaylight.mdsal.binding.dom.codec.spi;
    requires transitive org.opendaylight.mdsal.dom.api;
    requires transitive org.opendaylight.yangtools.yang.data.impl;
    requires org.opendaylight.mdsal.binding.dom.codec.api;
    requires org.opendaylight.mdsal.binding.spec.util;
    requires org.opendaylight.mdsal.dom.spi;
    requires org.osgi.framework;
    requires org.osgi.service.component;
    requires org.osgi.util.tracker;
    requires org.slf4j;

    // Annotations
    requires static org.eclipse.jdt.annotation;
    requires static org.gaul.modernizer_maven_annotations;
    requires static org.osgi.service.component.annotations;
}