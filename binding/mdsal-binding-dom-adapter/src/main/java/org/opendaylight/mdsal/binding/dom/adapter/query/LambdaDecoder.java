/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.function.Function;
import org.opendaylight.mdsal.binding.api.query.MatchBuilderPath.LeafReference;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Utility class for forcing decoding lambda instances to the method being invoked. The theory here is that
 * {@code MatchBuilderPath.leaf()} methods are expected to be used with {@code method references}, which are converted
 * to {@link LeafReference} lambdas.
 *
 * <p>
 * We then assume runtime is following guidance around {@link SerializedLambda}, thus {@link Serializable} lambdas have
 * a {@code writeReplace()} method and it produces a {@link SerializedLambda} -- which we use to get the information
 * about what the lambda does at least in the single case we support.
 *
 * <p>
 * An alternative approach to cracking the lambda would be to generate a dynamic proxy implementation of the base
 * DataObject (we have the Class to do that), back it by a invocation handler which throws a private RuntimeException
 * subclass containing the name of the invoked method. We then would invoke the lambda on such a proxy and intercept
 * the exception raised. This unfortunately has multiple downsides:
 * <ul>
 *   <li>it requires a properly-managed ClassLoader (or pollutes original classloader with the proxy class)</li>
 *   <li>it makes it appear we support something else than method references, which we do not</li>
 *   <li>it creates additional implementation of the interface, bringing the system-wide total to 3, which can hurt
 *       JIT's decisions</li>
 * </ul>
 * While that approach would certainly be feasible and would on top of plain {@link Function}, overall it would be
 * messier, less type-safe and a perf-killer.
 */
final class LambdaDecoder {
    // FIXME: when we have JDK16: this should be a record
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

    private static final LoadingCache<Class<?>, Method> REPLACE_CACHE = CacheBuilder.newBuilder()
            .weakKeys().weakValues().build(new CacheLoader<Class<?>, Method>() {
                @Override
                public Method load(final Class<?> key) throws PrivilegedActionException {
                    return AccessController.doPrivileged((PrivilegedExceptionAction<Method>) () -> {
                        final Method method = key.getDeclaredMethod("writeReplace");
                        method.setAccessible(true);
                        return method;
                    });
                }
            });
    private static final LoadingCache<LeafReference<?, ?>, LambdaTarget> LAMBDA_CACHE = CacheBuilder.newBuilder()
            .weakKeys().build(new CacheLoader<LeafReference<?, ?>, LambdaTarget>() {
                @Override
                public LambdaTarget load(final LeafReference<?, ?> ref) throws Exception {
                    final Object replaced = REPLACE_CACHE.get(ref.getClass()).invoke(ref);
                    verify(replaced instanceof SerializedLambda, "Unexpected replaced object %s", replaced);
                    final SerializedLambda serialized = (SerializedLambda) replaced;
                    return new LambdaTarget(serialized.getImplClass().replace('/', '.'),
                        serialized.getImplMethodName());
                }
            });

    private LambdaDecoder() {
        // Hidden on purpose
    }

    static LambdaTarget resolveLambda(final LeafReference<?, ?> lambda) {
        return LAMBDA_CACHE.getUnchecked(lambda);
    }
}
