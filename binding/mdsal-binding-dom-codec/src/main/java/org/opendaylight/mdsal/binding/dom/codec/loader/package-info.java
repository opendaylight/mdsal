/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * {@link java.lang.ClassLoader} support for Binding/DOM codec translation code generators. This package provides two
 * core classes:
 * <ul>
 * <li>{@link org.opendaylight.mdsal.binding.dom.codec.loader.StaticClassPool}, which is allows lookup of classes within
 *     Binding/DOM codec for the purpose of referencing them within code generators.</li>
 * <li>{@link org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader}, which allows lookup of
 *     compile-time-generated Binding classes for the purpose of referencing them within code generators and which
 *     serves as the ClassLoader holding runtime-generated codecs.
 * </li>
 */
package org.opendaylight.mdsal.binding.dom.codec.loader;