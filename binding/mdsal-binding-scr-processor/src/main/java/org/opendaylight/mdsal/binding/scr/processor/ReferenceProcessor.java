/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic.Kind;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.scr.api.ConfigurationSnapshot;
import org.opendaylight.mdsal.binding.scr.api.InitialConfiguration;
import org.osgi.service.component.annotations.Reference;

/**
 * An annotation processor to create glue between Service Component Runtime and
 * {@code org.opendaylight.mdsal.binding.api.config.ConfigurationService}. It processes
 * {@code org.osgi.service.component.annotations.Reference} annotations attached to {@link ConfigurationSnapshot}
 * instances to discern which parts of the configuration datastore are to be exposed into Service Registry and generates
 * the corresponding code.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes({
    "org.opendaylight.mdsal.binding.scr.api.InitialConfiguration",
    "org.osgi.service.component.annotations.Reference"
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
// FIXME: alright, the codegen part is probably not what we want to do. At the end of the day, we could enforce the
//        constants to be publicly available and:
//        - generate an DescriptorConstants.META_INF_SCR_CONFIGURATION, which will describe which DataObject interfaces
//          are of interest
//        - generate a @Requirement(namespace = ExtenderNamespace.EXTENDER_NAMESPACE) annotation in classes which are
//          receiving objects
//        - have an OSGi extender, which will process the configuration-snapshot.xml file for each component, and will
//          instantiate a ConfigurationService-using proxy, which will provide the appropriate ConfigurationSnapshot
//          service in ServiceRegistry
//
//        This way we leave a minimal footprint and can also implement alternative runtime support, if this pattern ends
//        up being useful.
public final class ReferenceProcessor extends AbstractProcessor {
    private static final String CONFIGURATION_SNAPSHOT = ConfigurationSnapshot.class.getName();
    private static final String INITIAL_CONFIGURATION = InitialConfiguration.class.getName();
    private static final String REFERENCE = Reference.class.getName();
    private static final Set<Modifier> CONSTANT_MODIFIERS = Set.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            // Nothing to search for
            return false;
        }

        notez("Lookz 4 " + annotations);

        final List<VariableElement> fields = new ArrayList<>();
        final List<ReferenceSite> references = new ArrayList<>();

        for (var annotation : annotations) {
            final Name fqn = annotation.getQualifiedName();
            if (fqn.contentEquals(INITIAL_CONFIGURATION)) {
                processInitialConfiguration(fields, roundEnv.getElementsAnnotatedWith(annotation));
            } else if (fqn.contentEquals(REFERENCE)) {
                processReference(references, roundEnv.getElementsAnnotatedWith(annotation));
            } else {
                throw new IllegalStateException("Unexpected annotation " + annotation);
            }
        }

        if (references.isEmpty()) {
            // We have not found any references: bail out
            notez("Iz nuttin :(");
            return false;
        }

        // Okay, we now need to extract which data objects we have
        for (var reference : references) {

        }


        notez("Needz shiniez " + references);
        notez("Haz shiniez " + fields);









        // FIXME: we also need to specialize references to include proper:
        // @Reference(target = "(org.opendaylight.mdsal.binding.ConfigurationType=SomeComponentConfiguration)")
        // or somesuch selector



        return false;
    }

    private void processInitialConfiguration(final List<VariableElement> fields,
            final Set<? extends Element> elements) {
        for (var element : elements) {
            if (element instanceof VariableElement && element.getModifiers().containsAll(CONSTANT_MODIFIERS)) {
                fields.add((VariableElement) element);
            } else {
                error("@InitialConfguration must be applied to a 'public static final' field", element);
            }
        }
    }

    private void processReference(final List<ReferenceSite> references, final Set<? extends Element> elements) {
        for (var element : elements) {
            switch (element.getKind()) {
                case FIELD:
                case PARAMETER:
                    processReference(references, element);
                    break;
                case METHOD:
                    // @Reference used on a method, search its parameters to find
                    for (var parameter : ((ExecutableElement) element).getParameters()) {
                        processReference(references, parameter);
                    }
                    break;
                default:
                    note("Skipping @Reference on unknown element, has its definition changed?", element);
            }
        }
    }

    private void processReference(final List<ReferenceSite> references, final Element element) {
        final var type = (DeclaredType) element.asType();
        final var typeName = ((TypeElement) type.asElement()).getQualifiedName();

        if (typeName.contentEquals(CONFIGURATION_SNAPSHOT)) {
            // A single reference injection
            addConfigurationReference(references, type, element);
        } else if (typeName.contentEquals("java.util.List")) {
            // Multiple reference injection
            final var typeArgs = type.getTypeArguments();
            if (typeArgs.size() == 1) {
                final var firstArg = (DeclaredType) typeArgs.get(0);
                final var firstName = ((TypeElement) firstArg.asElement()).getQualifiedName();
                if (firstName.contentEquals(CONFIGURATION_SNAPSHOT)) {
                    addConfigurationReference(references, firstArg, element);
                }
            }
        }
    }

    private void addConfigurationReference(final List<ReferenceSite> references, final DeclaredType cfgSnapType,
            final @NonNull Element element) {
        // We need to understand what type is the declaration referencing
        final var typeArgs = cfgSnapType.getTypeArguments();
        if (typeArgs.size() == 1) {
            final var firstElem = (TypeElement) ((DeclaredType) typeArgs.get(0)).asElement();
            switch (firstElem.getKind()) {
                case INTERFACE:
                    references.add(new ReferenceSite(element, firstElem));
                    break;
                default:
                    warning("Ignoring ConfigurationSnapshot reference to non-interface", firstElem);
            }
        } else {
            warning("Ignoring wrongly-parameterized ConfigurationSnapshot reference", element);
        }
    }

    private void generateServiceComponent(final VariableElement field) throws IOException {
        final var definingClass = (TypeElement) field.getEnclosingElement();
        final var componetClassName = definingClass.getQualifiedName() + "$$$SCR";

        final var builderFile = processingEnv.getFiler() .createSourceFile(componetClassName);
        try (var out = new PrintWriter(builderFile.openWriter())) {
            // writing generated file to out …
        }
    }

    private void note(final String message, final Element element) {
        log(Kind.NOTE, message, element);
    }

    private void warning(final String message, final Element element) {
        log(Kind.WARNING, message, element);
    }

    private void error(final String message, final Element element) {
        log(Kind.ERROR, message, element);
    }

    private void log(final Kind kind, final String message, final Element element) {
        processingEnv.getMessager().printMessage(Kind.WARNING, message, element);
    }

    // FIXME: debug only remove at some point
    private void notez(final String message) {
        note(message, null);
    }
}
