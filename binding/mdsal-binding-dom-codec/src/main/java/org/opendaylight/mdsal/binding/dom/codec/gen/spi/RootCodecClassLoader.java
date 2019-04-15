/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.spi;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import javassist.CtClass;
import javassist.NotFoundException;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RootCodecClassLoader extends CodecClassLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CodecClassLoader.class);

    static {
        verify(registerAsParallelCapable());
    }

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<RootCodecClassLoader, ImmutableMap> LOADERS_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(RootCodecClassLoader.class, ImmutableMap.class, "loaders");

    private volatile ImmutableMap<ClassLoader, CodecClassLoader> loaders = ImmutableMap.of();

    RootCodecClassLoader() {
        super(CodecClassLoader.class.getClassLoader());
    }

    @Override
    CodecClassLoader findClassLoader(final Class<?> bindingClass) {
        final ClassLoader target = bindingClass.getClassLoader();

        // Cache for update
        ImmutableMap<ClassLoader, CodecClassLoader> local = loaders;
        final CodecClassLoader known = local.get(target);
        if (known != null) {
            return known;
        }

        // Alright, we need to determine if the class is accessible through our hierarchy (in which case we use
        // ourselves) or we need to create a new Leaf.
        final CodecClassLoader found;
        if (!isOurClass(bindingClass)) {
            verifyClassLoader(target);
            found = new LeafCodecClassLoader(this, target);
        } else {
            found = this;
        }

        // Now make sure we cache this result
        while (true) {
            final Builder<ClassLoader, CodecClassLoader> builder = ImmutableMap.builderWithExpectedSize(
                local.size() + 1);
            builder.putAll(local);
            builder.put(target, found);

            if (LOADERS_UPDATER.compareAndSet(this, local, builder.build())) {
                return found;
            }

            local = loaders;
            final CodecClassLoader recheck = local.get(target);
            if (recheck != null) {
                return recheck;
            }
        }
    }

    @Override
    public CtClass findCodecClass(final Class<?> codecClass) throws NotFoundException {
        return getLocalFrozen(codecClass.getName());
    }

    private boolean isOurClass(final Class<?> bindingClass) {
        final Class<?> ourClass;
        try {
            ourClass = loadClass(bindingClass.getName(), false);
        } catch (ClassNotFoundException e) {
            LOG.debug("Failed to load class {}", e);
            return false;
        }
        return bindingClass.equals(ourClass);
    }

    private static void verifyClassLoader(final ClassLoader target) {
        // Sanity check: target has to resolve yang-binding contents to the same class, otherwise we are in a pickle
        final Class<?> targetClazz;
        try {
            targetClazz = target.loadClass(DataContainer.class.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("ClassLoader " + target + " cannot load " + DataContainer.class, e);
        }
        verify(DataContainer.class.equals(targetClazz),
            "Class mismatch on DataContainer. Ours is from %s, target %s has %s from %s",
            DataContainer.class.getClassLoader(), target, targetClazz, targetClazz.getClassLoader());
    }
}