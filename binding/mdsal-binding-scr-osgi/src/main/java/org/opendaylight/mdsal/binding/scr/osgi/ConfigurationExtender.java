/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.osgi;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ArrayListMultimap;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.config.ConfigurationService;
import org.opendaylight.mdsal.binding.scr.api.DescriptorConstants;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@Component(service = {})
@RequireServiceComponentRuntime
public final class ConfigurationExtender extends BundleTracker<BundleConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationExtender.class);
    private static final @NonNull DocumentBuilderFactory DBF;

    static {
        final DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setCoalescing(true);
        f.setExpandEntityReferences(false);
        f.setIgnoringElementContentWhitespace(true);
        f.setIgnoringComments(true);
        f.setNamespaceAware(true);
        f.setXIncludeAware(false);
        try {
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (ParserConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }

        try {
            // This makes our parsing code a breeze, as we only have valid documents
            f.setSchema(DescriptorConstants.descriptorSchema());
        } catch (UnsupportedOperationException e) {
            throw new ExceptionInInitializerError(e);
        }

        DBF = f;
    }

    private final ConfigurationService configService;

    @Activate
    public ConfigurationExtender(final BundleContext context, final @Reference ConfigurationService configService) {
        super(context, Bundle.STARTING | Bundle.ACTIVE, null);
        this.configService = requireNonNull(configService);
    }

    @Deactivate
    void deactivate() {
        // FIXME: drop all configurations
    }

    @Override
    public BundleConfiguration addingBundle(final Bundle bundle, final BundleEvent event) {
        final var resource = bundle.getEntry(DescriptorConstants.METAINF_SCR_CONFIGURATION);
        if (resource == null) {
            LOG.debug("Bundle {} does not have a {}", bundle, DescriptorConstants.METAINF_SCR_CONFIGURATION);
            return null;
        }

        // Try to parse the XML fragment with
        final Element initialNode;
        try {
            initialNode = DBF.newDocumentBuilder().parse(resource.openStream()).getDocumentElement();
        } catch (IOException e) {
            LOG.warn("Failed to read bundle {} descriptor", bundle, e);
            return null;
        } catch (ParserConfigurationException | SAXException e) {
            LOG.warn("Failed to parse bundle {} descriptor", bundle, e);
            return null;
        }

        // Collect field references keyed by their defining class
        final var fields = ArrayListMultimap.<String, BundleField>create();
        for (var fieldNode = initialNode.getFirstChild(); fieldNode != null; fieldNode = fieldNode.getNextSibling()) {
            final var field = new BundleField((Element) fieldNode);
            fields.put(field.className, field);
        }

        // Now walk all through classes and locate their fields
        final var fieldValues = new HashMap<BundleField, ChildOf<?>>();
        for (var entry : fields.asMap().entrySet()) {
            final var className = entry.getKey();
            final Class<?> loadedClass;
            try {
                loadedClass = bundle.loadClass(className);
            } catch (ClassNotFoundException e) {
                LOG.warn("Failed to find {} in bundle {}, ignoring %s fields", className, bundle,
                    entry.getValue().size());
                continue;
            }

            for (var field : entry.getValue()) {
                final Field classField;
                try {
                    classField = loadedClass.getDeclaredField(field.fieldName);
                } catch (NoSuchFieldException | SecurityException e) {
                    LOG.warn("Failed to find field {} in {} of bundle {}", field.fieldName, loadedClass, bundle);
                    continue;
                }

                final Object classValue;
                try {
                    classValue = classField.get(null);
                } catch (IllegalAccessException | IllegalArgumentException | NullPointerException
                        | ExceptionInInitializerError e) {
                    LOG.warn("Failed to extract {} value in bundle {}, ignoring it", classField, bundle, e);
                    continue;
                }

                if (classValue instanceof ChildOf) {
                    // Early check for ConfigurationService compatibility
                    fieldValues.put(field, (ChildOf<?>) classValue);
                } else {
                    LOG.warn("Ignoring {} in bundle {}: unexpected value {}", classField, bundle, classValue);
                }
            }
        }

        // FIXME: process fieldValues
        return null;
    }
}
