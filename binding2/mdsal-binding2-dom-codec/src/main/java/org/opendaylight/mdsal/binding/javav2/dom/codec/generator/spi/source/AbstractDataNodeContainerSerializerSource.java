/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Collection;
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
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public abstract class AbstractDataNodeContainerSerializerSource extends AbstractTreeNodeSerializerSource {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataNodeContainerSerializerSource.class);

    protected static final String INPUT = "_input";
    private static final String CHOICE_PREFIX = "CHOICE_";

    //Note: the field takes no effects by augmentation.
    private final DataNodeContainer schemaNode;
    private final GeneratedType dtoType;

    public AbstractDataNodeContainerSerializerSource(final AbstractGenerator generator, final GeneratedType type,
            final DataNodeContainer node) {
        super(generator);
        this.dtoType = requireNonNull(type);
        this.schemaNode = requireNonNull(node);
    }

    /**
     * Return the character sequence which should be used for start event.
     *
     * @return Start event character sequence
     */
    protected abstract CharSequence emitStartEvent();

    @Override
    public CharSequence getSerializerBody() {
        final StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append(statement(assign(TreeNodeSerializerRegistry.class.getName(), REGISTRY, "$1")));
        builder.append(statement(assign(dtoType.getFullyQualifiedName(), INPUT,
                cast(dtoType.getFullyQualifiedName(), "$2"))));
        builder.append(statement(assign(BindingStreamEventWriter.class.getName(), STREAM,
            cast(BindingStreamEventWriter.class.getName(), "$3"))));
        builder.append(statement(assign(BindingSerializer.class.getName(), SERIALIZER, null)));
        builder.append("if (");
        builder.append(STREAM);
        builder.append(" instanceof ");
        builder.append(BindingSerializer.class.getName());
        builder.append(") {");
        builder.append(statement(assign(SERIALIZER, cast(BindingSerializer.class.getName(), STREAM))));
        builder.append('}');
        builder.append(statement(emitStartEvent()));

        emitBody(builder);
        emitAfterBody(builder);
        builder.append(statement(endNode()));
        builder.append(statement("return null"));
        builder.append('}');
        return builder;
    }

    /**
     * Allows for customization of emitting code, which is processed after
     * normal DataNodeContainer body. Ideal for augmentations or others.
     */
    protected void emitAfterBody(final StringBuilder builder) {
    }

    private static Map<String, Type> collectAllProperties(final GeneratedType type, final Map<String, Type> hashMap) {
        for (final MethodSignature definition : type.getMethodDefinitions()) {
            hashMap.put(definition.getName(), definition.getReturnType());
        }

       /**
        * According to binding v2 spec., uses nodes are processed as-if they are direct children
        * of parent node, so we can't get properties from implements any more. Uses nodes are processed by invoking
        * {@link org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil#resolveDataSchemaNodes()} in which
        * {@link org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil#resolveDataSchemaNodesCheck()}
        * allows them to be resolved.
        */

        return hashMap;
    }

    private static String getGetterName(final DataSchemaNode node) {
        final TypeDefinition<?> type;
        if (node instanceof TypedDataSchemaNode) {
            type = ((TypedDataSchemaNode) node).getType();
        } else {
            type = null;
        }

        final String prefix;
        // Bug 8903: If it is a derived type of boolean, not a built-in type, then the return type
        // of method would be the generated type and the prefix should be 'get'.
        if (type instanceof BooleanTypeDefinition
                && (type.getPath().equals(node.getPath()) || type.getBaseType() == null)) {
            prefix = "is";
        } else {
            prefix = "get";
        }
        return prefix + JavaIdentifierNormalizer.normalizeSpecificIdentifier(node.getQName().getLocalName(),
            JavaIdentifier.CLASS);
    }

    private boolean emitCheck(final DataSchemaNode schemaChild) {
        if (schemaChild.isAugmenting()) {
            QName root = schemaChild.getPath().getPathFromRoot().iterator().next();
            return root.getModule().equals(schemaChild.getQName().getModule());
        }

        return true;
    }

    /**
     * Note: the method would be overrided by {@link AbstractAugmentSerializerSource#getChildNodes()},
     * since all augmentation schema nodes of same target would be grouped into sort of one node,
     * so call {@link AbstractAugmentSerializerSource#getChildNodes()} to get all these children
     * nodes of same target augmentation schemas.
     */
    protected Collection<DataSchemaNode> getChildNodes() {
        return schemaNode.getChildNodes();
    }

    private void emitBody(final StringBuilder builder) {
        final Map<String, Type> getterToType = collectAllProperties(dtoType, new HashMap<String, Type>());
        for (final DataSchemaNode schemaChild : getChildNodes()) {
            /**
             * As before, it only emitted data nodes which were not added by uses or augment, now
             * according to binding v2 specification, augment of the same module is same as inlining,
             * all data node children should be processed as-if they were directly defined inside
             * target node.
             */
            if (emitCheck(schemaChild)) {
                final String getter = getGetterName(schemaChild);
                final Type childType = getterToType.get(getter);
                if (childType == null) {
                    // FIXME AnyXml nodes are ignored, since their type cannot be found in generated bindnig
                    // Bug-706 https://bugs.opendaylight.org/show_bug.cgi?id=706
                    if (schemaChild instanceof AnyXmlSchemaNode) {
                        LOG.warn("Node {} will be ignored. AnyXml is not yet supported from binding aware code."
                            + "Binding Independent code can be used to serialize anyXml nodes.", schemaChild.getPath());
                        continue;
                    }

                    throw new IllegalStateException(
                        String.format("Unable to find type for child node %s. Expected child nodes: %s",
                            schemaChild.getPath(), getterToType));
                }
                emitChild(builder, getter, childType, schemaChild);
            }
        }
    }

    private void emitChild(final StringBuilder builder, final String getterName, final Type childType,
            final DataSchemaNode schemaChild) {
        builder.append(statement(assign(childType, getterName, cast(childType, invoke(INPUT, getterName)))));

        builder.append("if (").append(getterName).append(" != null) {\n");
        emitChildInner(builder, getterName, childType, schemaChild);
        builder.append("}\n");
    }

    private void emitChildInner(final StringBuilder builder, final String getterName, final Type childType,
            final DataSchemaNode child) {
        if (child instanceof LeafSchemaNode) {
            builder.append(statement(leafNode(child.getQName().getLocalName(), getterName)));
        } else if (child instanceof AnyXmlSchemaNode) {
            builder.append(statement(anyxmlNode(child.getQName().getLocalName(), getterName)));
        } else if (child instanceof LeafListSchemaNode) {
            final CharSequence startEvent;
            if (((LeafListSchemaNode) child).isUserOrdered()) {
                startEvent = startOrderedLeafSet(child.getQName().getLocalName(),
                    invoke(getterName, "size"));
            } else {
                startEvent = startLeafSet(child.getQName().getLocalName(),invoke(getterName, "size"));
            }
            builder.append(statement(startEvent));
            final Type valueType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            builder.append(forEach(getterName, valueType, statement(leafSetEntryNode(CURRENT))));
            builder.append(statement(endNode()));
        } else if (child instanceof ListSchemaNode) {
            final Type valueType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            final ListSchemaNode casted = (ListSchemaNode) child;
            emitList(builder, getterName, valueType, casted);
        } else if (child instanceof ContainerSchemaNode) {
            builder.append(tryToUseCacheElse(getterName,statement(staticInvokeEmitter(childType, getterName))));
        } else if (child instanceof ChoiceSchemaNode) {
            final String propertyName = CHOICE_PREFIX + childType.getName();
            staticConstant(propertyName, TreeNodeSerializerImplementation.class,
                ChoiceDispatchSerializer.from(loadClass(childType)));
            builder.append(tryToUseCacheElse(getterName,statement(invoke(propertyName,
                StreamWriterGenerator.SERIALIZE_METHOD_NAME, REGISTRY, cast(TreeNode.class.getName(),getterName),
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

    private void emitList(final StringBuilder builer, final String getterName, final Type valueType,
            final ListSchemaNode child) {
        final CharSequence startEvent;

        builer.append(statement(assign("int", "_count", invoke(getterName, "size"))));
        if (child.getKeyDefinition().isEmpty()) {
            startEvent = startUnkeyedList(classReference(valueType), "_count");
        } else if (child.isUserOrdered()) {
            startEvent = startOrderedMapNode(classReference(valueType), "_count");
        } else {
            startEvent = startMapNode(classReference(valueType), "_count");
        }
        builer.append(statement(startEvent));
        builer.append(forEach(getterName, valueType, tryToUseCacheElse(CURRENT,
            statement(staticInvokeEmitter(valueType, CURRENT)))));
        builer.append(statement(endNode()));
    }
}
