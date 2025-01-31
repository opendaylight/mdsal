/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * MD-SAL services working on {@code binding-spec} Java view of YANG-modeled data. All services defined here extend
 * {@link BindingService} and should be available through service injection. {@link MountPoint} also provides a
 * view into entities that provide such services.
 */
@org.osgi.annotation.bundle.Export
package org.opendaylight.mdsal.binding.api;