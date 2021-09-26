/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.config.impl;

import com.google.common.annotations.Beta;
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
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.api.config.ConfigurationService;
import org.opendaylight.mdsal.binding.config.api.ConfigurationSnapshot;
import org.osgi.service.component.annotations.Reference;

/**
 * An annotation processor to create glue between Service Component Runtime and {@link ConfigurationService}. It
 * processes {@link Reference} annotations attached to {@link ConfigurationSnapshot} instances.
 */
@Beta
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes({
    "org.opendaylight.mdsal.binding.config.api.InitialConfiguration",
    "org.osgi.service.component.annotations.Reference"
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public final class ReferenceProcessor extends AbstractProcessor {
    private static final String CONFIGURATION_SNAPSHOT = ConfigurationSnapshot.class.getName();
    private static final Set<Modifier> CONSTANT_MODIFIERS = Set.of(Modifier.STATIC, Modifier.FINAL);

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final List<VariableElement> fields = new ArrayList<>();
        final List<TypeElement> references = new ArrayList<>();

        for (var annotation : annotations) {
            final Name fqn = annotation.getQualifiedName();
            if (fqn.contentEquals("org.opendaylight.mdsal.binding.config.api.InitialConfiguration")) {
                processInitialConfiguration(fields, roundEnv.getElementsAnnotatedWith(annotation));
            } else if (fqn.contentEquals("org.osgi.service.component.annotations.Reference")) {
                processReference(references, roundEnv.getElementsAnnotatedWith(annotation));
            } else {
                throw new IllegalStateException("Unexpected annotation " + annotation);
            }
        }

        if (references.isEmpty()) {
            // We have not found any references: bail out
            return false;
        }

        // Okay, we now need to extract which data objects we have



        return false;
    }

    private void processInitialConfiguration(final List<VariableElement> fields, final Set<? extends Element> elements) {
        for (var element : elements) {
            if (element instanceof VariableElement && element.getModifiers().containsAll(CONSTANT_MODIFIERS)) {
                fields.add((VariableElement) element);
            } else {
                log(Kind.ERROR, "@InitialConfguration must be applied to a constant field", element);
            }
        }
    }

    private void processReference(final List<TypeElement> references, final Set<? extends Element> elements) {
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
                    log(Kind.NOTE, "Skipping @Reference on unknown element, has its definition changed?", element);
            }
        }
    }

    private void processReference(final List<TypeElement> references, final Element element) {
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

    private void addConfigurationReference(final List<TypeElement> references, final DeclaredType cfgSnapType,
            final Element element) {
        // We need to understand what type is the declaration referencing
        final var typeArgs = cfgSnapType.getTypeArguments();
        if (typeArgs.size() == 1) {
            final var firstElem = (TypeElement) ((DeclaredType) typeArgs.get(0)).asElement();
            switch (firstElem.getKind()) {
                case INTERFACE:
                    references.add(firstElem);
                    break;
                default:
                    log(Kind.WARNING, "Ignoring ConfigurationSnapshot reference to non-interface", firstElem);
            }


        } else {
            log(Kind.WARNING, "Ignoring wrongly-parameterized ConfigurationSnapshot reference", element);
        }
    }

    private void log(final Kind kind, final String message, final Element element) {
        processingEnv.getMessager().printMessage(kind, message, element);
    }
}
