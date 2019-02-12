/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.generator;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map.Entry;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.api.TreeNodeSerializerGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StaticBindingProperty;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.TreeNodeSerializerPrototype;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.source.AbstractTreeNodeSerializerSource;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer.AugmentableDispatchSerializer;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.runtime.context.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerImplementation;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerRegistry;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public abstract class AbstractStreamWriterGenerator extends AbstractGenerator implements TreeNodeSerializerGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractStreamWriterGenerator.class);

    public static final String SERIALIZE_METHOD_NAME = "serialize";
    public static final AugmentableDispatchSerializer AUGMENTABLE = new AugmentableDispatchSerializer();
    private static final Field FIELD_MODIFIERS;

    private final LoadingCache<Class<?>, TreeNodeSerializerImplementation> implementations;
    private final CtClass[] serializeArguments;
    private final JavassistUtils javassist;

    private BindingRuntimeContext context;

    static {
        /*
         * Cache reflection access to field modifiers field. We need this to set
         * fix the static declared fields to final once we initialize them. If
         * we cannot get access, that's fine, too.
         */
        Field field = null;
        try {
            field = Field.class.getDeclaredField("modifiers");
            field.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            LOG.warn("Could not get Field modifiers field, serializers run at decreased efficiency", e);
        }

        FIELD_MODIFIERS = field;
    }

    protected AbstractStreamWriterGenerator(final JavassistUtils utils) {
        this.javassist = requireNonNull(utils, "JavassistUtils instance is required.");
        this.serializeArguments = new CtClass[] { javassist.asCtClass(TreeNodeSerializerRegistry.class),
                javassist.asCtClass(TreeNode.class), javassist.asCtClass(BindingStreamEventWriter.class), };
        javassist.appendClassLoaderIfMissing(TreeNodeSerializerPrototype.class.getClassLoader());
        this.implementations = CacheBuilder.newBuilder().weakKeys().build(new SerializerImplementationLoader());
    }

    @Override
    public final TreeNodeSerializerImplementation getSerializer(final Class<?> type) {
        return implementations.getUnchecked(type);
    }

    @Override
    public final void onBindingRuntimeContextUpdated(final BindingRuntimeContext runtime) {
        this.context = runtime;
    }

    @Override
    public final String loadSerializerFor(final Class<?> cls) {
        return implementations.getUnchecked(cls).getClass().getName();
    }

    private final class SerializerImplementationLoader extends CacheLoader<Class<?>, TreeNodeSerializerImplementation> {

        private static final String GETINSTANCE_METHOD_NAME = "getInstance";
        private static final String SERIALIZER_SUFFIX = "$StreamWriter";

        private String getSerializerName(final Class<?> type) {
            return type.getName() + SERIALIZER_SUFFIX;
        }

        @Override
        @SuppressWarnings("unchecked")
        public TreeNodeSerializerImplementation load(@Nonnull final Class<?> type) throws Exception {
            Preconditions.checkArgument(BindingReflections.isBindingClass(type));
            Preconditions.checkArgument(Instantiable.class.isAssignableFrom(type),
                    "Instantiable is not assingnable from %s from classloader %s.", type, type.getClassLoader());

            final String serializerName = getSerializerName(type);

            Class<? extends TreeNodeSerializerImplementation> cls;
            try {
                cls = (Class<? extends TreeNodeSerializerImplementation>) ClassLoaderUtils
                        .loadClass(type.getClassLoader(), serializerName);
            } catch (final ClassNotFoundException e) {
                cls = generateSerializer(type, serializerName);
            }

            final TreeNodeSerializerImplementation obj =
                    (TreeNodeSerializerImplementation) cls.getDeclaredMethod(GETINSTANCE_METHOD_NAME).invoke(null);
            LOG.debug("Loaded serializer {} for class {}", obj, type);
            return obj;
        }

        private Class<? extends TreeNodeSerializerImplementation> generateSerializer(final Class<?> type,
                final String serializerName)
                throws CannotCompileException, IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
            final AbstractTreeNodeSerializerSource source = generateEmitterSource(type, serializerName);
            final CtClass poolClass = generateEmitter0(type, source, serializerName);
            final Class<? extends TreeNodeSerializerImplementation> cls =
                    poolClass.toClass(type.getClassLoader(), type.getProtectionDomain())
                    .asSubclass(TreeNodeSerializerImplementation.class);

            /*
             * Due to OSGi class loader rules we cannot initialize the fields
             * during construction, as the initializer expressions do not see
             * our implementation classes. This should be almost as good as
             * that, as we are resetting the fields to final before ever leaking
             * the class.
             */
            for (final StaticBindingProperty constant : source.getStaticConstants()) {
                final Field field = cls.getDeclaredField(constant.getName());
                field.setAccessible(true);
                field.set(null, constant.getValue());

                if (FIELD_MODIFIERS != null) {
                    FIELD_MODIFIERS.setInt(field, field.getModifiers() | Modifier.FINAL);
                }
            }

            return cls;
        }
    }

    private AbstractTreeNodeSerializerSource generateEmitterSource(final Class<?> type, final String serializerName) {
        Types.typeForClass(type);
        javassist.appendClassLoaderIfMissing(type.getClassLoader());

        if (Augmentation.class.isAssignableFrom(type)) {
            final Entry<Type, Collection<AugmentationSchemaNode>> entry = context.getAugmentationDefinition(type);
            return generateAugmentSerializer(((GeneratedTypeBuilder) entry.getKey()).toInstance(), entry.getValue());
        }

        final Entry<GeneratedType, Object> typeWithSchema = context.getTypeWithSchema(type);
        final GeneratedType generatedType = typeWithSchema.getKey();
        final Object schema = typeWithSchema.getValue();

        final AbstractTreeNodeSerializerSource source;
        if (schema instanceof ContainerSchemaNode) {
            source = generateContainerSerializer(generatedType, (ContainerSchemaNode) schema);
        } else if (schema instanceof ListSchemaNode) {
            final ListSchemaNode casted = (ListSchemaNode) schema;
            if (casted.getKeyDefinition().isEmpty()) {
                source = generateUnkeyedListEntrySerializer(generatedType, casted);
            } else {
                source = generateMapEntrySerializer(generatedType, casted);
            }
        } else if (schema instanceof CaseSchemaNode) {
            source = generateCaseSerializer(generatedType, (CaseSchemaNode) schema);
        } else if (schema instanceof NotificationDefinition) {
            source = generateNotificationSerializer(generatedType, (NotificationDefinition) schema);
        } else {
            throw new UnsupportedOperationException("Schema type " + schema.getClass() + " is not supported");
        }
        return source;
    }

    private CtClass generateEmitter0(final Class<?> type, final AbstractTreeNodeSerializerSource source,
            final String serializerName) {
        final CtClass product;

        /*
         * getSerializerBody() has side effects, such as loading classes and
         * codecs, it should be run in model class loader in order to correctly
         * reference load child classes.
         *
         * Furthermore the fact that getSerializedBody() can trigger other code
         * generation to happen, we need to take care of this before calling
         * instantiatePrototype(), as that will call our customizer with the
         * lock held, hence any code generation will end up being blocked on the
         * javassist lock.
         */
        final String body = ClassLoaderUtils.getWithClassLoader(type.getClassLoader(),
                source.getSerializerBody()::toString);

        try {
            product = javassist.instantiatePrototype(TreeNodeSerializerPrototype.class.getName(), serializerName,
                cls -> {
                    // Generate any static fields
                    for (final StaticBindingProperty def : source.getStaticConstants()) {
                        final CtField field = new CtField(javassist.asCtClass(def.getType()), def.getName(), cls);
                        field.setModifiers(Modifier.PRIVATE + Modifier.STATIC);
                        cls.addField(field);
                    }

                    // Replace serialize() -- may reference static fields
                    final CtMethod serializeTo = cls.getDeclaredMethod(SERIALIZE_METHOD_NAME, serializeArguments);
                    serializeTo.setBody(body);

                    // The prototype is not visible, so we need to take care
                    // of that
                    cls.setModifiers(Modifier.setPublic(cls.getModifiers()));
                });
        } catch (final NotFoundException e) {
            LOG.error("Failed to instatiate serializer {}", source, e);
            throw new LinkageError("Unexpected instantation problem: serializer prototype not found", e);
        }
        return product;
    }

    /**
     * Generates serializer source code for supplied container node, which will
     * read supplied binding type and invoke proper methods on supplied
     * {@link BindingStreamEventWriter}.
     *
     * <p>
     * Implementation is required to recursively invoke events for all reachable
     * binding objects.
     *
     * @param type - binding type of container
     * @param node - schema of container
     * @return source for container node writer
     */
    protected abstract AbstractTreeNodeSerializerSource generateContainerSerializer(GeneratedType type,
            ContainerSchemaNode node);

    /**
     * Generates serializer source for supplied case node, which will read
     * supplied binding type and invoke proper methods on supplied
     * {@link BindingStreamEventWriter}.
     *
     * <p>
     * Implementation is required to recursively invoke events for all reachable
     * binding objects.
     *
     * @param type - binding type of case
     * @param node - schema of case
     * @return source for case node writer
     */
    protected abstract AbstractTreeNodeSerializerSource generateCaseSerializer(GeneratedType type, CaseSchemaNode node);

    /**
     * Generates serializer source for supplied list node, which will read
     * supplied binding type and invoke proper methods on supplied
     * {@link BindingStreamEventWriter}.
     *
     * <p>
     * Implementation is required to recursively invoke events for all reachable
     * binding objects.
     *
     * @param type - binding type of list
     * @param node - schema of list
     * @return source for list node writer
     */
    protected abstract AbstractTreeNodeSerializerSource generateMapEntrySerializer(GeneratedType type,
            ListSchemaNode node);

    /**
     * Generates serializer source for supplied list node, which will read
     * supplied binding type and invoke proper methods on supplied
     * {@link BindingStreamEventWriter}.
     *
     * <p>
     * Implementation is required to recursively invoke events for all reachable
     * binding objects.
     *
     * @param type - binding type of list
     * @param node - schema of list
     * @return source for list node writer
     */
    protected abstract AbstractTreeNodeSerializerSource generateUnkeyedListEntrySerializer(GeneratedType type,
            ListSchemaNode node);

    /**
     * Generates serializer source for supplied augmentation node, which will
     * read supplied binding type and invoke proper methods on supplied
     * {@link BindingStreamEventWriter}.
     *
     * <p>
     * Implementation is required to recursively invoke events for all reachable
     * binding objects.
     *
     * @param type - binding type of augmentation
     * @param schemas - schemas of augmentation
     * @return source for augmentation node writer
     */
    protected abstract AbstractTreeNodeSerializerSource generateAugmentSerializer(GeneratedType type,
        Collection<AugmentationSchemaNode> schemas);

    /**
     * Generates serializer source for notification node, which will read
     * supplied binding type and invoke proper methods on supplied
     * {@link BindingStreamEventWriter}.
     *
     * <p>
     * Implementation is required to recursively invoke events for all reachable
     * binding objects.
     *
     * @param type - binding type of notification
     * @param node - schema of notification
     * @return source for notification node writer
     */
    protected abstract AbstractTreeNodeSerializerSource generateNotificationSerializer(GeneratedType type,
            NotificationDefinition node);

}
