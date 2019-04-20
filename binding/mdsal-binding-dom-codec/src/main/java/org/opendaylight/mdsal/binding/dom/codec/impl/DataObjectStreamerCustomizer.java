package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.impl.NodeCodecContext.CodecContextFactory;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader.Customizer;
import org.opendaylight.mdsal.binding.dom.codec.util.BindingSchemaMapping;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class DataObjectStreamerCustomizer implements Customizer {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectStreamerCustomizer.class);
    private static final String UNKNOWN_SIZE = BindingStreamEventWriter.class.getName() + ".UNKNOWN_SIZE";

    private final CodecContextFactory registry;
    private final DataNodeContainer schema;
    private final String startEvent;
    private final Class<?> type;

    DataObjectStreamerCustomizer(final CodecContextFactory registry, final DataNodeContainer schema,
            final Class<?> type, final String startEvent) {
        this.registry = requireNonNull(registry);
        this.schema = requireNonNull(schema);
        this.type = requireNonNull(type);
        this.startEvent = requireNonNull(startEvent);
    }

    public static DataObjectStreamerCustomizer create(
            final CodecContextFactory registry, final Class<?> type, final DataNodeContainer schema) {
        final String startEvent;
        if (schema instanceof ContainerSchemaNode || schema instanceof NotificationDefinition) {
            startEvent = "startContainerNode(" + type.getName() + ".class," + UNKNOWN_SIZE;
        } else if (schema instanceof ListSchemaNode) {
            final ListSchemaNode casted = (ListSchemaNode) schema;
            if (!casted.getKeyDefinition().isEmpty()) {
                startEvent = "startMapEntryNode($2." + BindingMapping.IDENTIFIABLE_KEY_NAME + "(), " + UNKNOWN_SIZE;
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

        return new DataObjectStreamerCustomizer(registry, schema, type, startEvent);
    }

    @Override
    public List<Class<?>> customize(final CodecClassLoader loader, final CtClass bindingClass, final CtClass generated)
            throws CannotCompileException, NotFoundException, IOException {

        // This results in a body
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("$3.").append(startEvent).append(");\n");

        final List<Class<?>> dependencies = emitChildren(sb);
        if (Augmentable.class.isAssignableFrom(type)) {
            sb.append("streamAugmentations($1, $3, $2);\n");
        }

        sb.append("$3.endNode();\n")
        .append('}');
        return dependencies;
    }

    private @NonNull List<Class<?>> emitChildren(final StringBuilder sb) {
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

                final Class<?> dependency = emitChild(sb, getterName, getter.getReturnType(), schemaChild);
                if (dependency != null) {
                    dependencies.add(dependency);
                }
            }
        }

        return dependencies;
    }

    private @Nullable Class<?> emitChild(final StringBuilder sb, final String getterName, final Class<?> returnType,
            final DataSchemaNode child) {
        if (child instanceof LeafSchemaNode) {
            sb.append("streamLeaf($3, \"").append(child.getQName().getLocalName()).append("\", $2.")
            .append(getterName).append("());\n");
            return null;
        }
        if (child instanceof ContainerSchemaNode) {
            final Class<?> dependency = registry.getDataObjectStreamer(returnType.asSubclass(DataObject.class))
                    .getClass();
            sb.append("streamContainer(").append(dependency.getName()).append(".getInstance(), $1, $3, $2.")
            .append(getterName).append("());\n");
            return dependency;
        }
        if (child instanceof ListSchemaNode) {
            final Class<?> valueType = singleTypeParameter(returnType);
            verify(DataObject.class.isAssignableFrom(valueType), "Value type %s of %s is not a DataObject", valueType,
                returnType);
            final Class<?> dependency = registry.getDataObjectStreamer(valueType.asSubclass(DataObject.class))
                    .getClass();
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

            sb.append('(').append(valueType.getName()).append(".class, ").append(dependency.getName())
            .append(".getInstance(), $1, $3, $2.").append(getterName).append("());\n");
            return dependency;
        }
        if (child instanceof AnyXmlSchemaNode) {
            sb.append("streamAnyxml($3, \"").append(child.getQName().getLocalName()).append("\", $2.")
            .append(getterName).append("\");\n");
            return null;
        }
        if (child instanceof LeafListSchemaNode) {
            sb.append("stream");
            if (((LeafListSchemaNode) child).isUserOrdered()) {
                sb.append("Ordered");
            }
            sb.append("LeafList($3, \"").append(child.getQName().getLocalName()).append("\", $2.")
            .append(getterName).append("());\n");
            return null;
        }
        if (child instanceof ChoiceSchemaNode) {
            sb.append("streamChoice(").append(returnType.getName()).append(".class, $1, $3, $2.").append(getterName)
            .append("());\n");
            return null;
        }

        LOG.debug("Ignoring {} due to unhandled schema {}", getterName, child);
        return null;
    }

    private static Class<?> singleTypeParameter(final Class<?> clazz) {
        final TypeVariable<?>[] params = clazz.getTypeParameters();
        verify(params.length == 1, "Unexpected parameters in %s", clazz);
        final java.lang.reflect.Type[] bounds = params[0].getBounds();
        verify(bounds.length == 1, "Unexpected parameter bounds in %s", clazz);
        final java.lang.reflect.Type bound = bounds[0];
        verify(bound instanceof Class, "Unexpected parameter bound %s in %s", bound, clazz);
        return (Class<?>) bound;
    }
}
