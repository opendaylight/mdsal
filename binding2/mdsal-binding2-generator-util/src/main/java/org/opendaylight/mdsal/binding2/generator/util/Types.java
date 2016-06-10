/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import org.opendaylight.mdsal.binding2.model.api.ConcreteType;
import org.opendaylight.mdsal.binding2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding2.model.api.Restrictions;
import org.opendaylight.mdsal.binding2.model.api.Type;

@Beta
public final class Types {

    public static final ConcreteType BOOLEAN = typeForClass(Boolean.class);
    public static final ConcreteType FUTURE = typeForClass(Future.class);
    public static final ConcreteType STRING = typeForClass(String.class);
    public static final ConcreteType VOID = typeForClass(Void.class);
    public static final ConcreteType BYTE_ARRAY = primitiveType("byte[]", null);
    public static final ConcreteType CHAR_ARRAY = primitiveType("char[]", null);

    private static final CacheLoader<Class<?>, ConcreteType> TYPE_LOADER =
            new CacheLoader<Class<?>, ConcreteType>() {

                @Override
                public ConcreteType load(Class<?> key) throws Exception {
                    return new ConcreteTypeImpl(key.getPackage().getName(), key.getSimpleName(), null);
                }
    };

    private static final LoadingCache<Class<?>, ConcreteType> TYPE_CACHE =
            CacheBuilder.newBuilder().weakKeys().build(TYPE_LOADER);

    private static final Splitter DOT_SPLITTER = Splitter.on('.');
    private static final Type SET_TYPE = typeForClass(Set.class);
    private static final Type LIST_TYPE = typeForClass(List.class);
    private static final Type MAP_TYPE = typeForClass(Map.class);

    private Types() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates the instance of type
     * {@link org.opendaylight.mdsal.binding2.model.api.ConcreteType
     * ConcreteType} which represents JAVA <code>void</code> type.
     *
     * @return <code>ConcreteType</code> instance which represents JAVA
     *         <code>void</code>
     */
    public static ConcreteType voidType() {
        return VOID;
    }

    /**
     * Returns an instance of {@link ConcreteType} describing the class
     *
     * @param cls
     *            Class to describe
     * @return Description of class
     */
    public static ConcreteType typeForClass(final Class<?> cls) {
        return TYPE_CACHE.getUnchecked(cls);
    }

    /**
     * Creates the instance of type
     * {@link org.opendaylight.mdsal.binding2.model.api.ConcreteType
     * ConcreteType} which represents primitive JAVA type for which package
     * doesn't exist.
     *
     * @param primitiveType
     *            string containing programmatic construction based on
     *            primitive type (e.g byte[])
     * @return <code>ConcreteType</code> instance which represents programmatic
     *         construction with primitive JAVA type
     */
    public static ConcreteType primitiveType(final String primitiveType, final Restrictions restrictions) {
        return new ConcreteTypeImpl("", primitiveType, restrictions);
    }

    /**
     * Creates instance of type
     * {@link org.opendaylight.mdsal.binding2.model.api.ParameterizedType
     * ParameterizedType}
     *
     * @param type
     *            JAVA <code>Type</code> for raw type
     * @param parameters
     *            JAVA <code>Type</code>s for actual parameter types
     * @return <code>ParametrizedType</code> reprezentation of <code>type</code>
     *         and its parameters <code>parameters</code>
     */
    public static ParameterizedType parameterizedTypeFor(final Type type, final Type... parameters) {
        return new ParameterizedTypeImpl(type, parameters);
    }

    /**
     *
     * Represents concrete JAVA type.
     *
     */
    private static final class ConcreteTypeImpl extends AbstractBaseType implements ConcreteType {

        private final Restrictions restrictions;

        /**
         * Creates instance of this class with package <code>pkName</code> and
         * with the type name <code>name</code>.
         *
         * @param pkName
         *            string with package name
         * @param name
         *            string with the name of the type
         */
        private ConcreteTypeImpl(final String pkName, final String name, final Restrictions restrictions) {
            super(pkName, name);
            this.restrictions = restrictions;
        }

        @Override
        public Restrictions getRestrictions() {
            return restrictions;
        }
    }

    /**
     *
     * Represents parametrized JAVA type.
     *
     */
    private static class ParameterizedTypeImpl extends AbstractBaseType implements ParameterizedType {
        /**
         * Array of JAVA actual type parameters.
         */
        private final Type[] actualTypes;

        /**
         * JAVA raw type (like List, Set, Map...)
         */
        private final Type rawType;

        @Override
        public Type[] getActualTypeArguments() {

            return actualTypes;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        /**
         * Creates instance of this class with concrete rawType and array of
         * actual parameters.
         *
         * @param rawType
         *            JAVA <code>Type</code> for raw type
         * @param actTypes
         *            array of actual parameters
         */
        public ParameterizedTypeImpl(final Type rawType, final Type[] actTypes) {
            super(rawType.getPackageName(), rawType.getName());
            this.rawType = rawType;
            this.actualTypes = actTypes.clone();
        }

    }
}

