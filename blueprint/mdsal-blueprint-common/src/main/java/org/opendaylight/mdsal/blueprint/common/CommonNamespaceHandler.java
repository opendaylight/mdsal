/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.blueprint.common;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.blueprint.common.BlueprintConstants.INTERFACE;
import static org.opendaylight.mdsal.blueprint.common.NamespaceHandlerUtils.createValue;
import static org.opendaylight.mdsal.blueprint.common.NamespaceHandlerUtils.getId;
import static org.opendaylight.mdsal.blueprint.common.NamespaceHandlerUtils.nodeNameEquals;

import com.google.common.base.Strings;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.apache.aries.blueprint.NamespaceHandler;
import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.ext.ComponentFactoryMetadata;
import org.apache.aries.blueprint.mutable.MutableServiceMetadata;
import org.apache.aries.blueprint.mutable.MutableServiceReferenceMetadata;
import org.opendaylight.mdsal.blueprint.restart.api.BlueprintContainerRestartService;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.osgi.service.blueprint.reflect.ServiceMetadata;
import org.osgi.service.blueprint.reflect.ServiceReferenceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class CommonNamespaceHandler implements NamespaceHandler {
    static final String NAMESPACE = "http://opendaylight.org/xmlns/mdsal/blueprint/common/v1.0.0";
    static final String TYPE_ATTR = "type";

    private static final Logger LOG = LoggerFactory.getLogger(CommonNamespaceHandler.class);
    private static final String SPECIFIC_REFERENCE_LIST = "specific-reference-list";
    private static final String STATIC_REFERENCE = "static-reference";
    private static final String RESTART_DEPENDENTS_ON_UPDATES = "restart-dependents-on-updates";
    private static final String USE_DEFAULT_FOR_REFERENCE_TYPES = "use-default-for-reference-types";

    private final BlueprintContainerRestartService restartService;

    public CommonNamespaceHandler(BlueprintContainerRestartService restartService) {
        this.restartService = requireNonNull(restartService);
    }

    @Override
    public Set<Class> getManagedClasses() {
        return Collections.emptySet();
    }

    @Override
    public URL getSchemaLocation(String namespace) {
        if (!NAMESPACE.equals(namespace)) {
            LOG.debug("Unknown namespace {}", namespace);
            return null;
        }

        final URL url = CommonNamespaceHandler.class.getResource("/odl-mdsal-blueprint-common-1.0.0.xsd");
        LOG.debug("getSchemaLocation for {} returning URL {}", namespace, url);
        return url;
    }

    @Override
    public Metadata parse(Element element, ParserContext context) {
        if (nodeNameEquals(element, SPECIFIC_REFERENCE_LIST)) {
            return parseSpecificReferenceList(element, context);
        }
        if (nodeNameEquals(element, STATIC_REFERENCE)) {
            return parseStaticReference(element, context);
        }

        throw new ComponentDefinitionException("Unsupported standalone element: " + element.getNodeName());
    }

    @Override
    public ComponentMetadata decorate(Node node, ComponentMetadata component, ParserContext context) {
        if (!(node instanceof Attr)) {
            throw new ComponentDefinitionException("Unsupported node type: " + node);
        }

        final Attr attr = (Attr) node;
        if (nodeNameEquals(node, TYPE_ATTR)) {
            if (component instanceof ServiceMetadata) {
                return decorateServiceType(attr, component, context);
            }
            if (component instanceof ServiceReferenceMetadata) {
                return decorateServiceReferenceType(attr, component, context);
            }

            throw new ComponentDefinitionException("Attribute " + attr.getNodeName()
                    + " can only be used on a <reference>, <reference-list> or <service> element");
        }
        if (nodeNameEquals(node, RESTART_DEPENDENTS_ON_UPDATES)) {
            return ComponentProcessor.decorateRestartDependentsOnUpdates(attr, component, context);
        }
        if (nodeNameEquals(node, USE_DEFAULT_FOR_REFERENCE_TYPES)) {
            return ComponentProcessor.decorateUseDefaultForReferenceTypes(attr, component, context);
        }

        throw new ComponentDefinitionException("Unsupported attribute: " + attr.getNodeName());
    }

    private static ComponentMetadata decorateServiceReferenceType(final Attr attr, final ComponentMetadata component,
            final ParserContext context) {
        if (!(component instanceof MutableServiceReferenceMetadata)) {
            throw new ComponentDefinitionException("Expected an instanceof MutableServiceReferenceMetadata");
        }

        // We don't actually need the ComponentProcessor for augmenting the OSGi filter here but we create it
        // to workaround an issue in Aries where it doesn't use the extended filter unless there's a
        // Processor or ComponentDefinitionRegistryProcessor registered. This may actually be working as
        // designed in Aries b/c the extended filter was really added to allow the OSGi filter to be
        // substituted by a variable via the "cm:property-placeholder" processor. If so, it's a bit funky
        // but as long as there's at least one processor registered, it correctly uses the extended filter.
        ComponentProcessor.register(context);

        MutableServiceReferenceMetadata serviceRef = (MutableServiceReferenceMetadata)component;
        String oldFilter = serviceRef.getExtendedFilter() == null ? null :
            serviceRef.getExtendedFilter().getStringValue();

        String filter;
        if (Strings.isNullOrEmpty(oldFilter)) {
            filter = String.format("(type=%s)", attr.getValue());
        } else {
            filter = String.format("(&(%s)(type=%s))", oldFilter, attr.getValue());
        }

        LOG.debug("decorateServiceReferenceType for {} with type {}, old filter: {}, new filter: {}",
                serviceRef.getId(), attr.getValue(), oldFilter, filter);

        serviceRef.setExtendedFilter(createValue(context, filter));
        return component;
    }

    private static ComponentMetadata decorateServiceType(final Attr attr, final ComponentMetadata component,
            final ParserContext context) {
        if (!(component instanceof MutableServiceMetadata)) {
            throw new ComponentDefinitionException("Expected an instanceof MutableServiceMetadata");
        }

        MutableServiceMetadata service = (MutableServiceMetadata)component;

        LOG.debug("decorateServiceType for {} - adding type property {}", service.getId(), attr.getValue());

        service.addServiceProperty(createValue(context, TYPE_ATTR), createValue(context, attr.getValue()));
        return component;
    }


    private Metadata parseSpecificReferenceList(final Element element, final ParserContext context) {
        ComponentFactoryMetadata metadata = new SpecificReferenceListMetadata(getId(context, element), restartService,
                element.getAttribute(INTERFACE));
        LOG.debug("parseSpecificReferenceList returning {}", metadata);
        return metadata;
    }

    private Metadata parseStaticReference(final Element element, final ParserContext context) {
        ComponentFactoryMetadata metadata = new StaticReferenceMetadata(getId(context, element), restartService,
                element.getAttribute(INTERFACE));
        LOG.debug("parseStaticReference returning {}", metadata);
        return metadata;
    }
}
