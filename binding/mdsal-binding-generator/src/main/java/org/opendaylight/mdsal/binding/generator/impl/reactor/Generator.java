/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.model.ri.Types.STRING;
import static org.opendaylight.mdsal.binding.model.ri.Types.classType;
import static org.opendaylight.mdsal.binding.model.ri.Types.primitiveBooleanType;
import static org.opendaylight.mdsal.binding.model.ri.Types.primitiveIntType;
import static org.opendaylight.mdsal.binding.model.ri.Types.wildcardTypeFor;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.generator.impl.tree.StatementRepresentation;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeMemberComment;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotableTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single node in generator tree. Each node will eventually resolve to a generated Java class. Each node also can have
 * a number of children, which are generators corresponding to the YANG subtree of this node.
 *
 * <p>
 * Each tree is rooted in a {@link ModuleGenerator} and its organization follows roughly YANG {@code schema tree}
 * layout, but with a twist coming from the reuse of generated interfaces from a {@code grouping} in the location of
 * every {@code uses} encountered and also the corresponding backwards propagation of {@code augment} effects.
 *
 * <p>
 * Overall the tree layout guides the allocation of Java package and top-level class namespaces.
 */
public abstract class Generator<S extends EffectiveStatement<?, ?>, R extends RuntimeType>
        implements Iterable<Generator<?, ?>>, CopyableNode, StatementRepresentation<S> {
    private static final Logger LOG = LoggerFactory.getLogger(Generator.class);

    static final JavaTypeName DEPRECATED_ANNOTATION = JavaTypeName.create(Deprecated.class);
    static final JavaTypeName OVERRIDE_ANNOTATION = JavaTypeName.create(Override.class);

    private final AbstractCompositeGenerator<?, ?> parent;
    private final @NonNull S statement;

    private Optional<Member> member;
    private GeneratorResult result;
    private JavaTypeName typeName;
    private String javaPackage;

    /**
     * Field tracking previous incarnation (along reverse of 'uses' and 'augment' axis) of this statement. This field
     * can either be one of:
     * <ul>
     *   <li>{@code null} when not resolved, i.e. access is not legal, or</li>
     *   <li>{@code this} object if this is the original definition, or</li>
     *   <li>a generator which is one step closer to the original definition</li>
     * </ul>
     */
    private Generator<S, R> prev;
    /**
     * Field holding the original incarnation, i.e. the terminal node along {@link #prev} links.
     */
    private Generator<S, R> orig;
    /**
     * Field containing and indicator holding the runtime type, if applicable.
     */
    private @Nullable R runtimeType;
    private boolean runtimeTypeInitialized;

    Generator(final S statement) {
        parent = null;
        this.statement = requireNonNull(statement);
    }

    Generator(final S statement, final AbstractCompositeGenerator<?, ?> parent) {
        this.parent = requireNonNull(parent);
        this.statement = requireNonNull(statement);
    }

    public final @NonNull Optional<GeneratedType> generatedType() {
        return Optional.ofNullable(result.generatedType());
    }

    public @NonNull List<GeneratedType> auxiliaryGeneratedTypes() {
        return List.of();
    }

    @Override
    public final @NonNull S statement() {
        return statement;
    }

    @Override
    public Iterator<Generator<?, ?>> iterator() {
        return Collections.emptyIterator();
    }

    /**
     * Return the {@link AbstractCompositeGenerator} inside which this generator is defined. It is illegal to call this
     * method on a {@link ModuleGenerator}.
     *
     * @return Parent generator
     */
    final @NonNull AbstractCompositeGenerator<?, ?> getParent() {
        return verifyNotNull(parent, "No parent for %s", this);
    }

    boolean isEmpty() {
        return true;
    }

    /**
     * Return the namespace of this statement.
     *
     * @return Corresponding namespace
     * @throws UnsupportedOperationException if this node does not have a corresponding namespace
     */
    abstract @NonNull StatementNamespace namespace();

    @NonNull ModuleGenerator currentModule() {
        return getParent().currentModule();
    }

    /**
     * Push this statement into a {@link SchemaInferenceStack} so that the stack contains a resolvable {@code data tree}
     * hierarchy.
     *
     * @param inferenceStack Target inference stack
     */
    abstract void pushToInference(@NonNull SchemaInferenceStack inferenceStack);

    @NonNull ClassPlacement classPlacement() {
        // We process nodes introduced through augment or uses separately
        // FIXME: this is not quite right!
        return isAddedByUses() || isAugmenting() ? ClassPlacement.NONE : ClassPlacement.TOP_LEVEL;
    }

    final @NonNull Member getMember() {
        return verifyNotNull(ensureMember(), "No member for %s", this);
    }

    final Member ensureMember() {
        if (member == null) {
            member = switch (classPlacement()) {
                case NONE -> Optional.empty();
                case MEMBER, PHANTOM, TOP_LEVEL -> Optional.of(createMember(parentDomain()));
            };
        }
        return member.orElse(null);
    }

    @NonNull CollisionDomain parentDomain() {
        return getParent().domain();
    }

    @NonNull Member createMember(final CollisionDomain domain) {
        return domain.addPrimary(this, new CamelCaseNamingStrategy(namespace(), localName()));
    }

    /**
     * Create the type associated with this builder. This method idempotent.
     *
     * @param builderFactory Factory for {@link TypeBuilder}s
     * @throws NullPointerException if {@code builderFactory} is {@code null}
     */
    final void ensureType(final TypeBuilderFactory builderFactory) {
        if (result != null) {
            return;
        }

        result = switch (classPlacement()) {
            case NONE, PHANTOM -> GeneratorResult.empty();
            case MEMBER -> GeneratorResult.member(createTypeImpl(requireNonNull(builderFactory)));
            case TOP_LEVEL -> GeneratorResult.toplevel(createTypeImpl(requireNonNull(builderFactory)));
        };

        for (var child : this) {
            child.ensureType(builderFactory);
        }
    }

    @NonNull GeneratedType getGeneratedType(final TypeBuilderFactory builderFactory) {
        return verifyNotNull(tryGeneratedType(builderFactory), "No type generated for %s", this);
    }

    final @Nullable GeneratedType tryGeneratedType(final TypeBuilderFactory builderFactory) {
        ensureType(builderFactory);
        return result.generatedType();
    }

    final @Nullable GeneratedType enclosedType(final TypeBuilderFactory builderFactory) {
        ensureType(builderFactory);
        return result.enclosedType();
    }

    /**
     * Create the type associated with this builder, as per {@link #ensureType(TypeBuilderFactory)} contract. This
     * method is guaranteed to be called at most once.
     *
     * @param builderFactory Factory for {@link TypeBuilder}s
     */
    abstract @NonNull GeneratedType createTypeImpl(@NonNull TypeBuilderFactory builderFactory);

    final @NonNull String assignedName() {
        return getMember().currentClass();
    }

    final @NonNull String javaPackage() {
        String local = javaPackage;
        if (local == null) {
            javaPackage = local = createJavaPackage();
        }
        return local;
    }

    @NonNull String createJavaPackage() {
        final String parentPackage = getPackageParent().javaPackage();
        final String myPackage = getMember().currentPackage();
        return Naming.normalizePackageName(parentPackage + '.' + myPackage);
    }

    final @NonNull JavaTypeName typeName() {
        JavaTypeName local = typeName;
        if (local == null) {
            typeName = local = createTypeName();
        }
        return local;
    }

    @NonNull JavaTypeName createTypeName() {
        return JavaTypeName.create(getPackageParent().javaPackage(), assignedName());
    }

    @NonNull AbstractCompositeGenerator<?, ?> getPackageParent() {
        return getParent();
    }


    /**
     * Return the {@link RuntimeType} associated with this object, if applicable. This represents the
     * externally-accessible view of this object when considered outside the schema tree or binding tree hierarchy.
     *
     * @return Associated run-time type, or empty
     */
    public final Optional<R> runtimeType() {
        if (!runtimeTypeInitialized) {
            final var type = runtimeJavaType();
            if (type != null) {
                runtimeType = createExternalRuntimeType(type);
            }
            runtimeTypeInitialized = true;
        }
        return Optional.ofNullable(runtimeType);
    }

    /**
     * Return the {@link Type} associated with this object at run-time, if applicable. This method often synonymous
     * with {@code generatedType().orElseNull()}, but not always. For example
     * <pre>
     *   <code>
     *     leaf foo {
     *       type string;
     *     }
     *   </code>
     * </pre>
     * Results in an empty {@link #generatedType()}, but still produces a {@code java.lang.String}-based
     * {@link RuntimeType}.
     *
     * @return Associated {@link Type}
     */
    // FIXME: this should be a generic class argument
    // FIXME: this needs a better name, but 'runtimeType' is already taken.
    @Nullable Type runtimeJavaType() {
        return generatedType().orElse(null);
    }

    /**
     * Create the externally-accessible {@link RuntimeType} view of this object. The difference between
     * this method and {@link #createInternalRuntimeType(EffectiveStatement)} is that this method represents the view
     * attached to {@link #statement()} and contains a separate global view of all available augmentations attached to
     * the GeneratedType.
     *
     * @param type {@link Type} associated with this object, as returned by {@link #runtimeJavaType()}
     * @return Externally-accessible RuntimeType
     */
    abstract @NonNull R createExternalRuntimeType(@NonNull Type type);

    /**
     * Create the internally-accessible {@link RuntimeType} view of this object, if applicable. The difference between
     * this method and {@link #createExternalRuntimeType()} is that this represents the view attached to the specified
     * {@code stmt}, which is supplied by the parent statement. The returned {@link RuntimeType} always reports the
     * global view of attached augmentations as empty.
     *
     * @param lookup context to use when looking up child statements
     * @param stmt Statement for which to create the view
     * @return Internally-accessible RuntimeType, or {@code null} if not applicable
     */
    final @Nullable R createInternalRuntimeType(final @NonNull AugmentResolver resolver, final @NonNull S stmt) {
        // FIXME: cache requests: if we visited this statement, we obviously know what it entails. Note that we walk
        //        towards the original definition. As such, the cache may have to live in the generator we look up,
        //        but should operate on this statement to reflect lookups. This needs a bit of figuring out.
        var gen = this;
        do {
            final var type = gen.runtimeJavaType();
            if (type != null) {
                return createInternalRuntimeType(resolver, stmt, type);
            }

            gen = gen.previous();
        } while (gen != null);

        return null;
    }

    abstract @NonNull R createInternalRuntimeType(@NonNull AugmentResolver resolver, @NonNull S statement,
        @NonNull Type type);

    @Override
    public final boolean isAddedByUses() {
        return statement instanceof AddedByUsesAware aware && aware.isAddedByUses();
    }

    @Override
    public final boolean isAugmenting() {
        return statement instanceof CopyableNode copyable && copyable.isAugmenting();
    }

    /**
     * Attempt to link the generator corresponding to the original definition for this generator.
     *
     * @return {@code true} if this generator is linked
     */
    final boolean linkOriginalGenerator() {
        if (orig != null) {
            // Original already linked
            return true;
        }

        if (prev == null) {
            LOG.trace("Linking {}", this);

            if (!isAddedByUses() && !isAugmenting()) {
                orig = prev = this;
                LOG.trace("Linked {} to self", this);
                return true;
            }

            final var link = getParent().<S, R>originalChild(getQName());
            if (link == null) {
                LOG.trace("Cannot link {} yet", this);
                return false;
            }

            prev = link.previous();
            orig = link.original();
            if (orig != null) {
                LOG.trace("Linked {} to {} original {}", this, prev, orig);
                return true;
            }

            LOG.trace("Linked {} to intermediate {}", this, prev);
            return false;
        }

        orig = prev.originalLink().original();
        if (orig != null) {
            LOG.trace("Linked {} to original {}", this, orig);
            return true;
        }
        return false;
    }

    /**
     * Return the previous incarnation of this generator, or {@code null} if this is the original generator.
     *
     * @return Previous incarnation or {@code null}
     */
    final @Nullable Generator<S, R> previous() {
        final var local = verifyNotNull(prev, "Generator %s does not have linkage to previous instance resolved", this);
        return local == this ? null : local;
    }

    /**
     * Return the original incarnation of this generator, or self if this is the original generator.
     *
     * @return Original incarnation of this generator
     */
    @NonNull Generator<S, R> getOriginal() {
        return verifyNotNull(orig, "Generator %s does not have linkage to original instance resolved", this);
    }

    @Nullable Generator<S, R> tryOriginal() {
        return orig;
    }

    /**
     * Return the link towards the original generator.
     *
     * @return Link towards the original generator.
     */
    final @NonNull OriginalLink<S, R> originalLink() {
        final var local = prev;
        if (local == null) {
            return OriginalLink.partial(this);
        } else if (local == this) {
            return OriginalLink.complete(this);
        } else {
            return OriginalLink.partial(local);
        }
    }

    @Nullable Generator<?, ?> findSchemaTreeGenerator(final QName qname) {
        return findLocalSchemaTreeGenerator(qname);
    }

    final @Nullable Generator<?, ?> findLocalSchemaTreeGenerator(final QName qname) {
        for (var child : this) {
            if (child.statement instanceof SchemaTreeEffectiveStatement<?> stmt && qname.equals(stmt.argument())) {
                return child;
            }
        }
        return null;
    }

    final @NonNull QName getQName() {
        final Object arg = statement.argument();
        if (arg instanceof QName qname) {
            return qname;
        }
        throw new VerifyException("Unexpected argument " + arg);
    }

    @NonNull AbstractQName localName() {
        // FIXME: this should be done in a nicer way
        final Object arg = statement.argument();
        if (arg instanceof AbstractQName aqn) {
            return aqn;
        }
        throw new VerifyException("Illegal argument " + arg);
    }


    void addAsGetterMethod(final @NonNull GeneratedTypeBuilderBase<?> builder,
            final @NonNull TypeBuilderFactory builderFactory) {
        if (isAugmenting()) {
            // Do not process augmented nodes: they will be taken care of in their home augmentation
            return;
        }
        if (isAddedByUses()) {
            // If this generator has been added by a uses node, it is already taken care of by the corresponding
            // grouping. There is one exception to this rule: 'type leafref' can use a relative path to point
            // outside of its home grouping. In this case we need to examine the instantiation until we succeed in
            // resolving the reference.
            addAsGetterMethodOverride(builder, builderFactory);
            return;
        }

        final Type returnType = methodReturnType(builderFactory);
        constructGetter(builder, returnType);
        constructRequire(builder, returnType);
    }

    MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        return constructGetter(builder, returnType, Naming.getGetterMethodName(localName().getLocalName()));
    }

    final MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder,
            final Type returnType, final String methodName) {
        final MethodSignatureBuilder getMethod = builder.addMethod(methodName).setReturnType(returnType);

        annotateDeprecatedIfNecessary(getMethod);

        statement.findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class)
            .map(TypeMemberComment::referenceOf).ifPresent(getMethod::setComment);

        return getMethod;
    }

    void constructRequire(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        // No-op in most cases
    }

    final void constructRequireImpl(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        constructGetter(builder, returnType, Naming.getRequireMethodName(localName().getLocalName()))
            .setDefault(true)
            .setMechanics(ValueMechanics.NONNULL);
    }

    void addAsGetterMethodOverride(final @NonNull GeneratedTypeBuilderBase<?> builder,
            final @NonNull TypeBuilderFactory builderFactory) {
        // No-op for most cases
    }

    @NonNull Type methodReturnType(final @NonNull TypeBuilderFactory builderFactory) {
        return getGeneratedType(builderFactory);
    }

    final void annotateDeprecatedIfNecessary(final AnnotableTypeBuilder builder) {
        annotateDeprecatedIfNecessary(statement, builder);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        helper.add("argument", statement.argument());
        if (isAddedByUses()) {
            helper.addValue("addedByUses");
        }
        if (isAugmenting()) {
            helper.addValue("augmenting");
        }
        return helper;
    }

    final void addImplementsChildOf(final GeneratedTypeBuilder builder) {
        AbstractCompositeGenerator<?, ?> ancestor = getParent();
        while (true) {
            // choice/case hierarchy does not factor into 'ChildOf' hierarchy, hence we need to skip them
            if (ancestor instanceof CaseGenerator || ancestor instanceof ChoiceGenerator) {
                ancestor = ancestor.getParent();
                continue;
            }

            // if we into a choice we need to follow the hierararchy of that choice
            if (ancestor instanceof AbstractAugmentGenerator augment
                && augment.targetGenerator() instanceof ChoiceGenerator targetChoice) {
                ancestor = targetChoice;
                continue;
            }

            break;
        }

        builder.addImplementsType(BindingTypes.childOf(Type.of(ancestor.typeName())));
    }

    /**
     * Add common methods implemented in a generated type. This includes {@link DataContainer#implementedInterface()} as
     * well has {@code bindingHashCode()}, {@code bindingEquals()} and {@code bindingToString()}.
     *
     * @param builder Target builder
     */
    static final void addConcreteInterfaceMethods(final GeneratedTypeBuilder builder) {
        defaultImplementedInterace(builder);

        builder.addMethod(Naming.BINDING_HASHCODE_NAME)
            .setAccessModifier(AccessModifier.PUBLIC)
            .setStatic(true)
            .setReturnType(primitiveIntType());
        builder.addMethod(Naming.BINDING_EQUALS_NAME)
            .setAccessModifier(AccessModifier.PUBLIC)
            .setStatic(true)
            .setReturnType(primitiveBooleanType());
        builder.addMethod(Naming.BINDING_TO_STRING_NAME)
            .setAccessModifier(AccessModifier.PUBLIC)
            .setStatic(true)
            .setReturnType(STRING);
    }

    static final void annotateDeprecatedIfNecessary(final EffectiveStatement<?, ?> stmt,
            final AnnotableTypeBuilder builder) {
        if (stmt instanceof WithStatus withStatus) {
            annotateDeprecatedIfNecessary(withStatus, builder);
        }
    }

    static final void annotateDeprecatedIfNecessary(final WithStatus node, final AnnotableTypeBuilder builder) {
        switch (node.getStatus()) {
            case DEPRECATED ->
                // FIXME: we really want to use a pre-made annotation
                builder.addAnnotation(DEPRECATED_ANNOTATION);
            case OBSOLETE -> builder.addAnnotation(DEPRECATED_ANNOTATION).addParameter("forRemoval", "true");
            case CURRENT -> {
                // No-op
            }
            default -> throw new IllegalStateException("Unhandled status in " + node);
        }
    }

    static final void addUnits(final GeneratedTOBuilder builder, final TypeDefinition<?> typedef) {
        typedef.getUnits().ifPresent(units -> {
            if (!units.isEmpty()) {
                builder.addConstant(Types.STRING, "_UNITS", "\"" + units + "\"");
                final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("UNITS");
                prop.setReturnType(Types.STRING);
                builder.addToStringProperty(prop);
            }
        });
    }

    /**
     * Add {@link java.io.Serializable} to implemented interfaces of this TO. Also compute and add serialVersionUID
     * property.
     *
     * @param builder transfer object which needs to be made serializable
     */
    static final void makeSerializable(final GeneratedTOBuilder builder) {
        builder.addImplementsType(Types.serializableType());
        addSerialVersionUID(builder);
    }

    static final void addSerialVersionUID(final GeneratedTOBuilder gto) {
        final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
        prop.setValue(Long.toString(SerialVersionHelper.computeDefaultSUID(gto)));
        gto.setSUID(prop);
    }

    /**
     * Add a {@link DataContainer#implementedInterface()} declaration with a narrower return type to specified builder.
     *
     * @param builder Target builder
     */
    static final void narrowImplementedInterface(final GeneratedTypeBuilder builder) {
        defineImplementedInterfaceMethod(builder, wildcardTypeFor(builder.getIdentifier()));
    }

    /**
     * Add a default implementation of {@link DataContainer#implementedInterface()} to specified builder.
     *
     * @param builder Target builder
     */
    static final void defaultImplementedInterace(final GeneratedTypeBuilder builder) {
        defineImplementedInterfaceMethod(builder, Type.of(builder)).setDefault(true);
    }

    @SuppressWarnings("unchecked")
    static final <T extends EffectiveStatement<?, ?>> Generator<T, ?> getChild(final Generator<?, ?> parent,
            final Class<T> type) {
        for (var child : parent) {
            if (type.isInstance(child.statement)) {
                return (Generator<T, ?>) child;
            }
        }
        throw new IllegalStateException("Cannot find " + type + " in " + parent);
    }

    private static MethodSignatureBuilder defineImplementedInterfaceMethod(final GeneratedTypeBuilder typeBuilder,
            final Type classType) {
        final MethodSignatureBuilder ret = typeBuilder
                .addMethod(Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME)
                .setAccessModifier(AccessModifier.PUBLIC)
                .setReturnType(classType(classType));
        ret.addAnnotation(OVERRIDE_ANNOTATION);
        return ret;
    }
}
