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
import java.util.Enumeration;
import java.util.List;
import org.opendaylight.mdsal.binding2.generator.util.AbstractBaseType;
import org.opendaylight.mdsal.binding2.model.api.Constant;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.yangtools.yang.common.QName;
import sun.reflect.annotation.AnnotationType;

@Beta
abstract class AbstractGeneratedType extends AbstractBaseType implements GeneratedType {

    //TODO: implement all methods
    public AbstractGeneratedType(final AbstractGeneratedTypeBuilder<?> builder) {
    }

    @Override
    public Type getParentType() {
        return null;
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public List<AnnotationType> getAnnotations() {
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

    @Override
    public Optional<String> getDescription() {
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
}
