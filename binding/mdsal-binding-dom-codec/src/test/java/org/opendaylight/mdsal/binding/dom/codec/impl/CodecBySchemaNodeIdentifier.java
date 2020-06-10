/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationInputQName;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationOutputQName;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.leafBuilder;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Grpcont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.InputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.OutputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grpcont.Bar;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont.ContChoice;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont.cont.choice.ContBase;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.grp.GrpCont;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

public class CodecBySchemaNodeIdentifier extends AbstractBindingCodecTest {
    @Test
    public void testDataObjectNodeCounterpart() {
        final YangInstanceIdentifier listItemId = YangInstanceIdentifier.builder(YangInstanceIdentifier.of(Top.QNAME))
                .node(TopLevelList.QNAME)
                .nodeWithKey(TopLevelList.QNAME, QName.create(TopLevelList.QNAME, "name"), "foo")
                .build();

        final PathArgument augmentationId =
                AugmentationIdentifier.create(Set.of(ListViaUses.QNAME,
                        QName.create(ListViaUses.QNAME, "container-with-uses")));

        assertEquals(codecContext.getSubtreeCodec(SchemaNodeIdentifier.Absolute.of(Top.QNAME, TopLevelList.QNAME)),
                codecContext.getSubtreeCodec(listItemId));

        final YangInstanceIdentifier yangAugListId =
                listItemId.node(augmentationId).node(ListViaUses.QNAME);

        final SchemaNodeIdentifier.Absolute schemaAugListId = SchemaNodeIdentifier.Absolute.of(Top.QNAME,
                        TopLevelList.QNAME,
                        QName.create(ListViaUses.QNAME, "list-via-uses"));

        assertEquals(codecContext.getSubtreeCodec(schemaAugListId), codecContext.getSubtreeCodec(yangAugListId));
    }

    @Test
    public void testRetrievingChoiceNodeContext() {
        final SchemaNodeIdentifier.Absolute contId = SchemaNodeIdentifier.Absolute.of(Cont.QNAME, ContChoice.QNAME,
                ContBase.QNAME, GrpCont.QNAME);

        final BindingCodecTreeNode schemaNodeCodec = codecContext.getSubtreeCodec(contId);

        final BindingCodecTreeNode yangInstanceCodec = codecContext
                .getSubtreeCodec(YangInstanceIdentifier.create(YangInstanceIdentifier.NodeIdentifier.create(Cont.QNAME),
                        YangInstanceIdentifier.NodeIdentifier.create(ContChoice.QNAME),
                        YangInstanceIdentifier.NodeIdentifier.create(GrpCont.QNAME)));

        assertEquals(schemaNodeCodec, yangInstanceCodec);
    }

    @Test
    public void testRetrievingActionCodecContext() {
        final YangInstanceIdentifier.NodeIdentifier fooInput =
                YangInstanceIdentifier.NodeIdentifier.create(operationInputQName(Foo.QNAME.getModule()));
        final YangInstanceIdentifier.NodeIdentifier fooOutput =
                YangInstanceIdentifier.NodeIdentifier.create(operationOutputQName(Foo.QNAME.getModule()));
        final YangInstanceIdentifier.NodeIdentifier fooXyzzy =
                YangInstanceIdentifier.NodeIdentifier.create(QName.create(Foo.QNAME, "xyzzy"));
        final ContainerNode domFooInput = containerBuilder().withNodeIdentifier(fooInput)
                .withChild(leafBuilder().withNodeIdentifier(fooXyzzy).withValue("xyzzy").build())
                .build();
        final ContainerNode domFooOutput = containerBuilder().withNodeIdentifier(fooOutput).build();
        final RpcInput bindingFooInput = new InputBuilder().setXyzzy("xyzzy").build();
        final RpcOutput bindingFooOutput = new OutputBuilder().build();

        final YangInstanceIdentifier.NodeIdentifier barInput =
                YangInstanceIdentifier.NodeIdentifier.create(operationInputQName(Foo.QNAME.getModule()));
        final YangInstanceIdentifier.NodeIdentifier barOutput =
                YangInstanceIdentifier.NodeIdentifier.create(operationOutputQName(Foo.QNAME.getModule()));
        final YangInstanceIdentifier.NodeIdentifier barXyzzy =
                YangInstanceIdentifier.NodeIdentifier.create(QName.create(Bar.QNAME, "xyzzy"));
        final ContainerNode domBarInput = containerBuilder().withNodeIdentifier(barInput).build();
        final ContainerNode domBarOutput = containerBuilder().withNodeIdentifier(barOutput)
                .withChild(leafBuilder().withNodeIdentifier(barXyzzy).withValue("xyzzy").build())
                .build();
        final RpcInput bindingBarInput =
                new org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.bar.InputBuilder().build();
        final RpcOutput bindingBarOutput =
                new org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.bar.OutputBuilder().setXyzzy("xyzzy")
                        .build();

        final SchemaNodeIdentifier.Absolute inpId = SchemaNodeIdentifier.Absolute.of(QName.create(Foo.QNAME, "cont"),
                Foo.QNAME,
                QName.create(Foo.QNAME, "input"));
        final SchemaNodeIdentifier.Absolute outId = SchemaNodeIdentifier.Absolute.of(QName.create(Foo.QNAME, "cont"),
                Foo.QNAME,
                QName.create(Foo.QNAME, "output"));
        final SchemaNodeIdentifier.Absolute barInpId = SchemaNodeIdentifier.Absolute.of(Grpcont.QNAME, Bar.QNAME,
                QName.create(Bar.QNAME, "input"));
        final SchemaNodeIdentifier.Absolute barOutId = SchemaNodeIdentifier.Absolute.of(Grpcont.QNAME, Bar.QNAME,
                QName.create(Bar.QNAME, "output"));
        final DataContainerCodecContext<RpcInput, ?> inputContext = (DataContainerCodecContext<RpcInput, ?>)
                codecContext.getSubtreeCodec(inpId);
        final DataContainerCodecContext<RpcOutput, ?> outputContext = (DataContainerCodecContext<RpcOutput, ?>)
                codecContext.getSubtreeCodec(outId);

        final DataContainerCodecContext<RpcInput, ?> barInputContext = (DataContainerCodecContext<RpcInput, ?>)
                codecContext.getSubtreeCodec(barInpId);
        final DataContainerCodecContext<RpcOutput, ?> barOutputContext = (DataContainerCodecContext<RpcOutput, ?>)
                codecContext.getSubtreeCodec(barOutId);


        assertEquals(inputContext.serialize(bindingFooInput), domFooInput);
        assertEquals(outputContext.serialize(bindingFooOutput), domFooOutput);
        assertEquals(barInputContext.serialize(bindingBarInput), domBarInput);
        assertEquals(barOutputContext.serialize(bindingBarOutput), domBarOutput);
    }

    @Test
    public void testRetrievingRpcCodecContext() {
        final QName rpcQName = QName.create(KnockKnockInput.QNAME, "knock-knock");

        final KnockKnockInput bindingInput = new KnockKnockInputBuilder().setQuestion("test-question").build();
        final KnockKnockOutput bindingOutput = new KnockKnockOutputBuilder().setAnswer("test-answer").build();
        final YangInstanceIdentifier.NodeIdentifier questionId =
                YangInstanceIdentifier.NodeIdentifier.create(QName.create(KnockKnockInput.QNAME, "question"));
        final YangInstanceIdentifier.NodeIdentifier answerId =
                YangInstanceIdentifier.NodeIdentifier.create(QName.create(KnockKnockInput.QNAME, "answer"));
        final ContainerNode domInput = containerBuilder()
                .withNodeIdentifier(YangInstanceIdentifier.NodeIdentifier.create(KnockKnockInput.QNAME))
                .withChild(leafBuilder().withNodeIdentifier(questionId).withValue("test-question").build())
                .build();
        final ContainerNode domOutput = containerBuilder()
                .withNodeIdentifier(YangInstanceIdentifier.NodeIdentifier.create(KnockKnockOutput.QNAME))
                .withChild(leafBuilder().withNodeIdentifier(answerId).withValue("test-answer").build())
                .build();

        final SchemaNodeIdentifier.Absolute inpSchemaId =
                SchemaNodeIdentifier.Absolute.of(rpcQName, KnockKnockInput.QNAME);
        final SchemaNodeIdentifier.Absolute outSchemaId =
                SchemaNodeIdentifier.Absolute.of(rpcQName, KnockKnockOutput.QNAME);

        final DataContainerCodecContext<RpcInput, ?> inputCodec =
                (DataContainerCodecContext<RpcInput, ?>) codecContext.getSubtreeCodec(inpSchemaId);
        final DataContainerCodecContext<RpcOutput, ?> outputCodec =
                (DataContainerCodecContext<RpcOutput, ?>) codecContext.getSubtreeCodec(outSchemaId);

        assertEquals(inputCodec.serialize(bindingInput), domInput);
        assertEquals(outputCodec.serialize(bindingOutput), domOutput);
    }
}
