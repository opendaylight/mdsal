/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.runtime;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

/**
 * A serializer which writes TreeNode to supplied stream event writer.
 */
@Beta
public interface TreeNodeSerializer {

    /**
     * Writes stream events representing object to supplied stream.
     *
     * @param obj
     *            Source of stream events
     * @param stream
     *            Stream to which events should be written.
     */
    void serialize(TreeNode obj, BindingStreamEventWriter stream) throws IOException;
}