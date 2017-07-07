/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.runtime.reflection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.spec.base.BaseIdentity;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.YangModelBindingProvider;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.YangModuleInfo;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class BindingReflections {

    private static final Logger LOG = LoggerFactory.getLogger(BindingReflections.class);

    private static final long EXPIRATION_TIME = 60;
    private static final String QNAME_STATIC_FIELD_NAME = "QNAME";
    private static final String OPERATION_ACTION_OUTPUT_SUFFIX = "Output";
    private static final String MODULE_INFO_CLASS_NAME = "$YangModuleInfoImpl";
    private static final String PACKAGE_PREFIX = "org.opendaylight.mdsal.gen.javav2";
    private static final String ROOT_PACKAGE_PATTERN_STRING =
            "(org.opendaylight.mdsal.gen.javav2.[a-z0-9_\\.]*\\.rev[0-9][0-9][0-1][0-9][0-3][0-9])";

    private static final Pattern ROOT_PACKAGE_PATTERN = Pattern.compile(ROOT_PACKAGE_PATTERN_STRING);

    private static final LoadingCache<Class<?>, Optional<QName>> CLASS_TO_QNAME = CacheBuilder.newBuilder().weakKeys()
            .expireAfterAccess(EXPIRATION_TIME, TimeUnit.SECONDS).build(new ClassToQNameLoader());

    private BindingReflections() {
        throw new UnsupportedOperationException("Utility class.");
    }

    /**
     * Find augmentation target class from concrete Augmentation class
     *
     * This method uses first generic argument of implemented
     * {@link Augmentation} interface.
     *
     * @param augmentation
     *            {@link Augmentation} subclass for which we want to determine
     *            augmentation target.
     * @return Augmentation target - class which augmentation provides
     *         additional extensions.
     */
    public static Class<? extends Augmentable<?>> findAugmentationTarget(
            final Class<? extends Augmentation<?>> augmentation) {
        return ClassLoaderUtils.findFirstGenericArgument(augmentation, Augmentation.class);
    }

    /**
     * Find data hierarchy parent from concrete Tree Node class
     *
     * This method uses first generic argument of implemented
     * {@link TreeChildNode} interface.
     *
     * @param childClass
     *            - child class for which we want to find the parent class
     * @return Parent class, e.g. class of which the childClass is ChildOf
     */
    static Class<?> findHierarchicalParent(final Class<? extends TreeChildNode<?, ?>> childClass) {
        return ClassLoaderUtils.findFirstGenericArgument(childClass, TreeChildNode.class);
    }

    /**
     * Returns a QName associated to supplied type
     *
     * @param dataType
     *            - type of data
     * @return QName associated to supplied dataType. If dataType is
     *         Augmentation method does not return canonical QName, but QName
     *         with correct namespace revision, but virtual local name, since
     *         augmentations do not have name.
     *
     *         May return null if QName is not present.
     */
    public static QName findQName(final Class<?> dataType) {
        return CLASS_TO_QNAME.getUnchecked(dataType).orNull();
    }

    /**
     * Checks if method is RPC or Action invocation
     *
     * @param possibleMethod
     *            - method to check
     * @return true if method is RPC or Action invocation, false otherwise.
     */
    public static boolean isOperationMethod(final Method possibleMethod) {
        return possibleMethod != null && Operation.class.isAssignableFrom(possibleMethod.getDeclaringClass());
    }

    /**
     * Extracts Output class for RPC method
     *
     * @param targetMethod
     *            method to scan
     * @return Optional.absent() if result type could not be get, or return type
     *         is Void.
     */
    @SuppressWarnings("rawtypes")
    public static Optional<Class<?>> resolveOperationOutputClass(final Method targetMethod) {
        checkState(isOperationMethod(targetMethod), "Supplied method is not a RPC or Action invocation method");
        final Type futureType = targetMethod.getGenericReturnType();
        final Type operationResultType = ClassLoaderUtils.getFirstGenericParameter(futureType);
        final Type operationResultArgument = ClassLoaderUtils.getFirstGenericParameter(operationResultType);
        if (operationResultArgument instanceof Class && !Void.class.equals(operationResultArgument)) {
            return Optional.of((Class) operationResultArgument);
        }
        return Optional.absent();
    }

    /**
     * Extracts input class for RPC or Action
     *
     * @param targetMethod
     *            - method to scan
     * @return Optional.absent() if RPC or Action has no input, RPC input type
     *         otherwise.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Optional<Class<? extends Instantiable<?>>> resolveOperationInputClass(final Method targetMethod) {
        for (final Class clazz : targetMethod.getParameterTypes()) {
            if (Instantiable.class.isAssignableFrom(clazz)) {
                return Optional.of(clazz);
            }
        }
        return Optional.absent();
    }

    /**
     * Find qname of base identity context.
     *
     * @param context
     *            - base identity type context
     * @return QName of base identity context
     */
    public static QName getQName(final Class<? extends BaseIdentity> context) {
        return findQName(context);
    }

    /**
     * Checks if class is child of augmentation.
     *
     * @param clazz
     *            - class to check
     * @return true if is augmentation, false otherwise
     */
    public static boolean isAugmentationChild(final Class<?> clazz) {
        // FIXME: Current resolver could be still confused when child node was
        // added by grouping
        checkArgument(clazz != null);

        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Class<?> parent = findHierarchicalParent((Class) clazz);
        if (parent == null) {
            LOG.debug("Did not find a parent for class {}", clazz);
            return false;
        }

        final String clazzModelPackage = getModelRootPackageName(clazz.getPackage());
        final String parentModelPackage = getModelRootPackageName(parent.getPackage());

        return !clazzModelPackage.equals(parentModelPackage);
    }

    /**
     * Returns root package name for supplied package.
     *
     * @param pkg
     *            Package for which find model root package.
     * @return Package of model root.
     */
    public static String getModelRootPackageName(final Package pkg) {
        return getModelRootPackageName(pkg.getName());
    }

    /**
     * Returns root package name for supplied package name.
     *
     * @param name
     *            - package for which find model root package
     * @return Package of model root
     */
    public static String getModelRootPackageName(final String name) {
        checkArgument(name != null, "Package name should not be null.");
        checkArgument(name.startsWith(PACKAGE_PREFIX), "Package name not starting with %s, is: %s", PACKAGE_PREFIX,
                name);
        final Matcher match = ROOT_PACKAGE_PATTERN.matcher(name);
        checkArgument(match.find(), "Package name '%s' does not match required pattern '%s'", name,
                ROOT_PACKAGE_PATTERN_STRING);
        return match.group(0);
    }

    /**
     * Get QName module of specific Binding object class.
     *
     * @param clz
     *            - class of binding object
     * @return QName module of binding object
     */
    public static final QNameModule getQNameModule(final Class<?> clz) {
        if (Instantiable.class.isAssignableFrom(clz) || BaseIdentity.class.isAssignableFrom(clz)) {
            return findQName(clz).getModule();
        }
        try {
            final YangModuleInfo modInfo = BindingReflections.getModuleInfo(clz);
            return getQNameModule(modInfo);
        } catch (final Exception e) {
            throw new IllegalStateException("Unable to get QName of defining model.", e);
        }
    }

    /**
     * Returns module QName.
     *
     * @param modInfo
     *            - module info
     * @return {@link QNameModule} from module info
     */
    public static final QNameModule getQNameModule(final YangModuleInfo modInfo) {
        return QNameModule.create(URI.create(modInfo.getNamespace()), QName.parseRevision(modInfo.getRevision()));
    }

    /**
     * Returns instance of {@link YangModuleInfo} of declaring model for
     * specific class.
     *
     * @param cls
     *            - class for getting info
     * @return Instance of {@link YangModuleInfo} associated with model, from
     *         which this class was derived.
     * @throws Exception
     */
    public static YangModuleInfo getModuleInfo(final Class<?> cls) throws Exception {
        checkArgument(cls != null);
        final String packageName = getModelRootPackageName(cls.getPackage());
        final String potentialClassName = getModuleInfoClassName(packageName);
        return ClassLoaderUtils.withClassLoader(cls.getClassLoader(), (Callable<YangModuleInfo>) () -> {
            final Class<?> moduleInfoClass = Thread.currentThread().getContextClassLoader().loadClass(potentialClassName);
            return (YangModuleInfo) moduleInfoClass.getMethod("getInstance").invoke(null);
         });
    }

    /**
     * Returns name of module info class.
     *
     * @param packageName
     *            - package name
     * @return name of module info class
     */
    public static String getModuleInfoClassName(final String packageName) {
        return packageName + "." + MODULE_INFO_CLASS_NAME;
    }

    /**
     * Check if supplied class is derived from YANG model.
     *
     * @param cls
     *            - class to check
     * @return true if class is derived from YANG model.
     */
    public static boolean isBindingClass(final Class<?> cls) {
        if (Instantiable.class.isAssignableFrom(cls) || Augmentation.class.isAssignableFrom(cls)
                || TreeNode.class.isAssignableFrom(cls)) {
            return true;
        }
        return cls.getName().startsWith(PACKAGE_PREFIX);
    }

    /**
     * Checks if supplied method is callback for notifications.
     *
     * @param method
     *            - method for check
     * @return true if method is notification callback
     */
    public static boolean isNotificationCallback(final Method method) {
        checkArgument(method != null);
        if (method.getName().startsWith("on") && method.getParameterTypes().length == 1) {
            final Class<?> potentialNotification = method.getParameterTypes()[0];
            if (isNotification(potentialNotification)
                    && method.getName().equals("on" + potentialNotification.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if supplied class is Notification.
     *
     * @param potentialNotification
     *            - class to check
     * @return true if class is notification, false otherwise
     */
    public static boolean isNotification(final Class<?> potentialNotification) {
        checkArgument(potentialNotification != null, "potentialNotification must not be null.");
        return Notification.class.isAssignableFrom(potentialNotification);
    }

    /**
     * Loads {@link YangModuleInfo} info available on current classloader.
     *
     * This method is shorthand for {@link #loadModuleInfos(ClassLoader)} with
     * {@link Thread#getContextClassLoader()} for current thread.
     *
     * @return Set of {@link YangModuleInfo} available for current classloader.
     */
    public static ImmutableSet<YangModuleInfo> loadModuleInfos() {
        return loadModuleInfos(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Loads {@link YangModuleInfo} info available on supplied classloader.
     *
     * {@link YangModuleInfo} are discovered using {@link ServiceLoader} for
     * {@link YangModelBindingProvider}. {@link YangModelBindingProvider} are
     * simple classes which holds only pointers to actual instance
     * {@link YangModuleInfo}.
     *
     * When {@link YangModuleInfo} is available, all dependencies are
     * recursively collected into returning set by collecting results of
     * {@link YangModuleInfo#getImportedModules()}.
     *
     * @param loader
     *            - classloader for which {@link YangModuleInfo} should be
     *            retrieved
     * @return Set of {@link YangModuleInfo} available for supplied classloader.
     */
    public static ImmutableSet<YangModuleInfo> loadModuleInfos(final ClassLoader loader) {
        final Builder<YangModuleInfo> moduleInfoSet = ImmutableSet.builder();
        final ServiceLoader<YangModelBindingProvider> serviceLoader = ServiceLoader.load(YangModelBindingProvider.class,
                loader);
        for (final YangModelBindingProvider bindingProvider : serviceLoader) {
            final YangModuleInfo moduleInfo = bindingProvider.getModuleInfo();
            checkState(moduleInfo != null, "Module Info for %s is not available.", bindingProvider.getClass());
            collectYangModuleInfo(bindingProvider.getModuleInfo(), moduleInfoSet);
        }
        return moduleInfoSet.build();
    }

    private static void collectYangModuleInfo(final YangModuleInfo moduleInfo,
            final Builder<YangModuleInfo> moduleInfoSet) {
        moduleInfoSet.add(moduleInfo);
        for (final YangModuleInfo dependency : moduleInfo.getImportedModules()) {
            collectYangModuleInfo(dependency, moduleInfoSet);
        }
    }

    /**
     * Checks if supplied class represents RPC or Action input/output.
     *
     * @param targetType
     *            - class to be checked
     * @return true if class represents RPC or Action input/output class
     */
    public static boolean isOperationType(final Class<? extends TreeNode> targetType) {
        return Instantiable.class.isAssignableFrom(targetType) && !TreeChildNode.class.isAssignableFrom(targetType)
                && !Notification.class.isAssignableFrom(targetType)
                && (targetType.getName().endsWith("Input") || targetType.getName().endsWith("Output"));
    }

    /**
     * Scans supplied class and returns an iterable of all data children
     * classes.
     *
     * @param type
     *            - YANG Modeled Entity derived from DataContainer
     * @return Iterable of all data children, which have YANG modeled entity
     */
    @SuppressWarnings("unchecked")
    public static Iterable<Class<? extends TreeNode>> getChildrenClasses(final Class<? extends Instantiable<?>> type) {
        checkArgument(type != null, "Target type must not be null");
        checkArgument(Instantiable.class.isAssignableFrom(type), "Supplied type must be derived from Instantiable");
        final List<Class<? extends TreeNode>> ret = new LinkedList<>();
        for (final Method method : type.getMethods()) {
            final Optional<Class<? extends Instantiable<?>>> entity = getYangModeledReturnType(method);
            if (entity.isPresent()) {
                ret.add((Class<? extends TreeNode>) entity.get());
            }
        }
        return ret;
    }

    /**
     * Scans supplied class and returns an iterable of all data children
     * classes.
     *
     * @param type
     *            - YANG Modeled Entity derived from DataContainer
     * @return Iterable of all data children, which have YANG modeled entity
     */
    public static Map<Class<?>, Method> getChildrenClassToMethod(final Class<?> type) {
        checkArgument(type != null, "Target type must not be null");
        checkArgument(Instantiable.class.isAssignableFrom(type), "Supplied type must be derived from Instantiable");
        final Map<Class<?>, Method> ret = new HashMap<>();
        for (final Method method : type.getMethods()) {
            final Optional<Class<? extends Instantiable<?>>> entity = getYangModeledReturnType(method);
            if (entity.isPresent()) {
                ret.put(entity.get(), method);
            }
        }
        return ret;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Optional<Class<? extends Instantiable<?>>> getYangModeledReturnType(final Method method) {
        if ("getClass".equals(method.getName()) || !method.getName().startsWith("get")
                || method.getParameterTypes().length > 0) {
            return Optional.absent();
        }

        final Class returnType = method.getReturnType();
        if (Instantiable.class.isAssignableFrom(returnType)) {
            return Optional.of(returnType);
        } else if (List.class.isAssignableFrom(returnType)) {
            try {
                return ClassLoaderUtils.withClassLoader(method.getDeclaringClass().getClassLoader(),
                        (Callable<Optional<Class<? extends Instantiable<?>>>>) () -> {
                            final Type listResult = ClassLoaderUtils.getFirstGenericParameter(method.getGenericReturnType());
                            if (listResult instanceof Class
                                    && Instantiable.class.isAssignableFrom((Class) listResult)) {
                                return Optional.of((Class) listResult);
                            }
                            return Optional.absent();
                        });
            } catch (final Exception e) {
                /*
                 *
                 * It is safe to log this this exception on debug, since this
                 * method should not fail. Only failures are possible if the
                 * runtime / backing.
                 */
                LOG.debug("Unable to find YANG modeled return type for {}", method, e);
            }
        }
        return Optional.absent();
    }

    private static class ClassToQNameLoader extends CacheLoader<Class<?>, Optional<QName>> {

        @Override
        public Optional<QName> load(@Nonnull final Class<?> key) throws Exception {
            return resolveQNameNoCache(key);
        }

        /**
         * Tries to resolve QName for supplied class.
         *
         * Looks up for static field with name from constant {@link #QNAME_STATIC_FIELD_NAME} and returns
         * value if present.
         *
         * If field is not present uses {@link #computeQName(Class)} to compute QName for missing types.
         *
         * @param key
         *            - class for resolving QName
         * @return resolved QName
         */
        private static Optional<QName> resolveQNameNoCache(final Class<?> key) {
            try {
                final Field field = key.getField(QNAME_STATIC_FIELD_NAME);
                final Object obj = field.get(null);
                if (obj instanceof QName) {
                    return Optional.of((QName) obj);
                }

            } catch (final NoSuchFieldException e) {
                return Optional.of(computeQName(key));
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                /*
                 * It is safe to log this this exception on debug, since this method
                 * should not fail. Only failures are possible if the runtime /
                 * backing.
                 */
                LOG.debug("Unexpected exception during extracting QName for {}", key, e);
            }
            return Optional.absent();
        }

        /**
         * Computes QName for supplied class
         *
         * Namespace and revision are same as {@link YangModuleInfo} associated
         * with supplied class.
         * <p>
         * If class is
         * <ul>
         * <li>rpc/action input: local name is "input".
         * <li>rpc/action output: local name is "output".
         * <li>augmentation: local name is "module name".
         * </ul>
         *
         * There is also fallback, if it is not possible to compute QName using
         * following algorithm returns module QName.
         *
         * FIXME: Extend this algorithm to also provide QName for YANG modeled
         * simple types.
         *
         * @throws IllegalStateException
         *             - if YangModuleInfo could not be resolved
         * @throws IllegalArgumentException
         *             - if supplied class was not derived from YANG model
         *
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        private static QName computeQName(final Class key) {
            if (isBindingClass(key)) {
                YangModuleInfo moduleInfo;
                try {
                    moduleInfo = getModuleInfo(key);
                } catch (final Exception e) {
                    throw new IllegalStateException("Unable to get QName for " + key
                            + ". YangModuleInfo was not found.", e);
                }
                final QName module = getModuleQName(moduleInfo).intern();
                if (Augmentation.class.isAssignableFrom(key)) {
                    return module;
                } else if (isOperationType(key)) {
                    final String className = key.getSimpleName();
                    if (className.endsWith(OPERATION_ACTION_OUTPUT_SUFFIX)) {
                        return QName.create(module, "output").intern();
                    } else {
                        return QName.create(module, "input").intern();
                    }
                }
                /*
                 * Fallback for Binding types which do not have QNAME field
                 */
                return module;
            } else {
                throw new IllegalArgumentException("Supplied class " + key + "is not derived from YANG.");
            }
        }

    }

    /**
     * Given a {@link YangModuleInfo}, create a QName representing it. The QName
     * is formed by reusing the module's namespace and revision using the
     * module's name as the QName's local name.
     *
     * @param moduleInfo
     *            module information
     * @return QName representing the module
     */
    public static QName getModuleQName(final YangModuleInfo moduleInfo) {
        checkArgument(moduleInfo != null, "moduleInfo must not be null.");
        return QName.create(moduleInfo.getNamespace(), moduleInfo.getRevision(), moduleInfo.getName());
    }

    /**
     * Extracts augmentation from Binding DTO field using reflection
     *
     * @param input
     *            Instance of DataObject which is augmentable and may contain
     *            augmentation
     * @return Map of augmentations if read was successful, otherwise empty map.
     */
    public static Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Augmentable<?> input) {
        return AugmentationFieldGetter.getGetter(input.getClass()).getAugmentations(input);
    }

    /**
     * Determines if two augmentation classes or case classes represents same
     * data.
     * <p>
     * Two augmentations or cases could be substituted only if and if:
     * <ul>
     * <li>Both implements same interfaces</li>
     * <li>Both have same children</li>
     * <li>If augmentations: Both have same augmentation target class. Target
     * class was generated for data node in grouping.</li>
     * <li>If cases: Both are from same choice. Choice class was generated for
     * data node in grouping.</li>
     * </ul>
     * <p>
     * <b>Explanation:</b> Binding Specification reuses classes generated for
     * groupings as part of normal data tree, this classes from grouping could
     * be used at various locations and user may not be aware of it and may use
     * incorrect case or augmentation in particular subtree (via copy
     * constructors, etc).
     *
     * @param potential
     *            - class which is potential substitution
     * @param target
     *            - class which should be used at particular subtree
     * @return true if and only if classes represents same data.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean isSubstitutionFor(final Class potential, final Class target) {
        final HashSet<Class> subImplemented = Sets.newHashSet(potential.getInterfaces());
        final HashSet<Class> targetImplemented = Sets.newHashSet(target.getInterfaces());
        if (!subImplemented.equals(targetImplemented)) {
            return false;
        }
        if (Augmentation.class.isAssignableFrom(potential)
                && !BindingReflections.findAugmentationTarget(potential).equals(
                        BindingReflections.findAugmentationTarget(target))) {
            return false;
        }
        for (final Method potentialMethod : potential.getMethods()) {
            try {
                final Method targetMethod = target.getMethod(potentialMethod.getName(), potentialMethod.getParameterTypes());
                if (!potentialMethod.getReturnType().equals(targetMethod.getReturnType())) {
                    return false;
                }
            } catch (final NoSuchMethodException e) {
                // Counterpart method is missing, so classes could not be
                // substituted.
                return false;
            } catch (final SecurityException e) {
                throw new IllegalStateException("Could not compare methods", e);
            }
        }
        return true;
    }
}
