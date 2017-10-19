/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl.action.examples;

import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;

/**
 * Example Action interface.
 * It represents following YANG snippet:
 *
 * <p>
 * module my-action-module {
 *
 * <p>
 *   container my-cont {
 *      action my-action-call {
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
 *
 * <p>
 *      }
 *   }
 * }
 */
public interface MyActionModuleMyActionCallAction extends Action<MyCont, MyActionCallInput, MyActionCallOutput> {

    @Override
    void invoke(MyActionCallInput input, InstanceIdentifier<MyCont> ii, RpcCallback<MyActionCallOutput> callback);
}
