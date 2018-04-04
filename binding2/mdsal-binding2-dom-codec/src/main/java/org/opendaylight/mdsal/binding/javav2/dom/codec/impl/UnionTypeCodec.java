/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import static org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer.normalizeSpecificIdentifier;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.UnionValueOptionContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.BindingCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.value.ReflectionBasedCodec;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.BaseYangTypes;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

/**
 * Codec for serialize/deserialize union type.
 *
 */
@Beta
public final class UnionTypeCodec extends ReflectionBasedCodec {

    private final ImmutableSet<UnionValueOptionContext> typeCodecs;

    private UnionTypeCodec(final Class<?> unionCls, final Set<UnionValueOptionContext> codecs) {
        super(unionCls);
        typeCodecs = ImmutableSet.copyOf(codecs);
    }

    /**
     * Loading union type codec for all subtypes of union.
     *
     * @param unionCls
     *            - binding class of union
     * @param unionType
     *            - type definition of union
     * @param bindingCodecContext
     *            - binding codec context
     * @return union codec
     */
    public static Callable<UnionTypeCodec> loader(final Class<?> unionCls, final UnionTypeDefinition unionType,
            final BindingCodecContext bindingCodecContext) {
        return () -> {
            final Set<UnionValueOptionContext> values = new LinkedHashSet<>();
            for (final TypeDefinition<?> subtype : unionType.getTypes()) {
                if (subtype instanceof LeafrefTypeDefinition) {
                    addLeafrefValueCodec(unionCls, unionType, bindingCodecContext, values, subtype);
                } else {
                    final Method valueGetter = unionCls.getMethod("get"
                        + normalizeSpecificIdentifier(subtype.getQName().getLocalName(), JavaIdentifier.CLASS));
                    final Class<?> valueType = valueGetter.getReturnType();
                    final Codec<Object, Object> valueCodec = bindingCodecContext.getCodec(valueType, subtype);
                    values.add(new UnionValueOptionContext(unionCls, valueType, valueGetter, valueCodec));
                }
            }
            return new UnionTypeCodec(unionCls, values);
        };
    }

    /**
     * Prepare codec for type from leaf's return type of leafref.
     *
     * @param unionCls
     *            - union class
     * @param unionType
     *            - union type
     * @param bindingCodecContext
     *            - binding codec context
     * @param values
     *            - union values
     * @param subtype
     *            - subtype of union
     * @throws NoSuchMethodException when the getter method is not found
     */
    private static void addLeafrefValueCodec(final Class<?> unionCls, final UnionTypeDefinition unionType,
            final BindingCodecContext bindingCodecContext, final Set<UnionValueOptionContext> values,
            final TypeDefinition<?> subtype) throws NoSuchMethodException {
        final SchemaContext schemaContext = bindingCodecContext.getRuntimeContext().getSchemaContext();
        final Module module = schemaContext.findModule(subtype.getQName().getModule()).get();
        final RevisionAwareXPath xpath = ((LeafrefTypeDefinition) subtype).getPathStatement();
        // find schema node in schema context by xpath of leafref
        final SchemaNode dataNode;
        if (xpath.isAbsolute()) {
            dataNode = SchemaContextUtil.findDataSchemaNode(schemaContext, module, xpath);
        } else {
            dataNode = SchemaContextUtil.findDataSchemaNodeForRelativeXPath(schemaContext, module, unionType, xpath);
        }

        final LeafSchemaNode typeNode = (LeafSchemaNode) dataNode;

        // prepare name of type form return type of referenced leaf
        final String typeName = BaseYangTypes.BASE_YANG_TYPES_PROVIDER
            .javaTypeForSchemaDefinitionType(typeNode.getType(), typeNode, null).getName();

        // get method via reflection from generated code according to
        // get_TypeName_Value method
        final String method = normalizeSpecificIdentifier(new StringBuilder("get").append("_")
            .append(typeName).append(unionCls.getSimpleName()).append("Value").toString(),
            JavaIdentifier.METHOD);
        final Method valueGetterParent = unionCls.getMethod(method);
        final Class<?> returnType = valueGetterParent.getReturnType();

        // prepare codec of union subtype according to return type of referenced
        // leaf
        final Codec<Object, Object> valueCodec = bindingCodecContext.getCodec(returnType, subtype);
        values.add(new UnionValueOptionContext(unionCls, returnType, valueGetterParent, valueCodec));
    }

    @Override
    public Object deserialize(final Object input) {
        for (final UnionValueOptionContext member : typeCodecs) {
            final Object ret = member.deserializeUnion(input);
            if (ret != null) {
                return ret;
            }
        }

        throw new IllegalArgumentException(
                String.format("Failed to construct instance of %s for input %s", getTypeClass(), input));
    }

    @Override
    public Object serialize(final Object input) {
        if (input != null) {
            for (final UnionValueOptionContext valCtx : typeCodecs) {
                final Object domValue = valCtx.serialize(input);
                if (domValue != null) {
                    return domValue;
                }
            }
        }
        return null;
    }
}
