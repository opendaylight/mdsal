/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util;

import com.google.common.annotations.Beta;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.opendaylight.mdsal.binding2.model.api.Restrictions;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

@Beta
/**
 * Standard Util class that contains various method for converting
 * input strings to valid JAVA language strings e.g. package names,
 * class names, attribute names and/or valid JavaDoc comments.
 */
public final class Binding2GeneratorUtil {

    private Binding2GeneratorUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final Restrictions restrictions = new Restrictions() {
        @Override
        public List<LengthConstraint> getLengthConstraints() {
            return Collections.emptyList();
        }

        @Override
        public List<PatternConstraint> getPatternConstraints() {
            return Collections.emptyList();
        }

        @Override
        public List<RangeConstraint> getRangeConstraints() {
            return Collections.emptyList();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    private static final Comparator<TypeMemberBuilder<?>> SUID_MEMBER_COMPARATOR =
            (o1, o2) -> o1.getName().compareTo(o2.getName());

    private static final Comparator<Type> SUID_NAME_COMPARATOR =
            (o1, o2) -> o1.getFullyQualifiedName().compareTo(o2.getFullyQualifiedName());

    //TODO: implement static Util methods
}
