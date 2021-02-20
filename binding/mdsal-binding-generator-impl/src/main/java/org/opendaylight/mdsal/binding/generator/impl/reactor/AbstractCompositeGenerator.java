/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;

/**
 * A composite generator. Composite generators may contain additional children, which end up being mapped into
 * the naming hierarchy 'under' the composite generator. To support this use case, each composite has a Java package
 * name assigned.
 */
abstract class AbstractCompositeGenerator<T extends EffectiveStatement<?, ?>> extends AbstractExplicitGenerator<T> {
    private final CollisionDomain domain = new CollisionDomain();

    private List<GroupingGenerator> usedGroupings;

    AbstractCompositeGenerator(final T statement) {
        super(statement);
        createChildren(statement, true);
    }

    AbstractCompositeGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        createChildren(statement, false);
    }

    private void createChildren(final EffectiveStatement<?, ?> statement, final boolean includeAugments) {
        final List<Generator> tmp = new ArrayList<>();
        int augmentIndex = 1;

        for (EffectiveStatement<?, ?> stmt : statement.effectiveSubstatements()) {
            final Generator child;
            if (stmt instanceof ActionEffectiveStatement) {
                child = new ActionGenerator((ActionEffectiveStatement) stmt, this);
            } else if (stmt instanceof AnydataEffectiveStatement) {
                child = new OpaqueObjectGenerator<>((AnydataEffectiveStatement) stmt, this);
            } else if (stmt instanceof AnyxmlEffectiveStatement) {
                child = new OpaqueObjectGenerator<>((AnyxmlEffectiveStatement) stmt, this);
            } else if (stmt instanceof CaseEffectiveStatement) {
                child = new CaseGenerator((CaseEffectiveStatement) stmt, this);
            } else if (stmt instanceof ChoiceEffectiveStatement) {
                child = new ChoiceGenerator((ChoiceEffectiveStatement) stmt, this);
            } else if (stmt instanceof ContainerEffectiveStatement) {
                child = new ContainerGenerator((ContainerEffectiveStatement) stmt, this);
            } else if (stmt instanceof GroupingEffectiveStatement) {
                child = new GroupingGenerator((GroupingEffectiveStatement) stmt, this);
            } else if (stmt instanceof IdentityEffectiveStatement) {
                child = new IdentityGenerator((IdentityEffectiveStatement) stmt, this);
            } else if (stmt instanceof InputEffectiveStatement) {
                child = new InputGenerator((InputEffectiveStatement) stmt, this);
            } else if (stmt instanceof LeafEffectiveStatement) {
                child = new LeafGenerator((LeafEffectiveStatement) stmt, this);
            } else if (stmt instanceof LeafListEffectiveStatement) {
                child = new LeafListGenerator((LeafListEffectiveStatement) stmt, this);
            } else if (stmt instanceof ListEffectiveStatement) {
                child = new ListGenerator((ListEffectiveStatement) stmt, this);
            } else if (stmt instanceof NotificationEffectiveStatement) {
                child = new NotificationGenerator((NotificationEffectiveStatement) stmt, this);
            } else if (stmt instanceof OutputEffectiveStatement) {
                child = new OutputGenerator((OutputEffectiveStatement) stmt, this);
            } else if (stmt instanceof RpcEffectiveStatement) {
                child = new RpcGenerator((RpcEffectiveStatement) stmt, this);
            } else if (stmt instanceof TypedefEffectiveStatement) {
                child = new TypedefGenerator((TypedefEffectiveStatement) stmt, this);
            } else if (stmt instanceof AugmentEffectiveStatement && includeAugments) {
                child = new AugmentGenerator((AugmentEffectiveStatement) stmt, this, augmentIndex++);
            } else {
                continue;
            }

            tmp.add(child);
        }

        addChildren(tmp);
    }

    final CollisionDomain domain() {
        return domain;
    }

    void linkCompositeDependencies(final GeneratorContext context) {
        usedGroupings = statement().streamEffectiveSubstatements(UsesEffectiveStatement.class)
            .map(uses -> context.resolveTreeScoped(GroupingGenerator.class, uses.argument()))
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Update the specified builder to implement interfaces generated for the {@code grouping} statements this generator
     * is using.
     *
     * @param builder Target builder
     * @param builderFactory factory for creating {@link TypeBuilder}s
     * @return The number of groupings this type uses.
     */
    final int addUsesInterfaces(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
        for (GroupingGenerator grp : usedGroupings) {
            builder.addImplementsType(grp.createType(builderFactory));
        }
        return usedGroupings.size();
    }

//    final void setChildPackageNames() {
//        final String myPackage = javaPackage();
//
//        // FIXME: this does not deal with conflicts yet
//        for (Generator child : this) {
//            if (child instanceof AbstractCompositeGenerator) {
//                final AbstractCompositeGenerator<?> composite = (AbstractCompositeGenerator<?>) child;
//                if (!composite.isEmpty()) {
//                    final String pkg = myPackage + '.' + child.preferredSubpackage();
//                    composite.setJavaPackage(BindingMapping.normalizePackageName(pkg));
//                    composite.setChildPackageNames();
//                }
//            }
//        }
//    }
}
