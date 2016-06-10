/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.GeneratedPropertyBuilder;

@Beta
public class GeneratedPropertyBuilderImpl extends AbstractTypeMemberBuilder<GeneratedPropertyBuilder> implements GeneratedPropertyBuilder {

    //TODO: implement methods
    @Override
    public GeneratedPropertyBuilder setValue(String value) {
        return null;
    }

    @Override
    public GeneratedPropertyBuilder setReadOnly(boolean isReadOnly) {
        return null;
    }

    @Override
    public GeneratedProperty toInstance(Type definingType) {
        return null;
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(String packageName, String name) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public GeneratedPropertyBuilder setReturnType(Type returnType) {
        return null;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return null;
    }

    @Override
    public GeneratedPropertyBuilder setAccessModifier(AccessModifier modifier) {
        return null;
    }

    @Override
    public GeneratedPropertyBuilder setComment(String comment) {
        return null;
    }

    @Override
    public GeneratedPropertyBuilder setFinal(boolean isFinal) {
        return null;
    }

    @Override
    public GeneratedPropertyBuilder setStatic(boolean isStatic) {
        return null;
    }

    @Override
    protected GeneratedPropertyBuilderImpl thisInstance() {
        return this;
    }
}
