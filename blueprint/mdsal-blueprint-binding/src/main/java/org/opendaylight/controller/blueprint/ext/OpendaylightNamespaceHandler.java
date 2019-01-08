/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.blueprint.ext;

import static org.opendaylight.mdsal.blueprint.common.BlueprintConstants.INTERFACE;
import static org.opendaylight.mdsal.blueprint.common.BlueprintConstants.REF_ATTR;
import static org.opendaylight.mdsal.blueprint.common.NamespaceHandlerUtils.addBlueprintBundleRefProperty;
import static org.opendaylight.mdsal.blueprint.common.NamespaceHandlerUtils.createBeanMetadata;
import static org.opendaylight.mdsal.blueprint.common.NamespaceHandlerUtils.createRef;
import static org.opendaylight.mdsal.blueprint.common.NamespaceHandlerUtils.createServiceRef;
import static org.opendaylight.mdsal.blueprint.common.NamespaceHandlerUtils.createValue;
import static org.opendaylight.mdsal.blueprint.common.NamespaceHandlerUtils.getId;
import static org.opendaylight.mdsal.blueprint.common.NamespaceHandlerUtils.nodeNameEquals;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.apache.aries.blueprint.ComponentDefinitionRegistry;
import org.apache.aries.blueprint.NamespaceHandler;
import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.ext.ComponentFactoryMetadata;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.aries.blueprint.mutable.MutableReferenceMetadata;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.osgi.service.blueprint.reflect.ReferenceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The NamespaceHandler for Opendaylight blueprint extensions.
 *
 * @author Thomas Pantelis
 */
public final class OpendaylightNamespaceHandler implements NamespaceHandler {
    public static final String NAMESPACE_1_0_0 = "http://opendaylight.org/xmlns/blueprint/v1.0.0";
    static final String ROUTED_RPC_REG_CONVERTER_NAME = "org.opendaylight.blueprint.RoutedRpcRegConverter";
    static final String DOM_RPC_PROVIDER_SERVICE_NAME = "org.opendaylight.blueprint.DOMRpcProviderService";
    static final String BINDING_RPC_PROVIDER_SERVICE_NAME = "org.opendaylight.blueprint.RpcProviderService";
    static final String SCHEMA_SERVICE_NAME = "org.opendaylight.blueprint.SchemaService";
    static final String NOTIFICATION_SERVICE_NAME = "org.opendaylight.blueprint.NotificationService";
    static final String TYPE_ATTR = "type";
    static final String UPDATE_STRATEGY_ATTR = "update-strategy";

    private static final Logger LOG = LoggerFactory.getLogger(OpendaylightNamespaceHandler.class);
    private static final String CLUSTERED_APP_CONFIG = "clustered-app-config";

    private static final String RPC_SERVICE = "rpc-service";
    private static final String ACTION_SERVICE = "action-service";

    @SuppressWarnings("rawtypes")
    @Override
    public Set<Class> getManagedClasses() {
        return Collections.emptySet();
    }

    @Override
    public URL getSchemaLocation(final String namespace) {
        if (NAMESPACE_1_0_0.equals(namespace)) {
            URL url = getClass().getResource("/opendaylight-blueprint-ext-1.0.0.xsd");
            LOG.debug("getSchemaLocation for {} returning URL {}", namespace, url);
            return url;
        }

        return null;
    }

    @Override
    public Metadata parse(final Element element, final ParserContext context) {
        LOG.debug("In parse for {}", element);

        if (nodeNameEquals(element, RpcImplementationBean.RPC_IMPLEMENTATION)) {
            return parseRpcImplementation(element, context);
        } else if (nodeNameEquals(element, RoutedRpcMetadata.ROUTED_RPC_IMPLEMENTATION)) {
            return parseRoutedRpcImplementation(element, context);
        } else if (nodeNameEquals(element, RPC_SERVICE)) {
            return parseRpcService(element, context);
        } else if (nodeNameEquals(element, NotificationListenerBean.NOTIFICATION_LISTENER)) {
            return parseNotificationListener(element, context);
        } else if (nodeNameEquals(element, CLUSTERED_APP_CONFIG)) {
            return parseClusteredAppConfig(element, context);
        } else if (nodeNameEquals(element, ACTION_SERVICE)) {
            return parseActionService(element, context);
        } else if (nodeNameEquals(element, ActionProviderBean.ACTION_PROVIDER)) {
            return parseActionProvider(element, context);
        }

        throw new ComponentDefinitionException("Unsupported standalone element: " + element.getNodeName());
    }

    @Override
    public ComponentMetadata decorate(final Node node, final ComponentMetadata component, final ParserContext context) {
        // FIXME: throw exception?
        return null;
    }

    private static Metadata parseActionProvider(final Element element, final ParserContext context) {
        registerDomRpcProviderServiceRefBean(context);
        registerBindingRpcProviderServiceRefBean(context);
        registerSchemaServiceRefBean(context);

        MutableBeanMetadata metadata = createBeanMetadata(context, context.generateId(), ActionProviderBean.class,
                true, true);
        addBlueprintBundleRefProperty(context, metadata);
        metadata.addProperty("domRpcProvider", createRef(context, DOM_RPC_PROVIDER_SERVICE_NAME));
        metadata.addProperty("bindingRpcProvider", createRef(context, BINDING_RPC_PROVIDER_SERVICE_NAME));
        metadata.addProperty("schemaService", createRef(context, SCHEMA_SERVICE_NAME));
        metadata.addProperty("interfaceName", createValue(context, element.getAttribute(INTERFACE)));

        if (element.hasAttribute(REF_ATTR)) {
            metadata.addProperty("implementation", createRef(context, element.getAttribute(REF_ATTR)));
        }

        LOG.debug("parseActionProvider returning {}", metadata);
        return metadata;
    }


    private static Metadata parseRpcImplementation(final Element element, final ParserContext context) {
        registerBindingRpcProviderServiceRefBean(context);

        MutableBeanMetadata metadata = createBeanMetadata(context, context.generateId(), RpcImplementationBean.class,
                true, true);
        addBlueprintBundleRefProperty(context, metadata);
        metadata.addProperty("rpcProvider", createRef(context, BINDING_RPC_PROVIDER_SERVICE_NAME));
        metadata.addProperty("implementation", createRef(context, element.getAttribute(REF_ATTR)));

        if (element.hasAttribute(INTERFACE)) {
            metadata.addProperty("interfaceName", createValue(context, element.getAttribute(INTERFACE)));
        }

        LOG.debug("parseRpcImplementation returning {}", metadata);
        return metadata;
    }

    private static Metadata parseRoutedRpcImplementation(final Element element, final ParserContext context) {
        registerBindingRpcProviderServiceRefBean(context);
        registerRoutedRpcRegistrationConverter(context);

        ComponentFactoryMetadata metadata = new RoutedRpcMetadata(getId(context, element),
                element.getAttribute(INTERFACE), element.getAttribute(REF_ATTR));

        LOG.debug("parseRoutedRpcImplementation returning {}", metadata);

        return metadata;
    }

    private static Metadata parseActionService(final Element element, final ParserContext context) {
        ComponentFactoryMetadata metadata = new ActionServiceMetadata(getId(context, element),
                element.getAttribute(INTERFACE));

        LOG.debug("parseActionService returning {}", metadata);

        return metadata;
    }

    private static Metadata parseRpcService(final Element element, final ParserContext context) {
        ComponentFactoryMetadata metadata = new RpcServiceMetadata(getId(context, element),
                element.getAttribute(INTERFACE));

        LOG.debug("parseRpcService returning {}", metadata);

        return metadata;
    }

    private static void registerRoutedRpcRegistrationConverter(final ParserContext context) {
        ComponentDefinitionRegistry registry = context.getComponentDefinitionRegistry();
        if (registry.getComponentDefinition(ROUTED_RPC_REG_CONVERTER_NAME) == null) {
            MutableBeanMetadata metadata = createBeanMetadata(context, ROUTED_RPC_REG_CONVERTER_NAME,
                    RoutedRpcRegistrationConverter.class, false, false);
            metadata.setActivation(ReferenceMetadata.ACTIVATION_LAZY);
            registry.registerTypeConverter(metadata);
        }
    }

    private static void registerDomRpcProviderServiceRefBean(final ParserContext context) {
        registerRefBean(context, DOM_RPC_PROVIDER_SERVICE_NAME, DOMRpcProviderService.class);
    }

    private static void registerBindingRpcProviderServiceRefBean(final ParserContext context) {
        registerRefBean(context, BINDING_RPC_PROVIDER_SERVICE_NAME, RpcProviderService.class);
    }

    private static void registerSchemaServiceRefBean(final ParserContext context) {
        registerRefBean(context, SCHEMA_SERVICE_NAME, DOMSchemaService.class);
    }

    private static void registerRefBean(final ParserContext context, final String name, final Class<?> clazz) {
        ComponentDefinitionRegistry registry = context.getComponentDefinitionRegistry();
        if (registry.getComponentDefinition(name) == null) {
            MutableReferenceMetadata metadata = createServiceRef(context, clazz, null);
            metadata.setId(name);
            registry.registerComponentDefinition(metadata);
        }
    }

    private static Metadata parseNotificationListener(final Element element, final ParserContext context) {
        registerNotificationServiceRefBean(context);

        MutableBeanMetadata metadata = createBeanMetadata(context, context.generateId(), NotificationListenerBean.class,
                true, true);
        addBlueprintBundleRefProperty(context, metadata);
        metadata.addProperty("notificationService", createRef(context, NOTIFICATION_SERVICE_NAME));
        metadata.addProperty("notificationListener", createRef(context, element.getAttribute(REF_ATTR)));

        LOG.debug("parseNotificationListener returning {}", metadata);

        return metadata;
    }

    private static void registerNotificationServiceRefBean(final ParserContext context) {
        ComponentDefinitionRegistry registry = context.getComponentDefinitionRegistry();
        if (registry.getComponentDefinition(NOTIFICATION_SERVICE_NAME) == null) {
            MutableReferenceMetadata metadata = createServiceRef(context, NotificationService.class, null);
            metadata.setId(NOTIFICATION_SERVICE_NAME);
            registry.registerComponentDefinition(metadata);
        }
    }

    private static Metadata parseClusteredAppConfig(final Element element, final ParserContext context) {
        LOG.debug("parseClusteredAppConfig");

        // Find the default-config child element representing the default app config XML, if present.
        Element defaultConfigElement = null;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (nodeNameEquals(child, DataStoreAppConfigMetadata.DEFAULT_CONFIG)) {
                defaultConfigElement = (Element) child;
                break;
            }
        }

        Element defaultAppConfigElement = null;
        if (defaultConfigElement != null) {
            // Find the CDATA element containing the default app config XML.
            children = defaultConfigElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                    defaultAppConfigElement = parseXML(DataStoreAppConfigMetadata.DEFAULT_CONFIG,
                            child.getTextContent());
                    break;
                }
            }
        }

        return new DataStoreAppConfigMetadata(getId(context, element), element.getAttribute(
                DataStoreAppConfigMetadata.BINDING_CLASS), element.getAttribute(
                        DataStoreAppConfigMetadata.LIST_KEY_VALUE), element.getAttribute(
                        DataStoreAppConfigMetadata.DEFAULT_CONFIG_FILE_NAME), parseUpdateStrategy(
                        element.getAttribute(UPDATE_STRATEGY_ATTR)), defaultAppConfigElement);
    }

    private static UpdateStrategy parseUpdateStrategy(final String updateStrategyValue) {
        if (Strings.isNullOrEmpty(updateStrategyValue)
                || updateStrategyValue.equalsIgnoreCase(UpdateStrategy.RELOAD.name())) {
            return UpdateStrategy.RELOAD;
        } else if (updateStrategyValue.equalsIgnoreCase(UpdateStrategy.NONE.name())) {
            return UpdateStrategy.NONE;
        } else {
            LOG.warn("update-strategy {} not supported, using reload", updateStrategyValue);
            return UpdateStrategy.RELOAD;
        }
    }

    private static Element parseXML(final String name, final String xml) {
        try {
            return UntrustedXML.newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement();
        } catch (SAXException | IOException e) {
            throw new ComponentDefinitionException(String.format("Error %s parsing XML: %s", name, xml), e);
        }
    }
}
