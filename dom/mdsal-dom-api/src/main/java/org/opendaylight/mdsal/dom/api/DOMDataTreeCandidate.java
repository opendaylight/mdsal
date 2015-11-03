/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;

/**
 * An encapsulation of a data tree modification. This candidate is ready for atomic commit to the
 * data broker. It allows access to before- and after-state as it will be seen in to subsequent
 * commit. This capture can be accessed for reference, but cannot be modified and the content is
 * limited to nodes which were affected by the modification from which this instance originated.
 */
public interface DOMDataTreeCandidate {

    /**
     * Get the candidate tree root path. This is the path of the root node relative to the root of
     * InstanceIdentifier namespace.
     *
     * @return Relative path of the root node
     */
    DOMDataTreeIdentifier getRootPath();

    /**
     * Get the candidate tree root node.
     *
     * @return Candidate tree root node
     */
    DataTreeCandidateNode getRootNode();
}
