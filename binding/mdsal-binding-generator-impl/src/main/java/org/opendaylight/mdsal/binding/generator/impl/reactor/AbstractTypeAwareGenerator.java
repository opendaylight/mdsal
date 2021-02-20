/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Common base class for {@link LeafGenerator} and {@link LeafListGenerator}.
 */
abstract class AbstractTypeAwareGenerator<T extends DataTreeEffectiveStatement<?>>
        extends AbstractTypeObjectGenerator<T> {
    AbstractTypeAwareGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        verify(statement instanceof TypeAware, "Unexpected statement %s", statement);
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().getIdentifier());
    }

    @Override
    final void bindDerivedGenerators(final TypeReference reference) {
        // No-op
    }

    @Override
    final ClassPlacement classPlacementImpl(final TypedefGenerator baseGen) {
        // TODO: make this a lot more accurate by comparing the effective delta between the base type and the effective
        //       restricted type. We should not be generating a type for constructs like:
        //
        //         leaf foo {
        //           type uint8 { range 0..255; }
        //         }
        //
        //       or
        //
        //         typedef foo {
        //           type uint8 { range 0..100; }
        //         }
        //
        //         leaf foo {
        //           type foo { range 0..100; }
        //         }
        //
        //       Which is relatively easy to do for integral types, but is way more problematic for 'pattern'
        //       restrictions. Nevertheless we can define the mapping in a way which can be implemented with relative
        //       ease.
        if ((baseGen != null || SIMPLE_TYPES.containsKey(type.argument())) && noSignificantSubstatement()) {
            return ClassPlacement.NONE;
        }
        return ClassPlacement.MEMBER;
    }

    @Override
    final TypeDefinition<?> extractTypeDefinition() {
        return ((TypedDataSchemaNode) statement()).getType();
    }

    private boolean noSignificantSubstatement() {
        return type.effectiveSubstatements().stream()
            .noneMatch(stmt -> stmt instanceof BaseEffectiveStatement
                || stmt instanceof LengthEffectiveStatement
                || stmt instanceof PatternEffectiveStatement
                || stmt instanceof RangeEffectiveStatement);
    }

    @Override
    final JavaTypeName createTypeName() {
        // FIXME: we should be be assigning a non-conflict name here
        return getParent().typeName().createEnclosed(assignedName(), "$");
    }

    @Override
    GeneratedType createRootType(final TypeBuilderFactory builderFactory) {
        // FIXME: union

        return builderFactory.newGeneratedTOBuilder(typeName()).build();
    }

    @Override
    final GeneratedTransferObject createDerivedType(final TypeBuilderFactory builderFactory,
            final GeneratedTransferObject baseType) {
        // FIXME: finish this

        return builderFactory.newGeneratedTOBuilder(typeName()).setExtendsType(baseType).build();
    }

    @Override
    Type methodReturnType(final TypeBuilderFactory builderFactory, final TypedefGenerator baseGen,
            final TypeReference refType) {
        final GeneratedType generatedType = tryGeneratedType(builderFactory);
        if (generatedType != null) {
            // We have generated a type here, so return it. This covers 'bits', 'enumeration' and 'union'.
            return generatedType;
        }

        if (refType != null) {
            // This is a reference type of some kind. Defer to its judgement as to what the return type is.
            return refType.methodReturnType(builderFactory);
        }

        final Type baseType;
        if (baseGen == null) {
            final QName qname = type.argument();
            baseType = verifyNotNull(SIMPLE_TYPES.get(qname), "Cannot resolve type %s in %s", qname, this);
        } else {
            // We are derived from a base generator. Defer to its type for return.
            baseType = baseGen.getGeneratedType(builderFactory);
        }

        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(extractTypeDefinition());
        return restrictions.isEmpty() ? baseType : Types.restrictedType(baseType, restrictions);
    }

    @Override
    final void addAsGetterMethodOverride(final GeneratedTypeBuilderBase<?> builder,
            final TypeBuilderFactory builderFactory) {
        // FIXME: generate an override method with concrete type if need be
        // addLeafrefNodeToBuilderAsMethod(context, (TypedDataSchemaNode) schemaNode, parent, inGrouping);

//        final MethodSignatureBuilder getter = constructGetter(interfaceBuilder, returnType, node);
//        getter.addAnnotation(OVERRIDE_ANNOTATION);
//        return getter;
    }
}
