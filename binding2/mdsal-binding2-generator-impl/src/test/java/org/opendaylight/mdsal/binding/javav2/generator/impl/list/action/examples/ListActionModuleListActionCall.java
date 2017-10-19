/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl.list.action.examples;

import org.opendaylight.mdsal.binding.javav2.spec.base.KeyedInstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.ListAction;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;

/**
 * Example Action interface.
 * It represents following YANG snippet:
 *
 * <p>
 * module list-action-module {
 *
 * <p>
 *   list my-list {
 *      action list-action-call {
 *
 * <p>
 *          input {
 *              leaf v1 {
 *                  type string;
 *              }
 *          }
 *
 * <p>
 *          output {
 *              leaf v2 {
 *                  type string
 *              }
 *          }
 *      }
 *   }
 * }
 */
public interface ListActionModuleListActionCall extends ListAction<MyList, ListActionCallInput, ListActionCallOutput> {

    @Override
    <K> void invoke(ListActionCallInput input, KeyedInstanceIdentifier<MyList, K> kii,
            RpcCallback<ListActionCallOutput> callback);
}
