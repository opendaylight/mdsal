/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.blueprint.common;

import static org.opendaylight.mdsal.blueprint.common.BlueprintConstants.ID_ATTR;

import com.google.common.annotations.Beta;
import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.aries.blueprint.mutable.MutableRefMetadata;
import org.apache.aries.blueprint.mutable.MutableReferenceMetadata;
import org.apache.aries.blueprint.mutable.MutableValueMetadata;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.RefMetadata;
import org.osgi.service.blueprint.reflect.ReferenceMetadata;
import org.osgi.service.blueprint.reflect.ValueMetadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Beta
public final class NamespaceHandlerUtils {
    private NamespaceHandlerUtils() {

    }

    public static void addBlueprintBundleRefProperty(final ParserContext context, final MutableBeanMetadata metadata) {
        metadata.addProperty("bundle", createRef(context, "blueprintBundle"));
    }

    public static MutableBeanMetadata createBeanMetadata(final ParserContext context, final String id,
            final Class<?> runtimeClass, final boolean initMethod, final boolean destroyMethod) {
        MutableBeanMetadata metadata = context.createMetadata(MutableBeanMetadata.class);
        metadata.setId(id);
        metadata.setScope(BeanMetadata.SCOPE_SINGLETON);
        metadata.setActivation(ReferenceMetadata.ACTIVATION_EAGER);
        metadata.setRuntimeClass(runtimeClass);

        if (initMethod) {
            metadata.setInitMethod("init");
        }

        if (destroyMethod) {
            metadata.setDestroyMethod("destroy");
        }

        return metadata;
    }

    public static RefMetadata createRef(final ParserContext context, final String id) {
        MutableRefMetadata metadata = context.createMetadata(MutableRefMetadata.class);
        metadata.setComponentId(id);
        return metadata;
    }

    public static MutableReferenceMetadata createServiceRef(final ParserContext context, final Class<?> cls,
            final String filter) {
        MutableReferenceMetadata metadata = context.createMetadata(MutableReferenceMetadata.class);
        metadata.setRuntimeInterface(cls);
        metadata.setInterface(cls.getName());
        metadata.setActivation(ReferenceMetadata.ACTIVATION_EAGER);
        metadata.setAvailability(ReferenceMetadata.AVAILABILITY_MANDATORY);

        if (filter != null) {
            metadata.setFilter(filter);
        }

        return metadata;
    }

    public static ValueMetadata createValue(final ParserContext context, final String value) {
        MutableValueMetadata metadata = context.createMetadata(MutableValueMetadata.class);
        metadata.setStringValue(value);
        return metadata;
    }

    public static String getId(final ParserContext context, final Element element) {
        return element.hasAttribute(ID_ATTR) ? element.getAttribute(ID_ATTR) : context.generateId();
    }

    public static boolean nodeNameEquals(final Node node, final String name) {
        return name.equals(node.getNodeName()) || name.equals(node.getLocalName());
    }
}

