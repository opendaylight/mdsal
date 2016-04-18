package org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import java.util.HashMap;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer
 *
 */
public class ImportingContainerBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer> {

    private MainGroupCont _mainGroupCont;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>> augmentation = Collections.emptyMap();

    public ImportingContainerBuilder() {
    }
    public ImportingContainerBuilder(org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.MainGroup arg) {
        this._mainGroupCont = arg.getMainGroupCont();
    }

    public ImportingContainerBuilder(ImportingContainer base) {
        this._mainGroupCont = base.getMainGroupCont();
        if (base instanceof ImportingContainerImpl) {
            ImportingContainerImpl impl = (ImportingContainerImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }

    /**
     *Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.yang.gen.v1.urn.test.mainyangtest.rev160101.MainGroup</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
    */
    public void fieldsFrom(TreeNode arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.MainGroup) {
            this._mainGroupCont = ((org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.MainGroup)arg).getMainGroupCont();
            isValidArg = true;
        }
        if (!isValidArg) {
            throw new IllegalArgumentException(
              "expected one of: [org.opendaylight.yang.gen.v1.urn.test.mainyangtest.rev160101.MainGroup] \n" +
              "but was: " + arg
            );
        }
    }

    public MainGroupCont getMainGroupCont() {
        return _mainGroupCont;
    }

    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }


    public ImportingContainerBuilder setMainGroupCont(final MainGroupCont value) {
        this._mainGroupCont = value;
        return this;
    }

    public ImportingContainerBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }

        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }

        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public ImportingContainerBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public ImportingContainer build() {
        return new ImportingContainerImpl(this);
    }

    private static final class ImportingContainerImpl implements ImportingContainer {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer> implementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer.class;
        }

        private final MainGroupCont _mainGroupCont;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>> augmentation = Collections.emptyMap();

        private ImportingContainerImpl(ImportingContainerBuilder base) {
            this._mainGroupCont = base.getMainGroupCont();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public MainGroupCont getMainGroupCont() {
            return _mainGroupCont;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + Objects.hashCode(_mainGroupCont);
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
            if (!org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer.class.equals(((TreeNode)obj).implementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer other = (org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer)obj;
            if (!Objects.equals(_mainGroupCont, other.getMainGroupCont())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                ImportingContainerImpl otherImpl = (ImportingContainerImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("ImportingContainer [");
            boolean first = true;

            if (_mainGroupCont != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_mainGroupCont=");
                builder.append(_mainGroupCont);
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
