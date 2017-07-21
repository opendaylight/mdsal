package org.opendaylight.mdsal.dom.broker;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

final class CandidateQueue extends ArrayDeque<DataTreeCandidate> {
    private static final long serialVersionUID = 1L;

    private transient volatile boolean dirty = false;

    boolean isDirty() {
        return dirty;
    }

    synchronized void append(final Collection<DataTreeCandidate> candidates) {
        super.addAll(candidates);
        dirty = true;
    }

    synchronized List<DataTreeCandidate> collect() {
        final List<DataTreeCandidate> ret = ImmutableList.copyOf(this);
        clear();
        dirty = false;
        return ret;
    }
}