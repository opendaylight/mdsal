/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import net.bytebuddy.dynamic.DynamicType.Builder;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.impl.NodeCodecContext.CodecContextFactory;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader.ByteBuddyCustomizer;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader.ByteBuddyResult;
import org.opendaylight.mdsal.binding.dom.codec.loader.StaticClassPool;
import org.opendaylight.mdsal.binding.dom.codec.util.BindingSchemaMapping;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class DataObjectStreamerCustomizer<T extends DataObjectStreamer<?>> implements ByteBuddyCustomizer<T> {
    static final CtClass CT_DOS = StaticClassPool.findClass(DataObjectStreamer.class);
    static final String INSTANCE_FIELD = "INSTANCE";

    private static final Logger LOG = LoggerFactory.getLogger(DataObjectStreamerCustomizer.class);
    private static final String UNKNOWN_SIZE = BindingStreamEventWriter.class.getName() + ".UNKNOWN_SIZE";

    private static final CtClass CT_VOID = StaticClassPool.findClass(void.class);
    private static final CtClass[] SERIALIZE_ARGS = new CtClass[] {
        StaticClassPool.findClass(DataObjectSerializerRegistry.class),
        StaticClassPool.findClass(DataObject.class),
        StaticClassPool.findClass(BindingStreamEventWriter.class)
    };

    private final Map<String, Class<? extends DataObject>> constants = new HashMap<>();
    private final ImmutableMap<String, Type> props;
    private final CodecContextFactory registry;
    private final DataNodeContainer schema;
    private final String startEvent;
    private final Class<?> type;

    DataObjectStreamerCustomizer(final CodecContextFactory registry, final GeneratedType genType,
            final DataNodeContainer schema, final Class<?> type, final String startEvent) {
        this.registry = requireNonNull(registry);
        this.schema = requireNonNull(schema);
        this.type = requireNonNull(type);
        this.startEvent = requireNonNull(startEvent);
        props = collectAllProperties(genType);
    }

    public static DataObjectStreamerCustomizer create(final CodecContextFactory registry, final Class<?> type) {
        final Entry<GeneratedType, WithStatus> typeAndSchema = registry.getRuntimeContext().getTypeWithSchema(type);
        final WithStatus schema = typeAndSchema.getValue();

        final String startEvent;
        if (schema instanceof ContainerSchemaNode || schema instanceof NotificationDefinition) {
            startEvent = "startContainerNode(" + type.getName() + ".class," + UNKNOWN_SIZE;
        } else if (schema instanceof ListSchemaNode) {
            final ListSchemaNode casted = (ListSchemaNode) schema;
            if (!casted.getKeyDefinition().isEmpty()) {
                startEvent = "startMapEntryNode(obj." + BindingMapping.IDENTIFIABLE_KEY_NAME + "(), " + UNKNOWN_SIZE;
            } else {
                startEvent = "startUnkeyedListItem(" + UNKNOWN_SIZE;
            }
        } else if (schema instanceof AugmentationSchemaNode) {
            startEvent = "startAugmentationNode(" + type.getName() + ".class";
        } else if (schema instanceof CaseSchemaNode) {
            startEvent = "startCase(" + type.getName() + ".class, " + UNKNOWN_SIZE;
        } else {
            throw new UnsupportedOperationException("Schema type " + schema.getClass() + " is not supported");
        }

        return new DataObjectStreamerCustomizer(registry, typeAndSchema.getKey(), (DataNodeContainer) schema, type,
            startEvent);
    }


    @Override
    public ByteBuddyResult<? extends T> customize(final CodecClassLoader loader, final Class<?> bindingInterface,
            final String fqn, final Builder<? extends T> builder) {
        LOG.info("Definining streamer {}", fqn);



        final CtField instanceField = new CtField(generated, INSTANCE_FIELD, generated);
        instanceField.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
        generated.addField(instanceField, "new " + generated.getName() + "()");

        // This results in a body
        final String objType = bindingClass.getName();
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("final ").append(objType).append(" obj = (").append(objType).append(") $2;\n")
                .append("$3.").append(startEvent).append(");\n");

        final List<Class<?>> dependencies = emitChildren(sb, loader, generated);
        if (Augmentable.class.isAssignableFrom(type)) {
            sb.append("streamAugmentations($1, $3, obj);\n");
        }

        sb.append("$3.endNode();\n")
        .append('}');

        final CtMethod serialize = new CtMethod(CT_VOID, "serialize", SERIALIZE_ARGS, generated);
        serialize.setModifiers(Modifier.PUBLIC);
        serialize.setBody(sb.toString());
        generated.addMethod(serialize);

        generated.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        LOG.info("Definition of {} done", generated.getName());

        return dependencies;
    }

    private List<Class<?>> emitChildren(final StringBuilder sb, final CodecClassLoader loader,
            final CtClass generated) throws CannotCompileException {
        final List<Class<?>> dependencies = new ArrayList<>();

        for (final DataSchemaNode schemaChild : schema.getChildNodes()) {
            if (!schemaChild.isAugmenting()) {
                final String getterName = BindingSchemaMapping.getGetterMethodName(schemaChild);
                final Method getter;
                try {
                    getter = type.getMethod(getterName);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Failed to find getter " + getterName, e);
                }

                final Class<?> dependency = emitChild(sb, loader, generated, getterName, getter.getReturnType(),
                    schemaChild);
                if (dependency != null) {
                    LOG.info("Require dependency {}", dependency);
                    dependencies.add(dependency);
                }
            }
        }

        return dependencies;
    }

    private @Nullable Class<?> emitChild(final StringBuilder sb, final CodecClassLoader loader, final CtClass generated,
            final String getterName, final Class<?> returnType, final DataSchemaNode child)
                    throws CannotCompileException {
        if (child instanceof LeafSchemaNode) {
            sb.append("streamLeaf($3, \"").append(child.getQName().getLocalName()).append("\", obj.")
            .append(getterName).append("());\n");
            return null;
        }
        if (child instanceof ContainerSchemaNode) {
            final Class<? extends DataObject> itemClass = returnType.asSubclass(DataObject.class);
            final String constField = declareDependency(generated, getterName, itemClass);

            sb.append("streamContainer(").append(constField).append(", $1, $3, obj.").append(getterName)
            .append("());\n");
            return registry.getDataObjectSerializer(itemClass).getClass();
        }
        if (child instanceof ListSchemaNode) {
            final Type childType = props.get(getterName);
            verify(childType instanceof ParameterizedType, "Unexpected type %s for %s", childType, getterName);
            final Type valueType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            final Class<?> valueClass;
            try {
                valueClass = loader.loadClass(valueType.getFullyQualifiedName());
            } catch (ClassNotFoundException e) {
                throw new LinkageError("Failed to load " + valueType, e);
            }

            verify(DataObject.class.isAssignableFrom(valueClass), "Value type %s of %s is not a DataObject", valueClass,
                returnType);
            final Class<? extends DataObject> itemClass = valueClass.asSubclass(DataObject.class);
            final ListSchemaNode casted = (ListSchemaNode) child;

            sb.append("stream");
            if (casted.getKeyDefinition().isEmpty()) {
                sb.append("List");
            } else {
                if (casted.isUserOrdered()) {
                    sb.append("Ordered");
                }
                sb.append("Map");
            }

            final String constField = declareDependency(generated, getterName, itemClass);
            sb.append('(').append(valueClass.getName()).append(".class, ").append(constField).append(", $1, $3, obj.")
            .append(getterName).append("());\n");
            return registry.getDataObjectSerializer(itemClass).getClass();
        }
        if (child instanceof AnyXmlSchemaNode) {
            sb.append("streamAnyxml($3, \"").append(child.getQName().getLocalName()).append("\", obj.")
            .append(getterName).append("());\n");
            return null;
        }
        if (child instanceof LeafListSchemaNode) {
            sb.append("stream");
            if (((LeafListSchemaNode) child).isUserOrdered()) {
                sb.append("Ordered");
            }
            sb.append("LeafList($3, \"").append(child.getQName().getLocalName()).append("\", obj.")
            .append(getterName).append("());\n");
            return null;
        }
        if (child instanceof ChoiceSchemaNode) {
            sb.append("streamChoice(").append(returnType.getName()).append(".class, $1, $3, obj.").append(getterName)
            .append("());\n");
            return null;
        }

        LOG.debug("Ignoring {} due to unhandled schema {}", getterName, child);
        return null;
    }

    private static ImmutableMap<String, Type> collectAllProperties(final GeneratedType type) {
        final Map<String, Type> props = new HashMap<>();
        collectAllProperties(type, props);
        return ImmutableMap.copyOf(props);
    }

    private static void collectAllProperties(final GeneratedType type, final Map<String, Type> hashMap) {
        for (final MethodSignature definition : type.getMethodDefinitions()) {
            hashMap.put(definition.getName(), definition.getReturnType());
        }
        for (final Type parent : type.getImplements()) {
            if (parent instanceof GeneratedType) {
                collectAllProperties((GeneratedType) parent, hashMap);
            }
        }
    }
}
