/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.osgi;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.config.ConfigurationService;
import org.opendaylight.mdsal.binding.scr.spi.ConfigurationDescriptors;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
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
            f.setSchema(ConfigurationDescriptors.descriptorSchema());
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
        final var resource = bundle.getEntry(ConfigurationDescriptors.METAINF_SCR_CONFIGURATION);
        if (resource == null) {
            LOG.debug("Bundle {} does not have a {}", bundle, ConfigurationDescriptors.METAINF_SCR_CONFIGURATION);
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

        // Walk through all fields and construct their type mapping as seen by the bundle. We construct multiple
        // indices:
        // - typeToClass holds the FQCN typeName -> ChildOf mapping
        // - typeToModule holds the FQCN typeName -> DataRoot mapping
        final var typeToClass = new HashMap<String, Class<? extends DataObject>>();
        final var typeToModule = new HashMap<Class<? extends DataObject>, Class<? extends DataRoot>>();
        for (var field : fields.values()) {
            final var typeName = field.typeName;
            if (!typeToClass.containsKey(typeName)) {
                final Class<?> loadedClass;
                try {
                    loadedClass = bundle.loadClass(typeName);
                } catch (ClassNotFoundException e) {
                    LOG.warn("Failed to find {} in bundle {}", typeName, bundle, e);
                    continue;
                }

                final Class<? extends DataObject> typeClass;
                try {
                    typeClass = loadedClass.asSubclass(ChildOf.class);
                } catch (ClassCastException e) {
                    LOG.warn("Ignoring non-ChildOf {} in bundle {}", loadedClass, bundle, e);
                    continue;
                }

                final var childOfArgOpt = ClassLoaderUtils.findFirstGenericArgument(typeClass, ChildOf.class);
                if (childOfArgOpt.isEmpty()) {
                    LOG.warn("Cannot extract parent of {} in bundle {}, ignoring it", typeClass, bundle);
                    continue;
                }

                final var childOfArg = childOfArgOpt.orElseThrow();
                final Class<? extends DataRoot> moduleClass;
                try {
                    moduleClass = childOfArg.asSubclass(DataRoot.class);
                } catch (ClassCastException e) {
                    LOG.warn("Type {} is not a module root in bundle {} , ignoring type {}", childOfArg, bundle,
                        typeName, e);
                    continue;
                }

                final var conflictClass = typeToClass.put(typeName, typeClass);
                verify(conflictClass == null, "Unexpected type conflict on %s wth %s", typeName, conflictClass);
                final var conflictModule = typeToModule.put(typeClass, moduleClass);
                verify(conflictModule == null, "Unexpected module conflict on %s wth %s", typeName, conflictModule);
            }
        }

        // Walk all through classes and locate their corresponding fields
        final var fieldValues = new HashMap<BundleField, DataObject>();
        for (var entry : fields.asMap().entrySet()) {
            final var className = entry.getKey();
            final var classFields = entry.getValue();

            final Class<?> loadedClass;
            try {
                loadedClass = bundle.loadClass(className);
            } catch (ClassNotFoundException e) {
                LOG.warn("Failed to find {} in bundle {}, ignoring {} fields", className, bundle, classFields.size(),
                    e);
                continue;
            }

            for (var field : classFields) {
                final var expectedType = typeToClass.get(field.typeName);
                if (expectedType == null) {
                    LOG.warn("Unknown type for {} field {} in bundle {}, ignoring it", loadedClass, field, bundle);
                    continue;
                }

                final Field classField;
                try {
                    classField = loadedClass.getDeclaredField(field.fieldName);
                } catch (NoSuchFieldException | SecurityException e) {
                    LOG.warn("Failed to find field {} in {} of bundle {}", field.fieldName, loadedClass, bundle, e);
                    continue;
                }

                final Object fieldValue;
                try {
                    fieldValue = classField.get(null);
                } catch (IllegalAccessException | IllegalArgumentException | NullPointerException
                        | ExceptionInInitializerError e) {
                    LOG.warn("Failed to extract {} value in bundle {}, ignoring it", classField, bundle, e);
                    continue;
                }

                // No shenanigans around casting
                if (expectedType.isInstance(fieldValue)) {
                    fieldValues.put(field, expectedType.cast(fieldValue));
                } else {
                    LOG.warn("Ignoring {} in bundle {}: unexpected value {}", classField, bundle, fieldValue);
                }
            }
        }

        // Post-validation, do we have anything left?
        if (fieldValues.isEmpty()) {
            LOG.info("No intial configuration left in bundle {}", bundle);
            return null;
        }

        // Final indexing pass: Module class -> Data Object class -> DataObject
        final var modules = HashBasedTable.<Class<? extends DataRoot>, Class<? extends DataObject>, DataObject>create();
        for (var entry : fieldValues.entrySet()) {
            final var field = entry.getKey();
            final var type = typeToClass.get(field.typeName);
            if (type == null) {
                LOG.warn("Ignoring unresolved {} field {} in bundle {}", field.className, field.fieldName, bundle);
                continue;
            }
            final var module = typeToModule.get(type);
            if (module == null) {
                LOG.warn("Ignoring unrecognized {} in bundle {}", type, bundle);
                continue;
            }

            modules.put(module, type, entry.getValue());
        }

        return fieldValues.isEmpty() ? null : new BundleConfiguration(configService, bundle, modules);
    }
}
