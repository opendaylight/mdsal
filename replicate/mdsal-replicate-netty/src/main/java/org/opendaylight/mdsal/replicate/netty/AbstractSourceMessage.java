/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static java.util.Objects.requireNonNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import org.opendaylight.yangtools.yang.data.codec.binfmt.DataTreeCandidateInputOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeStreamVersion;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;

abstract class AbstractSourceMessage {
    private static final class Empty extends AbstractSourceMessage {
        @Override
        void encodeTo(final NormalizedNodeStreamVersion version, final List<Object> out) throws IOException {
            out.add(Constants.EMPTY_DATA);
        }
    }

    private static final class Deltas extends AbstractSourceMessage {
        private final List<DataTreeCandidate> deltas;

        Deltas(final List<DataTreeCandidate> deltas) {
            this.deltas = requireNonNull(deltas);
        }

        @Override
        void encodeTo(final NormalizedNodeStreamVersion version, final List<Object> out) throws IOException {
            for (DataTreeCandidate candidate : deltas) {
                try (DataOutputStream stream = new DataOutputStream(new SplittingOutputStream(out))) {
                    try (NormalizedNodeDataOutput output = version.newDataOutput(stream)) {
                        DataTreeCandidateInputOutput.writeDataTreeCandidate(output, candidate);
                    }
                }
                out.add(Constants.DTC_APPLY);
            }
        }
    }

    private static final AbstractSourceMessage EMPTY = new Empty();

    static AbstractSourceMessage empty() {
        return EMPTY;
    }

    static AbstractSourceMessage of(final List<DataTreeCandidate> deltas) {
        return new Deltas(deltas);
    }

    abstract void encodeTo(NormalizedNodeStreamVersion version, List<Object> out) throws IOException;
}
