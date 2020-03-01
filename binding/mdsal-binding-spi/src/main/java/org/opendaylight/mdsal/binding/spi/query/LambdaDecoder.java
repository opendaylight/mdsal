/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi.query;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.opendaylight.mdsal.binding.api.query.MatchBuilderPath.LeafReference;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * @author Robert Varga
 *
 */
final class LambdaDecoder {
    static final class LambdaTarget implements Immutable {
        final String targetClass;
        final String targetMethod;

        LambdaTarget(final String targetClass, final String targetMethod) {
            this.targetClass = requireNonNull(targetClass);
            this.targetMethod = requireNonNull(targetMethod);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("class", targetClass).add("method", targetMethod).toString();
        }

    }

    private static final LoadingCache<Class<?>, Method> REPLACE_CACHE =
            CacheBuilder.newBuilder().weakKeys().weakValues()
                .build(new CacheLoader<Class<?>, Method>() {
                    @Override
                    public Method load(final Class<?> key) throws PrivilegedActionException {
                        return AccessController.doPrivileged((PrivilegedExceptionAction<Method>) () -> {
                            final Method method = key.getDeclaredMethod("writeReplace");
                            method.setAccessible(true);
                            return method;
                        });
                    }
                });
    private static final LoadingCache<LeafReference<?, ?>, LambdaTarget> LAMBDA_CACHE =
            CacheBuilder.newBuilder().weakKeys()
            .build(new CacheLoader<LeafReference<?, ?>, LambdaTarget>() {
                @Override
                public LambdaTarget load(final LeafReference<?, ?> ref) throws Exception {
                    final Object replaced = REPLACE_CACHE.get(ref.getClass()).invoke(ref);
                    verify(replaced instanceof SerializedLambda, "Unexpected replaced object %s", replaced);
                    final SerializedLambda serialized = (SerializedLambda) replaced;
                    return new LambdaTarget(serialized.getImplClass(), serialized.getImplMethodName());
                }
            });

    private LambdaDecoder() {
        // Hidden on purpose
    }

    static LambdaTarget resolveLambda(final LeafReference<?, ?> lambda) {
        return LAMBDA_CACHE.getUnchecked(lambda);
    }
}
