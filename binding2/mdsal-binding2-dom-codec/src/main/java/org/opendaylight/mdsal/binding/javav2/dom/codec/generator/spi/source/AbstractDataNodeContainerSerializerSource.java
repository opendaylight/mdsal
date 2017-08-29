/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.source;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.generator.AbstractGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer.ChoiceDispatchSerializer;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingSerializer;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerImplementation;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public abstract class AbstractDataNodeContainerSerializerSource extends AbstractTreeNodeSerializerSource {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataNodeContainerSerializerSource.class);

    protected static final String INPUT = "_input";
    private static final String CHOICE_PREFIX = "CHOICE_";

    private final DataNodeContainer schemaNode;
    private final GeneratedType dtoType;

    public AbstractDataNodeContainerSerializerSource(final AbstractGenerator generator, final GeneratedType type,
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
    public CharSequence getSerializerBody() {
        final StringBuilder b = new StringBuilder();
        b.append("{\n");
        b.append(statement(assign(TreeNodeSerializerRegistry.class.getName(), REGISTRY, "$1")));
        b.append(statement(assign(dtoType.getFullyQualifiedName(), INPUT,
                cast(dtoType.getFullyQualifiedName(), "$2"))));
        b.append(statement(assign(BindingStreamEventWriter.class.getName(), STREAM, cast(BindingStreamEventWriter.class.getName(), "$3"))));
        b.append(statement(assign(BindingSerializer.class.getName(), SERIALIZER, null)));
        b.append("if (");
        b.append(STREAM);
        b.append(" instanceof ");
        b.append(BindingSerializer.class.getName());
        b.append(") {");
        b.append(statement(assign(SERIALIZER, cast(BindingSerializer.class.getName(), STREAM))));
        b.append('}');
        b.append(statement(emitStartEvent()));

        emitBody(b);
        emitAfterBody(b);
        b.append(statement(endNode()));
        b.append(statement("return null"));
        b.append('}');
        return b;
    }

    /**
     * Allows for customization of emitting code, which is processed after
     * normal DataNodeContainer body. Ideal for augmentations or others.
     */
    protected void emitAfterBody(final StringBuilder b) {
    }

    private static Map<String, Type> collectAllProperties(final GeneratedType type, final Map<String, Type> hashMap) {
        for (final MethodSignature definition : type.getMethodDefinitions()) {
            hashMap.put(definition.getName(), definition.getReturnType());
        }
        return hashMap;
    }

    private static String getGetterName(final DataSchemaNode node) {
        final TypeDefinition<?> type;
        if (node instanceof TypedSchemaNode) {
            type = ((TypedSchemaNode) node).getType();
        } else {
            type = null;
        }

        final String prefix;
        if (type != null  && (type.getPath().equals(node.getPath()) || type.getBaseType() == null)
            && (type instanceof BooleanTypeDefinition || type instanceof EmptyTypeDefinition)) {
            prefix = "is";
        } else {
            prefix = "get";
        }
        return prefix + JavaIdentifierNormalizer.normalizeSpecificIdentifier(node.getQName().getLocalName(), JavaIdentifier.CLASS);
    }

    private boolean isLocalAugmentDataChild(final DataSchemaNode schemaChild) {
        if (schemaChild.isAugmenting()) {
            QName root = schemaChild.getPath().getPathFromRoot().iterator().next();
            return root.getModule().equals(schemaChild.getQName().getModule());
        }

        return false;
    }

    private void emitBody(final StringBuilder b) {
        final Map<String, Type> getterToType = collectAllProperties(dtoType, new HashMap<String, Type>());
        for (final DataSchemaNode schemaChild : schemaNode.getChildNodes()) {
            if (!schemaChild.isAugmenting() || isLocalAugmentDataChild(schemaChild)) {
                final String getter = getGetterName(schemaChild);
                final Type childType = getterToType.get(getter);
                if (childType == null) {
                    // FIXME AnyXml nodes are ignored, since their type cannot be found in generated bindnig
                    // Bug-706 https://bugs.opendaylight.org/show_bug.cgi?id=706
                    if (schemaChild instanceof AnyXmlSchemaNode) {
                        LOG.warn("Node {} will be ignored. AnyXml is not yet supported from binding aware code." +
                                "Binding Independent code can be used to serialize anyXml nodes.", schemaChild.getPath());
                        continue;
                    }

                    throw new IllegalStateException(
                        String.format("Unable to find type for child node %s. Expected child nodes: %s",
                            schemaChild.getPath(), getterToType));
                }
                emitChild(b, getter, childType, schemaChild);
            }
        }
    }

    private void emitChild(final StringBuilder b, final String getterName, final Type childType,
            final DataSchemaNode schemaChild) {
        b.append(statement(assign(childType, getterName, cast(childType, invoke(INPUT, getterName)))));

        b.append("if (").append(getterName).append(" != null) {\n");
        emitChildInner(b, getterName, childType, schemaChild);
        b.append("}\n");
    }

    private void emitChildInner(final StringBuilder b, final String getterName, final Type childType,
            final DataSchemaNode child) {
        if (child instanceof LeafSchemaNode) {
            b.append(statement(leafNode(child.getQName().getLocalName(), getterName)));
        } else if (child instanceof AnyXmlSchemaNode) {
            b.append(statement(anyxmlNode(child.getQName().getLocalName(), getterName)));
        } else if (child instanceof LeafListSchemaNode) {
            final CharSequence startEvent;
            if (((LeafListSchemaNode) child).isUserOrdered()) {
                startEvent = startOrderedLeafSet(child.getQName().getLocalName(),invoke(getterName, "size"));
            } else {
                startEvent = startLeafSet(child.getQName().getLocalName(),invoke(getterName, "size"));
            }
            b.append(statement(startEvent));
            final Type valueType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            b.append(forEach(getterName, valueType, statement(leafSetEntryNode(CURRENT))));
            b.append(statement(endNode()));
        } else if (child instanceof ListSchemaNode) {
            final Type valueType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            final ListSchemaNode casted = (ListSchemaNode) child;
            emitList(b, getterName, valueType, casted);
        } else if (child instanceof ContainerSchemaNode) {
            b.append(tryToUseCacheElse(getterName,statement(staticInvokeEmitter(childType, getterName))));
        } else if (child instanceof ChoiceSchemaNode) {
            final String propertyName = CHOICE_PREFIX + childType.getName();
            staticConstant(propertyName, TreeNodeSerializerImplementation.class, ChoiceDispatchSerializer.from(loadClass(childType)));
            b.append(tryToUseCacheElse(getterName,statement(invoke(propertyName, StreamWriterGenerator.SERIALIZE_METHOD_NAME, REGISTRY, cast(TreeNode.class.getName(),getterName), STREAM))));
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

    private void emitList(final StringBuilder b, final String getterName, final Type valueType,
            final ListSchemaNode child) {
        final CharSequence startEvent;

        b.append(statement(assign("int", "_count", invoke(getterName, "size"))));
        if (child.getKeyDefinition().isEmpty()) {
            startEvent = startUnkeyedList(classReference(valueType), "_count");
        } else if (child.isUserOrdered()) {
            startEvent = startOrderedMapNode(classReference(valueType), "_count");
        } else {
            startEvent = startMapNode(classReference(valueType), "_count");
        }
        b.append(statement(startEvent));
        b.append(forEach(getterName, valueType, tryToUseCacheElse(CURRENT,statement(staticInvokeEmitter(valueType, CURRENT)))));
        b.append(statement(endNode()));
    }
}
