/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * A {@link DOMService} providing access to details on how the conceptual data tree
 * is distributed among providers (also known as shards). Each shard is tied to a
 * single {@link DOMDataTreeIdentifier}. Based on those data tree identifiers, the
 * shards are organized in a tree, where there is a logical parent/child relationship.
 *
 * <p>
 * It is not allowed to attach two shards to the same data tree identifier, which means
 * the mapping of each piece of information has an unambiguous home. When accessing
 * the information, the shard with the longest matching data tree identifier is used,
 * which is why this interface treats it is a prefix.
 *
 * <p>
 * Whenever a parent/child relationship is changed, the parent is notified, so it can
 * understand that a logical child has been attached.
 */
public interface DOMDataTreeShardingService extends DOMService {
    /**
     * Register a shard as responsible for a particular subtree prefix.
     *
     * @param prefix Data tree identifier, may not be null.
     * @param shard Responsible shard instance
     * @param producer Producer instance to verify namespace claim
     * @return A registration. To remove the shard's binding, close the registration.
     * @throws DOMDataTreeShardingConflictException if the prefix is already bound
     */
    <T extends DOMDataTreeShard> @NonNull ListenerRegistration<T> registerDataTreeShard(
            @NonNull DOMDataTreeIdentifier prefix, @NonNull T shard,
            @NonNull DOMDataTreeProducer producer) throws DOMDataTreeShardingConflictException;
}
