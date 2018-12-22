/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Abstract base class for functionality exposed from {@link BindingClassSupport} and BindingRuntimeContext.
 *
 * @author Robert Varga
 */
@Beta
public class AbstractBindingClassSupport implements Immutable, SchemaContextProvider {
    private final BindingRuntimeTypes bindingTypes;
    private final SchemaContext schemaContext;

    protected AbstractBindingClassSupport(final SchemaContext schemaContext, final BindingRuntimeTypes bindingTypes) {
        this.schemaContext = requireNonNull(schemaContext);
        this.bindingTypes = requireNonNull(bindingTypes);
    }

    @Override
    public final SchemaContext getSchemaContext() {
        return schemaContext;
    }

    public final BindingRuntimeTypes getBindingTypes() {
        return bindingTypes;
    }

    /**
     * Returns schema ({@link DataSchemaNode}, {@link AugmentationSchemaNode} or {@link TypeDefinition})
     * from which supplied class was generated. Returned schema may be augmented with
     * additional information, which was not available at compile type
     * (e.g. third party augmentations).
     *
     * @param type Binding Class for which schema should be retrieved.
     * @return Instance of generated type (definition of Java API), along with
     *     {@link DataSchemaNode}, {@link AugmentationSchemaNode} or {@link TypeDefinition}
     *     which was used to generate supplied class.
     */
    public final Entry<GeneratedType, WithStatus> getTypeWithSchema(final Class<?> type) {
        return getTypeWithSchema(referencedType(type));
    }

    private Entry<GeneratedType, WithStatus> getTypeWithSchema(final Type referencedType) {
        final WithStatus schema = getBindingTypes().findSchema(referencedType).orElseThrow(
            () -> new NullPointerException("Failed to find schema for type " + referencedType));
        final Type definedType = getBindingTypes().findType(schema).orElseThrow(
            () -> new NullPointerException("Failed to find defined type for " + referencedType + " schema " + schema));

        if (definedType instanceof GeneratedTypeBuilder) {
            return new SimpleEntry<>(((GeneratedTypeBuilder) definedType).build(), schema);
        }
        checkArgument(definedType instanceof GeneratedType, "Type %s is not a GeneratedType", referencedType);
        return new SimpleEntry<>((GeneratedType) definedType, schema);
    }

    @Override
    public final String toString() {
        return addToString(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToString(final ToStringHelper helper) {
        return helper.add("runtimeTypes", bindingTypes).add("schemaContext", schemaContext);
    }

    protected static final Type referencedType(final Class<?> type) {
        final JavaTypeName typeName = JavaTypeName.create(type);
        return () -> typeName;
    }
}
