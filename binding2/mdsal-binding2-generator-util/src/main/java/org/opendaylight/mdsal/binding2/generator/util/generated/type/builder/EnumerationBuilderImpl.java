/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding2.generator.util.AbstractBaseType;
import org.opendaylight.mdsal.binding2.generator.util.Binding2Mapping;
import org.opendaylight.mdsal.binding2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding2.model.api.Constant;
import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.mdsal.binding2.model.api.Enumeration.Pair;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

@Beta
public class EnumerationBuilderImpl extends AbstractBaseType implements EnumBuilder {

    private final String packageName;
    private final String name;
    private List<Pair> values = Collections.emptyList();
    private List<AnnotationTypeBuilder> annotationBuilders = Collections.emptyList();
    private List<Pair> unmodifiableValues  = Collections.emptyList();

    public EnumerationBuilderImpl(String packageName, String name) {
        super(packageName, name);
        this.packageName = packageName;
        this.name = name;
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(String packageName, String name) {
        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, name);
        if (!annotationBuilders.contains(builder)) {
            annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
        }
        return builder;
    }

    @Override
    public void addValue(String name, int value, String description) {
        final EnumPairImpl p = new EnumPairImpl(name, value, description);
    }

    @Override
    public Enumeration toInstance(Type definingType) {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EnumerationBuilderImpl [packageName=");
        builder.append(packageName);
        builder.append(", name=");
        builder.append(name);
        builder.append(", values=");
        builder.append(values);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void updateEnumPairsFromEnumTypeDef(EnumTypeDefinition enumTypeDef) {

    }

    @Override
    public void setDescription(String description) {

    }

    private static final class EnumPairImpl implements Enumeration.Pair {

        private final String name;
        private final String mappedName;
        private final int value;
        private final String description;

        public EnumPairImpl(final String name, final int value, final String description) {
            this.name = name;
            this.mappedName = Binding2Mapping.getClassName(name);
            this.value = value;
            this.description = description;
        }

        @Override
        public String getName() {
            return name;
        }

        //TODO: API needs fix, missing method getMappedName() in Enumeration.Pair inner class

        @Override
        public int getValue() {
            return value;
        }

        @Nullable
        @Override
        public String getDescription() {
            return description;
        }

        @Nullable
        @Override
        public String getReference() {
            //noop
            return null;
        }

        @Nonnull
        @Override
        public Status getStatus() {
            //noop
            return null;
        }
    }

    private static final class EnumerationImpl extends AbstractBaseType implements Enumeration {

        public EnumerationImpl(String packageName, String name) {
            super(packageName, name);
        }

        @Override
        public List<AnnotationType> getAnnotations() {
            return null;
        }

        @Override
        public Type getParentType() {
            return null;
        }

        @Override
        public Optional<String> getDescription() {
            return null;
        }

        @Override
        public List<Pair> getValues() {
            return null;
        }

        @Override
        public String toFormattedString() {
            return null;
        }

        @Override
        public Optional<String> getReference() {
            return null;
        }

        @Override
        public List<QName> getSchemaPath() {
            return null;
        }

        @Override
        public String getModuleName() {
            return null;
        }

        @Override
        public String getComment() {
            return null;
        }

        @Override
        public boolean isAbstract() {
            return false;
        }

        @Override
        public List<Type> getImplements() {
            return null;
        }

        @Override
        public List<GeneratedType> getEnclosedTypes() {
            return null;
        }

        @Override
        public List<Enumeration> getEnumerations() {
            return null;
        }

        @Override
        public List<Constant> getConstantDefinitions() {
            return null;
        }

        @Override
        public List<MethodSignature> getMethodDefinitions() {
            return null;
        }

        @Override
        public List<GeneratedProperty> getProperties() {
            return null;
        }
    }
}
