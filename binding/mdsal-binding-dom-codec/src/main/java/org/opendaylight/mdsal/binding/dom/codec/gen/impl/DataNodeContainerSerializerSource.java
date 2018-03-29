/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.dom.codec.gen.impl;

import static org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext.referencedType;

import com.google.common.base.Preconditions;
import java.lang.reflect.Method;
import java.util.List;
import org.opendaylight.mdsal.binding.dom.codec.util.ChoiceDispatchSerializer;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.BindingSerializer;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class DataNodeContainerSerializerSource extends DataObjectSerializerSource {

    private static final Logger LOG = LoggerFactory.getLogger(DataNodeContainerSerializerSource.class);

    protected static final String INPUT = "_input";
    private static final String CHOICE_PREFIX = "CHOICE_";

    protected final DataNodeContainer schemaNode;
    private final Type dtoType;

    DataNodeContainerSerializerSource(final AbstractGenerator generator, final Type type,
            final DataNodeContainer node) {
        super(generator);
        this.dtoType = Preconditions.checkNotNull(type);
        this.schemaNode = Preconditions.checkNotNull(node);
    }

    /**
     * Return the character sequence which should be used for start event.
     *
     * @return Start event character sequence
     */
    protected abstract CharSequence emitStartEvent();

    @Override
    protected CharSequence getSerializerBody() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append(statement(assign(DataObjectSerializerRegistry.class.getName(), REGISTRY, "$1")));
        sb.append(statement(assign(dtoType.getFullyQualifiedName(), INPUT,
                cast(dtoType.getFullyQualifiedName(), "$2"))));
        sb.append(statement(assign(BindingStreamEventWriter.class.getName(), STREAM,
            cast(BindingStreamEventWriter.class.getName(), "$3"))));
        sb.append(statement(assign(BindingSerializer.class.getName(), SERIALIZER, null)));
        sb.append("if (");
        sb.append(STREAM);
        sb.append(" instanceof ");
        sb.append(BindingSerializer.class.getName());
        sb.append(") {");
        sb.append(statement(assign(SERIALIZER, cast(BindingSerializer.class.getName(), STREAM))));
        sb.append('}');
        sb.append(statement(emitStartEvent()));

        emitBody(sb);
        emitAfterBody(sb);
        sb.append(statement(endNode()));
        sb.append(statement("return null"));
        sb.append('}');
        return sb;
    }

    /**
     * Allows for customization of emitting code, which is processed after
     * normal DataNodeContainer body. Ideal for augmentations or others.
     */
    protected void emitAfterBody(final StringBuilder sb) {
        // No-op
    }

    private static String getGetterName(final DataSchemaNode node) {
        final TypeDefinition<?> type;
        if (node instanceof TypedDataSchemaNode) {
            type = ((TypedDataSchemaNode) node).getType();
        } else {
            type = null;
        }

        final String prefix;
        // Bug 8903: If it is a derived type of boolean or empty, not an inner type, then the return type
        // of method would be the generated type of typedef not build-in types, so here it should be 'get'.
        if ((type instanceof BooleanTypeDefinition || type instanceof EmptyTypeDefinition)
                && (type.getPath().equals(node.getPath()) || type.getBaseType() == null)) {
            prefix = "is";
        } else {
            prefix = "get";
        }
        return prefix + BindingMapping.getGetterSuffix(node.getQName());
    }

    private void emitBody(final StringBuilder sb) {
        final Class<?> typeClazz;
        try {
            typeClazz = ClassLoaderUtils.loadClass(
                DataNodeContainerSerializerSource.class.getClassLoader(), dtoType.getFullyQualifiedName());
        } catch (final ClassNotFoundException exception) {
            throw new IllegalArgumentException("Can not find class" + dtoType, exception);
        }

        for (final DataSchemaNode schemaChild : schemaNode.getChildNodes()) {
            if (!schemaChild.isAugmenting()) {
                final String getter = getGetterName(schemaChild);
                final Type returnType;
                final Type childType;
                try {
                    final Method getterMethod = typeClazz.getMethod(getter);
                    //Use 'getName' string to take nested classes into consideration.
                    returnType = referencedType(getterMethod.getReturnType());
                    if (List.class.isAssignableFrom(getterMethod.getReturnType())) {
                        childType = referencedType((Class<? extends DataObject>) ClassLoaderUtils
                            .getFirstGenericParameter(getterMethod.getGenericReturnType()));
                    } else {
                        childType = returnType;
                    }
                } catch (NoSuchMethodException exp) {
                    // FIXME AnyXml nodes are ignored, since their type cannot be found in generated bindnig
                    // Bug-706 https://bugs.opendaylight.org/show_bug.cgi?id=706
                    if (schemaChild instanceof AnyXmlSchemaNode) {
                        LOG.warn("Node {} will be ignored. AnyXml is not yet supported from binding aware code."
                            + "Binding Independent code can be used to serialize anyXml nodes.", schemaChild.getPath());
                        continue;
                    }

                    throw new IllegalStateException(
                        String.format("Unable to find getter method %s for child node %s",
                            getter, schemaChild.getPath()), exp);
                }

                emitChild(sb, getter, returnType, childType, schemaChild);
            }
        }
    }

    private void emitChild(final StringBuilder sb, final String getterName, final Type returnType, final Type childType,
            final DataSchemaNode schemaChild) {
        sb.append(statement(assign(returnType, getterName, cast(returnType, invoke(INPUT, getterName)))));

        sb.append("if (").append(getterName).append(" != null) {\n");
        emitChildInner(sb, getterName, childType, schemaChild);
        sb.append("}\n");
    }

    private void emitChildInner(final StringBuilder sb, final String getterName, final Type childType,
            final DataSchemaNode child) {
        if (child instanceof LeafSchemaNode) {
            sb.append(statement(leafNode(child.getQName().getLocalName(), getterName)));
        } else if (child instanceof AnyXmlSchemaNode) {
            sb.append(statement(anyxmlNode(child.getQName().getLocalName(), getterName)));
        } else if (child instanceof LeafListSchemaNode) {
            final CharSequence startEvent;
            if (((LeafListSchemaNode) child).isUserOrdered()) {
                startEvent = startOrderedLeafSet(child.getQName().getLocalName(),invoke(getterName, "size"));
            } else {
                startEvent = startLeafSet(child.getQName().getLocalName(),invoke(getterName, "size"));
            }
            sb.append(statement(startEvent));
            sb.append(forEach(getterName, childType, statement(leafSetEntryNode(CURRENT))));
            sb.append(statement(endNode()));
        } else if (child instanceof ListSchemaNode) {
            final ListSchemaNode casted = (ListSchemaNode) child;
            emitList(sb, getterName, childType, casted);
        } else if (child instanceof ContainerSchemaNode) {
            sb.append(tryToUseCacheElse(getterName,statement(staticInvokeEmitter(childType, getterName))));
        } else if (child instanceof ChoiceSchemaNode) {
            final String propertyName = CHOICE_PREFIX + childType.getName();
            staticConstant(propertyName, DataObjectSerializerImplementation.class,
                ChoiceDispatchSerializer.from(loadClass(childType)));
            sb.append(tryToUseCacheElse(getterName,statement(invoke(propertyName,
                StreamWriterGenerator.SERIALIZE_METHOD_NAME, REGISTRY, cast(DataObject.class.getName(), getterName),
                STREAM))));
        }
    }

    private static StringBuilder tryToUseCacheElse(final String getterName, final CharSequence statement) {
        final StringBuilder b = new StringBuilder();
        b.append("if ( ");
        b.append(SERIALIZER).append("== null || ");
        b.append(invoke(SERIALIZER, "serialize", getterName)).append("== null");
        b.append(") {");
        b.append(statement);
        b.append('}');
        return b;
    }

    private void emitList(final StringBuilder sb, final String getterName, final Type valueType,
            final ListSchemaNode child) {
        final CharSequence startEvent;

        sb.append(statement(assign("int", "_count", invoke(getterName, "size"))));
        if (child.getKeyDefinition().isEmpty()) {
            startEvent = startUnkeyedList(classReference(valueType), "_count");
        } else if (child.isUserOrdered()) {
            startEvent = startOrderedMapNode(classReference(valueType), "_count");
        } else {
            startEvent = startMapNode(classReference(valueType), "_count");
        }
        sb.append(statement(startEvent));
        sb.append(forEach(getterName, valueType, tryToUseCacheElse(CURRENT, statement(staticInvokeEmitter(valueType,
            CURRENT)))));
        sb.append(statement(endNode()));
    }
}
