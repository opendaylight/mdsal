package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered
 *
 */
public class MainGroupContListUnorderedBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered> {

    private MainGroupContListUnorderedKey _key;
    private java.lang.String _name2;
    private java.lang.String _type2;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>> augmentation = Collections.emptyMap();

    public MainGroupContListUnorderedBuilder() {
    }

    public MainGroupContListUnorderedBuilder(MainGroupContListUnordered base) {
        if (base.identifier() == null) {
            this._key = new MainGroupContListUnorderedKey(
                base.getName2()
            );
            this._name2 = base.getName2();
        } else {
            this._key = base.identifier();
            this._name2 = _key.getName2();
        }
        this._type2 = base.getType2();
        if (base instanceof MainGroupContListUnorderedImpl) {
            MainGroupContListUnorderedImpl impl = (MainGroupContListUnorderedImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }


    public MainGroupContListUnorderedKey getKey() {
        return _key;
    }

    public java.lang.String getName2() {
        return _name2;
    }

    public java.lang.String getType2() {
        return _type2;
    }

    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

     
    public MainGroupContListUnorderedBuilder setKey(final MainGroupContListUnorderedKey value) {
        this._key = value;
        return this;
    }


    public MainGroupContListUnorderedBuilder setName2(final java.lang.String value) {
        this._name2 = value;
        return this;
    }


    public MainGroupContListUnorderedBuilder setType2(final java.lang.String value) {
        this._type2 = value;
        return this;
    }

    public MainGroupContListUnorderedBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }

        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }

        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public MainGroupContListUnorderedBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public MainGroupContListUnordered build() {
        return new MainGroupContListUnorderedImpl(this);
    }

    private static final class MainGroupContListUnorderedImpl implements MainGroupContListUnordered {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered> implementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered.class;
        }

        private final MainGroupContListUnorderedKey _key;
        private final java.lang.String _name2;
        private final java.lang.String _type2;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>> augmentation = Collections.emptyMap();

        private MainGroupContListUnorderedImpl(MainGroupContListUnorderedBuilder base) {
            if (base.getKey() == null) {
                this._key = new MainGroupContListUnorderedKey(
                    base.getName2()
                );
                this._name2 = base.getName2();
            } else {
                this._key = base.getKey();
                this._name2 = _key.getName2();
            }
            this._type2 = base.getType2();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public MainGroupContListUnorderedKey identifier() {
            return _key;
        }

        @Override
        public java.lang.String getName2() {
            return _name2;
        }

        @Override
        public java.lang.String getType2() {
            return _type2;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + Objects.hashCode(_key);
            result = prime * result + Objects.hashCode(_name2);
            result = prime * result + Objects.hashCode(_type2);
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
            if (!org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered.class.equals(((TreeNode)obj).implementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered other = (org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered)obj;
            if (!Objects.equals(_key, other.identifier())) {
                return false;
            }
            if (!Objects.equals(_name2, other.getName2())) {
                return false;
            }
            if (!Objects.equals(_type2, other.getType2())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                MainGroupContListUnorderedImpl otherImpl = (MainGroupContListUnorderedImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("MainGroupContListUnordered [");
            boolean first = true;

            if (_key != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_key=");
                builder.append(_key);
             }
            if (_name2 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_name2=");
                builder.append(_name2);
             }
            if (_type2 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_type2=");
                builder.append(_type2);
             }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
