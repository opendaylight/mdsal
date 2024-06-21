/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext;
import org.opendaylight.mdsal.binding.dom.adapter.BindingAdapterFactory;
import org.opendaylight.mdsal.binding.dom.adapter.ConstantAdapterContext;
import org.opendaylight.mdsal.binding.dom.adapter.query.DefaultQueryFactory;
import org.opendaylight.mdsal.binding.dom.adapter.spi.AdapterFactory;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeFactory;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecFactory;

module org.opendaylight.mdsal.binding.dom.adapter {
    exports org.opendaylight.mdsal.binding.dom.adapter;
    exports org.opendaylight.mdsal.binding.dom.adapter.spi;

    provides AdapterFactory with BindingAdapterFactory;
    provides AdapterContext with ConstantAdapterContext;
    provides QueryFactory with DefaultQueryFactory;

    uses BindingCodecTreeFactory;
    uses BindingDOMCodecFactory;

    requires transitive com.google.common;
    requires transitive org.opendaylight.mdsal.binding.api;
    requires transitive org.opendaylight.mdsal.dom.api;
    requires transitive org.opendaylight.yangtools.binding.data.codec.spi;
    requires transitive org.opendaylight.yangtools.yang.data.impl;
    requires com.google.common.util.concurrent.internal;
    requires org.opendaylight.mdsal.dom.spi;
    requires org.opendaylight.yangtools.binding.data.codec.api;
    requires org.opendaylight.yangtools.binding.runtime.api;
    requires org.opendaylight.yangtools.binding.reflect;
    requires org.opendaylight.yangtools.binding.spec;
    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.data.api;
    requires org.opendaylight.yangtools.yang.data.spi;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.slf4j;

    // OSGi is optional
    requires static org.osgi.framework;
    requires static org.osgi.service.component;
    requires static org.osgi.util.tracker;

    // Annotations
    requires static com.github.spotbugs.annotations;
    requires static javax.inject;
    requires static org.kohsuke.metainf_services;
    requires static org.eclipse.jdt.annotation;
    requires static org.gaul.modernizer_maven_annotations;
    requires static org.osgi.service.component.annotations;
}
