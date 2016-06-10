/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.mdsal.binding2.model.api.Restrictions;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
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

    private static final Restrictions EMPTY_RESTRICTIONS = new Restrictions() {
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

    /**
     * Creates package name from specified <code>basePackageName</code> (package
     * name for module) and <code>schemaPath</code>.
     *
     * Resulting package name is concatenation of <code>basePackageName</code>
     * and all local names of YANG nodes which are parents of some node for
     * which <code>schemaPath</code> is specified.
     *
     * Based on type of node, there is also possible suffix added in order
     * to prevent package name conflicts.
     *
     * @param basePackageName
     *            string with package name of the module, MUST be normalized,
     *            otherwise this method may return an invalid string.
     * @param schemaPath
     *            list of names of YANG nodes which are parents of some node +
     *            name of this node
     * @return string with valid JAVA package name
     * @throws NullPointerException if any of the arguments are null
     */
    public static String packageNameForGeneratedType(final String basePackageName, final SchemaPath schemaPath) {
        final int size = Iterables.size(schemaPath.getPathTowardsRoot()) - 1;
        if (size <= 0) {
            return basePackageName;
        }

        return generateNormalizedPackageName(basePackageName, schemaPath.getPathFromRoot(), size);
    }

    /**
     * Creates package name from specified <code>basePackageName</code> (package
     * name for module) and <code>schemaPath</code> which crosses an augmentation.
     *
     * Resulting package name is concatenation of <code>basePackageName</code>
     * and all local names of YANG nodes which are parents of some node for
     * which <code>schemaPath</code> is specified.
     *
     * Based on type of node, there is also possible suffix added in order
     * to prevent package name conflicts.
     *
     * @param basePackageName
     *            string with package name of the module, MUST be normalized,
     *            otherwise this method may return an invalid string.
     * @param schemaPath
     *            list of names of YANG nodes which are parents of some node +
     *            name of this node
     * @return string with valid JAVA package name
     * @throws NullPointerException if any of the arguments are null
     */
    public static String packageNameForAugmentedGeneratedType(final String basePackageName, final SchemaPath schemaPath) {
        final int size = Iterables.size(schemaPath.getPathTowardsRoot());
        if (size == 0) {
            return basePackageName;
        }

        return generateNormalizedPackageName(basePackageName, schemaPath.getPathFromRoot(), size);
    }

    //FIXME: colon in identifier?
    private static String generateNormalizedPackageName(final String base, final Iterable<QName> path, final int size) {
        final StringBuilder builder = new StringBuilder(base);
        final Iterator<QName> iterator = path.iterator();
        for (int i = 0; i < size; ++i) {
            builder.append('.');
            String nodeLocalName = iterator.next().getLocalName();
        }
        return Binding2Mapping.normalizePackageName(builder.toString());
    }

    //TODO: further implementation of static util methods...
}
