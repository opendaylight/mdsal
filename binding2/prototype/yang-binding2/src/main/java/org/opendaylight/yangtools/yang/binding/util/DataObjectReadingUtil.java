/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.InterfaceTyped;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import org.opendaylight.yangtools.yang.binding.IdentifiableListItem;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

public class DataObjectReadingUtil {

    private DataObjectReadingUtil() {
        throw new UnsupportedOperationException("Utility class. Instantion is not allowed.");
    }

    /**
     *
     * @param parent
     *            Parent object on which read operation will be performed
     * @param parentPath
     *            Path, to parent object.
     * @param childPath
     *            Path, which is nested to parent, and should be readed.
     * @return Value of object.
     */
    public static final <T extends TreeNode, P extends TreeNode> Map<InstanceIdentifier<T>, T> readData(final P parent,
                                                                                                        final InstanceIdentifier<P> parentPath, final InstanceIdentifier<T> childPath) {
        checkArgument(parent != null, "Parent must not be null.");
        checkArgument(parentPath != null, "Parent path must not be null");
        checkArgument(childPath != null, "Child path must not be null");
        checkArgument(parentPath.containsWildcarded(childPath), "Parent object must be parent of child.");

        List<PathArgument> pathArgs = subList(parentPath.getPathArguments(), childPath.getPathArguments());
        @SuppressWarnings("rawtypes")
        Map<InstanceIdentifier, InterfaceTyped> lastFound = Collections
                .<InstanceIdentifier, InterfaceTyped> singletonMap(parentPath, parent);
        for (PathArgument pathArgument : pathArgs) {
            @SuppressWarnings("rawtypes")
            final ImmutableMap.Builder<InstanceIdentifier, InterfaceTyped> potentialBuilder = ImmutableMap.builder();
            for (@SuppressWarnings("rawtypes")
            Entry<InstanceIdentifier, InterfaceTyped> entry : lastFound.entrySet()) {
                potentialBuilder.putAll(readData(entry, pathArgument));
            }
            lastFound = potentialBuilder.build();
            if (lastFound.isEmpty()) {
                return Collections.emptyMap();
            }
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Map<InstanceIdentifier<T>, T> result = (Map) lastFound;
        return result;
    }

    @SuppressWarnings("rawtypes")
    private static Map<InstanceIdentifier, InterfaceTyped> readData(final Entry<InstanceIdentifier, InterfaceTyped> entry,
                                                                    final PathArgument pathArgument) {
        return readData(entry.getValue(), entry.getKey(), pathArgument);
    }

    public static final <T extends TreeNode> Optional<T> readData(final TreeNode source, final Class<T> child) {
        checkArgument(source != null, "Object should not be null.");
        checkArgument(child != null, "Child type should not be null");
        Class<? extends InterfaceTyped> parentClass = source.implementedInterface();

        @SuppressWarnings("unchecked")
        T potential = (T) resolveReadStrategy(parentClass, child).read(source, child);
        return Optional.fromNullable(potential);
    }

    @SuppressWarnings("rawtypes")
    private static final Map<InstanceIdentifier, InterfaceTyped> readData(final InterfaceTyped parent,
                                                                          final InstanceIdentifier parentPath, final PathArgument child) {
        checkArgument(parent != null, "Object should not be null.");
        checkArgument(child != null, "Child argument should not be null");
        Class<? extends InterfaceTyped> parentClass = parent.implementedInterface();
        return resolveReadStrategy(parentClass, child.getType()).readUsingPathArgument(parent, child, parentPath);
    }

    private static DataObjectReadingStrategy resolveReadStrategy(final Class<? extends InterfaceTyped> parentClass,
            final Class<? extends InterfaceTyped> type) {

        DataObjectReadingStrategy strategy = createReadStrategy(parentClass, type);
        // FIXME: Add caching of strategies
        return strategy;
    }

    private static DataObjectReadingStrategy createReadStrategy(final Class<? extends InterfaceTyped> parent,
            final Class<? extends InterfaceTyped> child) {

        if (Augmentable.class.isAssignableFrom(parent) && Augmentation.class.isAssignableFrom(child)) {
            return REAUSABLE_AUGMENTATION_READING_STRATEGY;
        }

        /*
         * FIXME Ensure that this strategies also works for children of cases.
         * Possible edge-case is : Parent container uses grouping foo case is
         * added by augmentation also uses foo.
         */
        if (IdentifiableListItem.class.isAssignableFrom(child)) {
            @SuppressWarnings("unchecked")
            final Class<? extends IdentifiableListItem<?>> identifiableClass = (Class<? extends IdentifiableListItem<?>>) child;
            return new ListItemReadingStrategy(parent, identifiableClass);
        }
        return new ContainerReadingStrategy(parent, child);
    }

    private static Method resolveGetterMethod(final Class<? extends InterfaceTyped> parent, final Class<?> child) {
        String methodName = "get" + child.getSimpleName();
        try {
            return parent.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    private static abstract class DataObjectReadingStrategy {

        private final Class<? extends InterfaceTyped> parentType;
        private final Class<? extends InterfaceTyped> childType;
        private final Method getterMethod;

        @SuppressWarnings("unchecked")
        public DataObjectReadingStrategy(final Class parentType, final Class childType) {
            super();
            checkArgument(InterfaceTyped.class.isAssignableFrom(parentType));
            checkArgument(InterfaceTyped.class.isAssignableFrom(childType));
            this.parentType = parentType;
            this.childType = childType;
            this.getterMethod = resolveGetterMethod(parentType, childType);
        }

        @SuppressWarnings("unchecked")
        public DataObjectReadingStrategy(final Class parentType, final Class childType, final Method getter) {
            this.parentType = parentType;
            this.childType = childType;
            this.getterMethod = getter;
        }

        @SuppressWarnings("unused")
        protected Class<? extends InterfaceTyped> getParentType() {
            return parentType;
        }

        protected Class<? extends InterfaceTyped> getChildType() {
            return childType;
        }

        protected Method getGetterMethod() {
            return getterMethod;
        }

        public abstract Map<InstanceIdentifier, InterfaceTyped> readUsingPathArgument(InterfaceTyped parent,
                                                                                      PathArgument childArgument, InstanceIdentifier targetBuilder);

        public abstract InterfaceTyped read(InterfaceTyped parent, Class<?> childType);

    }

    @SuppressWarnings("rawtypes")
    private static class ContainerReadingStrategy extends DataObjectReadingStrategy {

        public ContainerReadingStrategy(final Class<? extends InterfaceTyped> parent, final Class<? extends InterfaceTyped> child) {
            super(parent, child);
            checkArgument(child.isAssignableFrom(getGetterMethod().getReturnType()));
        }

        @Override
        public Map<InstanceIdentifier, InterfaceTyped> readUsingPathArgument(final InterfaceTyped parent,
                                                                             final PathArgument childArgument, final InstanceIdentifier parentPath) {
            final InterfaceTyped result = read(parent, childArgument.getType());
            if (result != null) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier childPath = parentPath.child(childArgument.getType());
                return Collections.singletonMap(childPath, result);
            }
            return Collections.emptyMap();
        }

        @Override
        public InterfaceTyped read(final InterfaceTyped parent, final Class<?> childType) {
            try {
                Object potentialData = getGetterMethod().invoke(parent);
                checkState(potentialData instanceof InterfaceTyped);
                return (InterfaceTyped) potentialData;

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static class ListItemReadingStrategy extends DataObjectReadingStrategy {

        public ListItemReadingStrategy(final Class<? extends InterfaceTyped> parent, final Class child) {
            super(parent, child);
            checkArgument(Iterable.class.isAssignableFrom(getGetterMethod().getReturnType()));
        }

        @Override
        public InterfaceTyped read(final InterfaceTyped parent, final Class<?> childType) {
            // This will always fail since we do not have key.
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<InstanceIdentifier, InterfaceTyped> readUsingPathArgument(final InterfaceTyped parent,
                                                                             final PathArgument childArgument, final InstanceIdentifier builder) {
            try {
                Object potentialList = getGetterMethod().invoke(parent);
                if (potentialList instanceof Iterable) {

                    final Iterable<IdentifiableListItem> dataList = (Iterable<IdentifiableListItem>) potentialList;
                    if (childArgument instanceof IdentifiableItem<?, ?>) {
                        return readUsingIdentifiableItem(dataList, (IdentifiableItem) childArgument, builder);
                    } else {
                        return readAll(dataList, builder);
                    }
                }
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e);
            }
            return Collections.emptyMap();
        }

        private Map<InstanceIdentifier, InterfaceTyped> readAll(final Iterable<IdentifiableListItem> dataList,
                                                                final InstanceIdentifier parentPath) {
            Builder<InstanceIdentifier, InterfaceTyped> result = ImmutableMap
                    .<InstanceIdentifier, InterfaceTyped> builder();
            for (IdentifiableListItem item : dataList) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier childPath = parentPath.child(getChildType(), item.identifier());
                result.put(childPath, (InterfaceTyped) item);
            }
            return result.build();
        }

        @SuppressWarnings("unchecked")
        private static Map<InstanceIdentifier, InterfaceTyped> readUsingIdentifiableItem(final Iterable<IdentifiableListItem> dataList,
                                                                                         final IdentifiableItem childArgument, final InstanceIdentifier parentPath) {
            final Identifier<?> key = childArgument.getKey();
            for (IdentifiableListItem item : dataList) {
                if (key.equals(item.identifier()) && item instanceof InterfaceTyped) {
                    checkState(childArgument.getType().isInstance(item),
                            "Found child is not instance of requested type");
                    InstanceIdentifier childPath = parentPath
                            .child(childArgument.getType(), item.identifier());
                    return Collections.singletonMap(childPath, (InterfaceTyped) item);
                }
            }
            return Collections.emptyMap();
        }

    }

    private static final DataObjectReadingStrategy REAUSABLE_AUGMENTATION_READING_STRATEGY = new AugmentationReadingStrategy();

    private static final class AugmentationReadingStrategy extends DataObjectReadingStrategy {

        public AugmentationReadingStrategy() {
            super(Augmentable.class, Augmentation.class, null);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Map<InstanceIdentifier, InterfaceTyped> readUsingPathArgument(final InterfaceTyped parent,
                                                                             final PathArgument childArgument, final InstanceIdentifier builder) {
            checkArgument(childArgument instanceof Item<?>, "Path Argument must be Item without keys");
            InterfaceTyped aug = read(parent, childArgument.getType());
            if (aug != null) {
                @SuppressWarnings("unchecked")
                final InstanceIdentifier childPath = builder.child(childArgument.getType());
                return Collections.singletonMap(childPath, aug);
            } else {
                return Collections.emptyMap();
            }
        }

        @Override
        public InterfaceTyped read(final InterfaceTyped parent, final Class<?> childType) {
            checkArgument(Augmentation.class.isAssignableFrom(childType), "Parent must be Augmentable.");
            checkArgument(parent instanceof Augmentable<?>, "Parent must be Augmentable.");

            @SuppressWarnings({ "rawtypes", "unchecked" })
            Augmentation potential = ((Augmentable) parent).getAugmentation(childType);
            checkState(potential instanceof InterfaceTyped, "Readed augmention must be data object");
            return (InterfaceTyped) potential;
        }
    }

    /**
     * Create sublist view of child from element on [size-of-parent] position to
     * last element.
     *
     * @param parent
     * @param child
     * @return sublist view of child argument
     * @throws IllegalArgumentException
     *             if parent argument is bigger than child
     */
    private static <P, C> List<C> subList(final Iterable<P> parent, final Iterable<C> child) {
        Iterator<P> iParent = parent.iterator();
        List<C> result = new ArrayList<>();
        for (C arg : child) {
            if (iParent.hasNext()) {
                iParent.next();
            } else {
                result.add(arg);
            }
        }
        if (iParent.hasNext()) {
            throw new IllegalArgumentException("Parent argument is bigger than child.");
        }
        return result;
    }

}
