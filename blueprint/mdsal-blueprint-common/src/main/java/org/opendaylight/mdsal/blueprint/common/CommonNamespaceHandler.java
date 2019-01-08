/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.blueprint.common;

import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.apache.aries.blueprint.NamespaceHandler;
import org.apache.aries.blueprint.ParserContext;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

final class CommonNamespaceHandler implements NamespaceHandler {
    static final String NAMESPACE = "http://opendaylight.org/xmlns/mdsal/blueprint/common/v1.0.0";
    private static final Logger LOG = LoggerFactory.getLogger(CommonNamespaceHandler.class);

    @Override
    public ComponentMetadata decorate(Node node, ComponentMetadata component, ParserContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Class> getManagedClasses() {
        return Collections.emptySet();
    }

    @Override
    public URL getSchemaLocation(String namespace) {
        if (NAMESPACE.equals(namespace)) {
            URL url = getClass().getResource("/odl-mdsal-blueprint-common-1.0.0.xsd");
            LOG.debug("getSchemaLocation for {} returning URL {}", namespace, url);
            return url;
        }

        return null;
    }

    @Override
    public Metadata parse(Element element, ParserContext context) {
        // TODO Auto-generated method stub
        return null;
    }
}
