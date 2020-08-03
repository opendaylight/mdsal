/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import ch.vorburger.xtendbeans.XtendBeanGenerator;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.Iterables;
import java.util.Comparator;
import java.util.Optional;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * {@link XtendBeanGenerator} customized for YANG beans stored in MD SAL
 * DataBroker.
 *
 * <p>This is required: (a) because YANG model DataObject beans (when read from a
 * DataBroker) are funky java.lang.reflect.Proxy instances, and XtendBeanGenerator
 * cannot find the Builder or the property getters for them without a bit of help,
 * which this class provides;
 *
 * <p>(b) to integrate it with the {@link XtendBuilderExtensions}
 * (for ">>" instead of "->" and no build() method calls);
 *
 * <p>(c) to integrate it with the {@link AugmentableExtension}.
 *
 * @see XtendBeanGenerator
 *
 * @author Michael Vorburger
 */
// package-local: no need to expose this, consider it an implementation detail; public API is the AssertDataObjects
class XtendYangBeanGenerator extends XtendBeanGenerator {

    @Override
    public String getExpression(final Object bean) {
        final String beanText = super.getExpression(bean);
        if (useBuilderExtensions(bean)) {
            return new StringBuilder("import static extension ").append(XtendBuilderExtensions.class.getName())
                    .append(".operator_doubleGreaterThan\n\n").append(beanText).toString();
        }
        return beanText;
    }

    @Override
    protected boolean isUsingBuilder(final Object bean, final Class<?> builderClass) {
        return useBuilderExtensions(bean) ? false : super.isUsingBuilder(bean, builderClass);
    }

    @Override
    protected String getOperator(final Object bean, final Class<?> builderClass) {
        return useBuilderExtensions(bean) ? ">>" : super.getOperator(bean, builderClass);
    }

    @Override
    protected CharSequence getNewBeanExpression(final Object bean) {
        if (bean instanceof DataContainer) {
            DataContainer dataContainerBean = (DataContainer) bean;
            Optional<Class<?>> optBuilderClass = getOptionalBuilderClassByAppendingBuilderToClassName(
                    dataContainerBean.implementedInterface());
            if (optBuilderClass.isEmpty()) {
                throw new IllegalArgumentException("DataContainer has no *Builder class: " + bean.getClass());
            }
            return super.getNewBeanExpression(dataContainerBean, optBuilderClass.get());
        }
        return super.getNewBeanExpression(bean);
    }

    @Override
    protected String stringify(final Class<?> klass) {
        return klass.getSimpleName();
    }

    @Override
    protected Iterable<Property> filter(final Iterable<Property> properties) {
        // YANG keys duplicate existing other properties (there are getter/setter for both), so filter them
        return Iterables.filter(properties, property -> !property.getName().equals("key"));
    }

    private static Optional<ClassToInstanceMap<Augmentation<?>>> getAugmentations(final Object bean) {
        if (bean instanceof Augmentable<?>) {
            ClassToInstanceMap<Augmentation<?>> augmentables = augmentations((Augmentable<?>) bean);
            if (!augmentables.isEmpty()) {
                return Optional.of(augmentables);
            }
        }
        return Optional.empty();
    }

    private static ClassToInstanceMap<Augmentation<?>> augmentations(final Augmentable<?> augmentable) {
        return ImmutableClassToInstanceMap.copyOf(augmentable.augmentations());
    }

    @Override
    protected CharSequence getAdditionalInitializationExpression(final Object bean, final Class<?> builderClass) {
        Optional<ClassToInstanceMap<Augmentation<?>>> optional = getAugmentations(bean);
        if (optional.isPresent()) {
            StringBuilder sb = new StringBuilder();
            optional.get().entrySet().stream()
                // We sort the augmentations by Class type, because the Map has unpredictable order:
                .sorted(Comparator.comparing(e2 -> e2.getKey().getName()))
                .forEachOrdered(e -> {
                    sb.append("addAugmentation(").append(stringify(e.getKey())).append(", ")
                        .append(getNewBeanExpression(e.getValue())).append(')');
                });
            return sb;
        }
        return "";
    }

/*
    TODO activate this once YANG objects either have a setAugmentations(Map)
      or implement a new TBD interface AugmentableBuilder with a method like:
          <E extends Augmentation<T>> Builder? addAugmentation(Class<E> augmentationType, E augmentation);
      which an extension method could jump on.

    @Override
    public Iterable<Property> getAdditionalSpecialProperties(Object bean, Class<?> builderClass) {
        Optional<ClassToInstanceMap<Augmentation<?>>> optional = getAugmentations(bean);
        if (optional.isPresent()) {
            Property augmentableProperty = new Property("augmentations", true, Map.class, () -> optional.get(), null);
            return Collections.singleton(augmentableProperty);
        } else {
            return Collections.emptyList();
        }
    }
 */

    private static boolean useBuilderExtensions(final Object bean) {
        return bean instanceof DataObject;
    }
}
