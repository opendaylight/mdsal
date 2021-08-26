/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.mdsal.binding.dom.codec.impl.DefaultBindingDOMCodecFactory;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.impl.DefaultBindingCodecTreeFactory;

module org.opendaylight.mdsal.binding.dom.codec {
    provides BindingDOMCodecFactory with DefaultBindingDOMCodecFactory;
    provides BindingDOMCodecServices with BindingCodecContext;
    provides BindingCodecTreeFactory with DefaultBindingCodecTreeFactory;

    requires transitive org.opendaylight.mdsal.binding.dom.codec.spi;
    requires transitive org.opendaylight.mdsal.binding.dom.codec.api;
    requires com.google.common;
    requires org.opendaylight.mdsal.binding.spec.util;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.data.util;
    requires org.opendaylight.yangtools.yang.model.util;
    requires org.opendaylight.yangtools.yang.binding;
    requires org.opendaylight.yangtools.yang.data.impl;
    requires org.slf4j;
    requires osgi.cmpn;
    requires net.bytebuddy;

    requires static org.eclipse.jdt.annotation;
    requires static javax.inject;
    requires static metainf.services;
    requires static com.github.spotbugs.annotations;
}