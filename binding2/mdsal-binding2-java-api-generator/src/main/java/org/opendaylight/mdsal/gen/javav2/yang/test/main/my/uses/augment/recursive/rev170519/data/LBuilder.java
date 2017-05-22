package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data;

import org.opendaylight.mdsal.binding.javav2.spec.structural.AugmentationHolder;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import com.google.common.collect.ClassToInstanceMap;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.UsesAugmentRecursiveData;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.A.A1;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;

/**
 * Class that builds {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L} instances.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L
 */
public class LBuilder implements Builder <org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L> {

    private A1 _a1;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>> augmentation = Collections.emptyMap();

    public LBuilder() {
    }

    public LBuilder( org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.grp.A arg) {
        this._a1 = arg.getA1();
    }

    public LBuilder (L base) {
        this._a1 = base.getA1();
        if (base instanceof LImpl) {
            LImpl impl = (LImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L> casted =(AugmentationHolder<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }

    /**
     *Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.grp.A</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
     */
    public void fieldsFrom(TreeNode arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.grp.A) {
            isValidArg = true;
        }
        if (!isValidArg) {
            throw new IllegalArgumentException( "expected one of: [org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.grp.A] \n" + "but was: " + arg );
        }
    }

    public A1 getA1() {
        return _a1;
    }

    @SuppressWarnings("unchecked")
    public <E extends Augmentation<? super L>> E getAugmentation (java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public LBuilder setA1(final A1 value) {
        this._a1 = value;
        return this;
    }

    public LBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>> augmentationType, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public LBuilder removeAugmentation (java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public L build() {
        return new LImpl(this);
    }

    private static final class LImpl implements L {
        @Override
        public java.lang.Class<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L> implementedInterface() {
            return org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L.class;
        }
        private  final A1 _a1;
        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>> augmentation = Collections.emptyMap();
        private LImpl (LBuilder base) {
            this._a1 = base.getA1();
            switch (base.augmentation.size()) {
                case 0: this.augmentation = Collections.emptyMap();
                break;
                case 1: final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>> singletonMap(e.getKey(), e.getValue());
                break;
                default : this.augmentation = new HashMap<>(base.augmentation);
            }
        }
        @Override
        public A1 getA1() {
            return _a1;
        }
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<? super L>> E getAugmentation (java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }
        private int hash = 0;
        private volatile boolean hashValid = false;
        @Override
        public int hashCode() {
            if (hashValid) {
                return hash;
            }
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(_a1);
            result = prime * result + Objects.hashCode(augmentation);
            hash = result;
            hashValid = true;
            return result;
        }
        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TreeNode)) {
                return false;
            }
            if (!(obj instanceof Instantiable)) {
                return false;
            }
            if (!org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L.class.equals(((Instantiable)obj) .implementedInterface())) {
                return false;
            }
            org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L other = (org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L)obj;
            if (!Objects.equals(_a1, other.getA1())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                LImpl otherImpl = (LImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }
        @Override
        public java.lang.String toString() {
            java.lang.String name = "L [";
            java.lang.StringBuilder builder = new java.lang.StringBuilder(name);
            if (_a1 != null) {
                builder.append("_a1=");
                builder.append(_a1);
            }
            final int builderLength = builder.length();
            final int builderAdditionalLength = builder.substring(name.length(), builderLength).length();
            if (builderAdditionalLength > 2 && !builder.substring(builderLength - 2, builderLength).equals(", ")) {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
        @Override
        public Item<UsesAugmentRecursiveData> treeIdentifier() {
            //TODO implement
            return null;
        }
        @Override
        public ClassToInstanceMap<Augmentation<? super L>> augments() {
            //TODO implement
            return null;
        }
    }

}
