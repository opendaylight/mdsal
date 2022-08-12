/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.common.annotations.Beta;

/**
 * Completeness of a Java class. A class can be abstract or final or non-final (default in-between). Note this is
 * distinct use of where {@code abstract}, {@code final}.
 */
@Beta
public enum JavaClassCompleteness {
    /**
     * Abstract class. Also applies to interface, but we do not model that right now.
     */
    ABSTRACT,
    /**
     * Non-final class. May apply to other constructs, but we do not model that here. This construct has not relevance
     * to {@code default} methods.
     */
    DEFAULT,
    /**
     * Final class.
     */
    FINAL;
}