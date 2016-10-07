/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.model.api;

/**
 * Enum definition which provides four access modifiers that are described
 * in Java programming language (Default, Private, Protected, Public).
 *
 * @deprecated Use {@link org.opendaylight.mdsal.binding.model.api.AccessModifier} instead.
 */
@Deprecated
public enum AccessModifier {
    DEFAULT, PRIVATE, PUBLIC, PROTECTED
}
