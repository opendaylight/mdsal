/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.binding.YangFeatureProvider;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;

/**
 * A collection of YANG models for testing purposes.
 *
 * @provides YangFeatureProvider
 * @provides YangModelBindingProvider
 */
open module org.opendaylight.mdsal.binding.test.model {
    requires org.opendaylight.mdsal.model.yang.ext;
    requires org.opendaylight.yang.gen.ietf.restconf.rfc8040;

    provides YangModelBindingProvider with
        org.opendaylight.yang.gen.v1.bug5446.rev151105.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.bug8449.rev170516.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.bug8903.rev170829.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.lal.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal._182.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal._300.aug.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal._300.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal._355.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal.query.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal426.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal437.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal438.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal442.keyuse.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal533.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal552.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal600.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal661.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal668.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal766.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal767.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.mdsal813.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.odl.test.binary.key.rev160101.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.rpc.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.odl.actions.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test._2.rev160111.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.opendaylight.test.bug._3090.rev160101.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.rpc.routing.rev140701.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.store.rev140422.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.test.top.via.uses.rev151112.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.typedef.empty.rev170829.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.leaf.caching.codec.rev190201.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module2.rev160101.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module4.rev160101.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal309.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal337.rev180424.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.aug.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal483.norev.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.pattern.rev170101.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.rev170101.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.unsigned.rev180408.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.$YangModelBindingProvider,
        org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101.$YangModelBindingProvider;

    provides YangFeatureProvider with
        org.opendaylight.mdsal.binding.test.model.util.Mdsal767Support;

    requires transitive org.opendaylight.yangtools.yang.binding;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires com.google.common;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static org.kohsuke.metainf_services;
    requires static java.compiler;
    requires static java.management;
}
