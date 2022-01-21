/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import com.google.common.base.MoreObjects;

/**
 * Shared structure tracking progress of a linkage pass.
 */
final class LinkageProgress {
    private boolean retry;
    private int originals;
    private int augments;

    boolean hasProgress() {
        return augments != 0 || originals != 0;
    }

    boolean retry() {
        return retry;
    }

    void setRetry() {
        retry = true;
    }

    void linkedOriginal() {
        originals++;
    }

    void resolvedAugment() {
        augments++;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("retry", retry)
            .add("augments", augments)
            .add("originals", originals)
            .toString();
    }
}
