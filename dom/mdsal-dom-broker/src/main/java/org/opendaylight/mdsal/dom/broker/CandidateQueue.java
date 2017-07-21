/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

final class CandidateQueue extends ArrayDeque<DataTreeCandidate> {
    private static final long serialVersionUID = 1L;

    synchronized void append(final Collection<DataTreeCandidate> candidates) {
        super.addAll(candidates);
    }

    synchronized List<DataTreeCandidate> collect() {
        final List<DataTreeCandidate> ret = ImmutableList.copyOf(this);
        clear();
        return ret;
    }
}
