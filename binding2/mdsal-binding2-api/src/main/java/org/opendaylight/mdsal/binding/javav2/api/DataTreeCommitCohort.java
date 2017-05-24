/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import java.util.function.BiConsumer;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.DataValidationFailedException;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;

@Beta
public interface DataTreeCommitCohort<T extends TreeNode> {

    void canCommit(Object txId, DataTreeModification<T> modification, BiConsumer<DataValidationFailedException,
        PostCanCommitStep> callback);
}
