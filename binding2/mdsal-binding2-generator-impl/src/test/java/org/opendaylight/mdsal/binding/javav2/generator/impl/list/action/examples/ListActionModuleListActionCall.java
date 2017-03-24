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
 * It represents following YANG snippet:
 *
 * module list-action-module {
 *
 *   list my-list {
 *      action list-action-call {
 *
 *          input {
 *              leaf v1 {
 *                  type string;
 *              }
 *          }
 *
 *          output {
 *              leaf v2 {
 *                  type string
 *              }
 *          }
 *
 *      }
 *   }
 * }
 */
public interface ListActionModuleListActionCall extends ListAction<MyList, ListActionCallInput, ListActionCallOutput> {

    @Override
    <K> void invoke(ListActionCallInput input, KeyedInstanceIdentifier<MyList, K> kii, RpcCallback<ListActionCallOutput> callback);
}
