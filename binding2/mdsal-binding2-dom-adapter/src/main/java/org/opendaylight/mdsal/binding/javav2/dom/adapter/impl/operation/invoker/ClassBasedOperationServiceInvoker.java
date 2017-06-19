/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.invoker;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.yangtools.yang.common.QName;

@Beta
final class ClassBasedOperationServiceInvoker extends AbstractMappedOperationInvoker<String> {

    private static final LoadingCache<Class<?>, OperationServiceInvoker> INVOKERS =
            CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<Class<?>, OperationServiceInvoker>() {
                @Override
                public OperationServiceInvoker load(final Class<?> key) {
                    final Map<String, Method> ret = new HashMap<>();
                    for (final Method m : key.getMethods()) {
                        ret.put(m.getName(), m);
                    }

                    return new ClassBasedOperationServiceInvoker(ret);
                }
            });

    ClassBasedOperationServiceInvoker(final Map<String, Method> ret) {
        super(ret);
    }

    @Override
    protected String qnameToKey(final QName qname) {
        return JavaIdentifierNormalizer.normalizeSpecificIdentifier(qname.getLocalName(), JavaIdentifier.METHOD);
    }

    static OperationServiceInvoker instanceFor(final Class<?> type) {
        Preconditions.checkArgument(Rpc.class.isAssignableFrom(type) || Action.class.isAssignableFrom(type));
        Preconditions.checkArgument(type.isInterface());
        Preconditions.checkArgument(BindingReflections.isBindingClass(type));
        return INVOKERS.getUnchecked(type);
    }
}