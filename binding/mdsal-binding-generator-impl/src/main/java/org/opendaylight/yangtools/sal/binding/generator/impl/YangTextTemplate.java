/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

final class YangTextTemplate {
    private static final CharMatcher NEWLINE_OR_TAB = CharMatcher.anyOf("\n\t");

    // FIXME: this is not thread-safe and seems to be unused!
    private static Module module = null;

    private YangTextTemplate() {
        throw new UnsupportedOperationException();
    }

    static String formatSchemaPath(final String moduleName, final Iterable<QName> schemaPath) {
        final StringBuilder sb = new StringBuilder();
        sb.append(moduleName);

        QName currentElement = Iterables.getFirst(schemaPath, null);
        for (QName pathElement : schemaPath) {
            sb.append('/');
            if (!currentElement.getNamespace().equals(pathElement.getNamespace())) {
                currentElement = pathElement;
                sb.append(pathElement);
            } else {
                sb.append(pathElement.getLocalName());
            }
        }
        return sb.toString();
    }

    static String formatToParagraph(final String text, final int nextLineIndent) {
        if (Strings.isNullOrEmpty(text)) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        final StringBuilder lineBuilder = new StringBuilder();
        boolean isFirstElementOnNewLineEmptyChar = false;
        final String lineIndent = Strings.repeat(" ", nextLineIndent);

        String formattedText = NEWLINE_OR_TAB.removeFrom(text);
        formattedText = formattedText.replaceAll(" +", " ");

        final StringTokenizer tokenizer = new StringTokenizer(formattedText, " ", true);

        while (tokenizer.hasMoreElements()) {
            final String nextElement = tokenizer.nextElement().toString();

            if (lineBuilder.length() + nextElement.length() > 80) {
                // Trim trailing whitespace
                for (int i = lineBuilder.length() - 1; i >= 0 && lineBuilder.charAt(i) != ' '; --i) {
                    lineBuilder.setLength(i);
                }

                // Trim leading whitespace
                while (lineBuilder.charAt(0) == ' ') {
                    lineBuilder.deleteCharAt(0);
                }

                sb.append(lineBuilder).append('\n');
                lineBuilder.setLength(0);

                if (nextLineIndent > 0) {
                    sb.append(lineIndent);
                }

                if (" ".equals(nextElement.toString())) {
                    isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
                }
            }
            if (isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
            } else {
                lineBuilder.append(nextElement);
            }
        }

        return sb.append(lineBuilder).append('\n').toString();
    }

    private static boolean isNullOrEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    private static String formatToAugmentPath(final Iterable<QName> schemaPath) {
        final StringBuilder sb = new StringBuilder();
        for (QName pathElement : schemaPath) {
            sb.append("\\(").append(pathElement.getNamespace()).append(')').append(pathElement.getLocalName());
        }
        return sb.toString();
    }

    static String generateYangSnipet(final SchemaNode schemaNode) {
        if (schemaNode == null) {
            return "";
        }

        final StringConcatenation sc = new StringConcatenation();
        if (schemaNode instanceof DataSchemaNode) {
            sc.append(writeDataSchemaNode(((DataSchemaNode)schemaNode)));
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof EnumTypeDefinition.EnumPair) {
            sc.append(writeEnumPair(((EnumTypeDefinition.EnumPair)schemaNode)));
            sc.newLine();
        }
        if (schemaNode instanceof ExtensionDefinition) {
            sc.append(writeExtension(((ExtensionDefinition)schemaNode)));
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof FeatureDefinition) {
            sc.append(writeFeature(((FeatureDefinition)schemaNode)));
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof GroupingDefinition) {
            sc.append(writeGroupingDef(((GroupingDefinition)schemaNode)));
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof IdentitySchemaNode) {
            sc.append(writeIdentity(((IdentitySchemaNode)schemaNode)));
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof NotificationDefinition) {
            sc.append(writeNotification(((NotificationDefinition)schemaNode)));
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof RpcDefinition) {
            CharSequence _writeRPC = writeRPC(((RpcDefinition)schemaNode));
            sc.append(_writeRPC, "");
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof TypeDefinition<?>) {
            sc.append(writeTypeDefinition(((TypeDefinition<?>)schemaNode)));
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof UnknownSchemaNode) {
            sc.append(writeUnknownSchemaNode(((UnknownSchemaNode)schemaNode)));
            sc.newLineIfNotEmpty();
        }
        return sc.toString();
    }

    static String generateYangSnipet(final Set<? extends SchemaNode> nodes) {
        if (isNullOrEmpty(nodes)) {
            return "";
        }

        final StringConcatenation sc = new StringConcatenation();
        for (final SchemaNode node : nodes) {
            if (node instanceof NotificationDefinition) {
                sc.append(writeNotification(((NotificationDefinition)node)));
                sc.newLineIfNotEmpty();
            } else if (node instanceof RpcDefinition) {
                sc.append(writeRPC(((RpcDefinition) node)));
                sc.newLineIfNotEmpty();
            }
        }
        return sc.toString();
    }

    private static CharSequence writeEnumPair(final EnumTypeDefinition.EnumPair pair) {
        final StringBuilder sb = new StringBuilder();
        sb.append("enum ").append(pair.getName());

        final Integer value = pair.getValue();
        if (value != null) {
            sb.append("{\n");
            sb.append("    value ").append(value).append(";\n");
            sb.append("}\n");
        } else {
            sb.append(';');
        }
        return sb;
    }

    private static String writeModuleImports(final Set<ModuleImport> moduleImports) {
        final StringBuilder sb = new StringBuilder();
        for (final ModuleImport moduleImport : moduleImports) {
            if (sb.length() != 0) {
                sb.append('\n');
            }
            if (moduleImport != null) {
                final String name = moduleImport.getModuleName();
                if (!Strings.isNullOrEmpty(name)) {
                    sb.append("import ").append(name).append(" { prefix \"").append(moduleImport.getPrefix());
                    sb.append("\"; }\n");
                }
            }
        }

        return sb.toString();
    }

    private static CharSequence writeRevision(final Date moduleRevision, final String moduleDescription) {
        final StringConcatenation sc = new StringConcatenation();
        sc.append("revision ");
        sc.append(SimpleDateFormatUtil.getRevisionFormat().format(moduleRevision));
        sc.append(" {\n");
        sc.append("    description \"");
        sc.append(YangTextTemplate.formatToParagraph(moduleDescription, 12), "    ");
        sc.append("\";\n");
        sc.append("}\n");
        return sc;
    }

    static String generateYangSnipet(final Module module) {
        final StringConcatenation sc = new StringConcatenation();
        sc.append("module ");
        sc.append(module.getName());
        sc.append(" {\n");
        sc.append("    yang-version ");
        sc.append(module.getYangVersion());
        sc.append(";\n");
        sc.append("    namespace \"");
        sc.append(module.getQNameModule().getNamespace().toString());
        sc.append("\";\n");
        sc.append("    prefix \"");
        sc.append(module.getPrefix());
        sc.append("\";\n\n");

        final Set<ModuleImport> imports = module.getImports();
        if (!isNullOrEmpty(imports)) {
            sc.append("    ");
            sc.append(writeModuleImports(imports), "    ");
        }

        final Date revision = module.getRevision();
        if (revision != null) {
            sc.append("    ");
            sc.append(writeRevision(revision, module.getDescription()), "    ");
            sc.newLine();
        }

        final Collection<DataSchemaNode> childNodes = module.getChildNodes();
        if (!isNullOrEmpty(childNodes)) {
            sc.newLine();
            sc.append("    ");
            sc.append(writeDataSchemaNodes(childNodes), "    ");
            sc.newLineIfNotEmpty();
        }

        final Set<GroupingDefinition> groupings = module.getGroupings();
        if (!isNullOrEmpty(groupings)) {
            sc.newLine();
            sc.append("    ");
            sc.append(writeGroupingDefs(groupings), "    ");
            sc.newLineIfNotEmpty();
        }

        final Set<AugmentationSchema> augmentations = module.getAugmentations();
        if (!isNullOrEmpty(augmentations)) {
            sc.newLine();
            sc.append("    ");
            sc.append(writeAugments(augmentations), "    ");
            sc.newLineIfNotEmpty();
        }

        final Set<Deviation> deviations = module.getDeviations();
        if (!isNullOrEmpty(deviations)) {
            sc.newLine();
            sc.append("    ");
            sc.append(writeDeviations(deviations), "    ");
            sc.newLineIfNotEmpty();
        }

        final List<ExtensionDefinition> extensionSchemaNodes = module.getExtensionSchemaNodes();
        if (!isNullOrEmpty(extensionSchemaNodes)) {
            sc.newLine();
            sc.append("    ");
            sc.append(writeExtensions(extensionSchemaNodes), "    ");
            sc.newLineIfNotEmpty();
        }

        final Set<FeatureDefinition> features = module.getFeatures();
        if (!isNullOrEmpty(features)) {
            sc.newLine();
            sc.append("    ");
            sc.append(writeFeatures(features), "    ");
            sc.newLineIfNotEmpty();
        }

        final Set<IdentitySchemaNode> identities = module.getIdentities();
        if (!isNullOrEmpty(identities)) {
            sc.newLine();
            sc.append("    ");
            sc.append(writeIdentities(identities), "    ");
            sc.newLineIfNotEmpty();
        }

        final Set<NotificationDefinition> notifications = module.getNotifications();
        if (!isNullOrEmpty(notifications)) {
            sc.newLine();
            sc.append("    ");
            sc.append(writeNotifications(notifications), "    ");
            sc.newLineIfNotEmpty();
        }

        final Set<RpcDefinition> rpcs = module.getRpcs();
        if (!isNullOrEmpty(rpcs)) {
            sc.newLine();
            sc.append("    ");
            sc.append(writeRPCs(rpcs), "    ");
            sc.newLineIfNotEmpty();
        }

        final List<UnknownSchemaNode> unknownSchemaNodes = module.getUnknownSchemaNodes();
        if (!isNullOrEmpty(unknownSchemaNodes)) {
            sc.newLine();
            sc.append("    ");
            sc.append(writeUnknownSchemaNodes(unknownSchemaNodes), "    ");
            sc.newLineIfNotEmpty();
        }

        final Set<UsesNode> uses = module.getUses();
        if (!isNullOrEmpty(uses)) {
            sc.newLine();
            sc.append("    ");
            sc.append(writeUsesNodes(uses), "    ");
            sc.newLineIfNotEmpty();
        }

        sc.append("}\n");
        return sc.toString();
    }

    private static CharSequence writeRPCs(final Set<RpcDefinition> rpcDefs) {
        final StringConcatenation sc = new StringConcatenation();
        for (final RpcDefinition rpc : rpcDefs) {
            if (rpc != null) {
                sc.append(writeRPC(rpc));
                sc.newLineIfNotEmpty();
            }
        }
        return sc;
    }

    // FIXME: below lies unaudited xtend-generated code

    private static CharSequence writeRPC(final RpcDefinition rpc) {
        CharSequence _xblockexpression = null;
        {
            Status _status = rpc.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("rpc ");
            QName _qName = rpc.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            {
                String _description = rpc.getDescription();
                boolean _isNullOrEmpty = Strings.isNullOrEmpty(_description);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                    _builder.append("    ");
                    _builder.append("\"");
                    String _description_1 = rpc.getDescription();
                    _builder.append(_description_1, "    ");
                    _builder.append("\";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Set<GroupingDefinition> _groupings = rpc.getGroupings();
                boolean _isNullOrEmpty_1 = isNullOrEmpty(_groupings);
                boolean _not_1 = (!_isNullOrEmpty_1);
                if (_not_1) {
                    _builder.append("    ");
                    Set<GroupingDefinition> _groupings_1 = rpc.getGroupings();
                    CharSequence _writeGroupingDefs = writeGroupingDefs(_groupings_1);
                    _builder.append(_writeGroupingDefs, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                ContainerSchemaNode _input = rpc.getInput();
                boolean _notEquals = (!Objects.equal(_input, null));
                if (_notEquals) {
                    _builder.append("    ");
                    ContainerSchemaNode _input_1 = rpc.getInput();
                    CharSequence _writeRpcInput = writeRpcInput(_input_1);
                    _builder.append(_writeRpcInput, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                ContainerSchemaNode _output = rpc.getOutput();
                boolean _notEquals_1 = (!Objects.equal(_output, null));
                if (_notEquals_1) {
                    _builder.append("    ");
                    ContainerSchemaNode _output_1 = rpc.getOutput();
                    CharSequence _writeRpcOutput = writeRpcOutput(_output_1);
                    _builder.append(_writeRpcOutput, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                String _reference = rpc.getReference();
                boolean _isNullOrEmpty_2 = Strings.isNullOrEmpty(_reference);
                boolean _not_2 = (!_isNullOrEmpty_2);
                if (_not_2) {
                    _builder.append("    ");
                    _builder.append("reference");
                    _builder.newLine();
                    _builder.append("    ");
                    _builder.append("    ");
                    _builder.append("\"");
                    String _reference_1 = rpc.getReference();
                    _builder.append(_reference_1, "        ");
                    _builder.append("\";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                if (isStatusDeprecated) {
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = rpc.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeRpcInput(final ContainerSchemaNode input) {
        CharSequence _xblockexpression = null;
        {
            boolean _equals = Objects.equal(input, null);
            if (_equals) {
                return "";
            }
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("input {");
            _builder.newLine();
            {
                Collection<DataSchemaNode> _childNodes = input.getChildNodes();
                boolean _isNullOrEmpty = isNullOrEmpty(_childNodes);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                    _builder.append("    ");
                    Collection<DataSchemaNode> _childNodes_1 = input.getChildNodes();
                    CharSequence _writeDataSchemaNodes = writeDataSchemaNodes(_childNodes_1);
                    _builder.append(_writeDataSchemaNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeRpcOutput(final ContainerSchemaNode output) {
        CharSequence _xblockexpression = null;
        {
            boolean _equals = Objects.equal(output, null);
            if (_equals) {
                return "";
            }
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("output {");
            _builder.newLine();
            {
                Collection<DataSchemaNode> _childNodes = output.getChildNodes();
                boolean _isNullOrEmpty = isNullOrEmpty(_childNodes);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                    _builder.append("    ");
                    Collection<DataSchemaNode> _childNodes_1 = output.getChildNodes();
                    CharSequence _writeDataSchemaNodes = writeDataSchemaNodes(_childNodes_1);
                    _builder.append(_writeDataSchemaNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeNotifications(final Set<NotificationDefinition> notifications) {
        StringConcatenation _builder = new StringConcatenation();
        {
            for(final NotificationDefinition notification : notifications) {
                {
                    boolean _notEquals = (!Objects.equal(notification, null));
                    if (_notEquals) {
                        CharSequence _writeNotification = writeNotification(notification);
                        _builder.append(_writeNotification);
                        _builder.newLineIfNotEmpty();
                    }
                }
            }
        }
        return _builder;
    }

    private static CharSequence writeNotification(final NotificationDefinition notification) {
        CharSequence _xblockexpression = null;
        {
            Status _status = notification.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("notification ");
            QName _qName = notification.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            {
                String _description = notification.getDescription();
                boolean _isNullOrEmpty = Strings.isNullOrEmpty(_description);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                    _builder.append("    ");
                    _builder.append("description");
                    _builder.newLine();
                    _builder.append("    ");
                    _builder.append("    ");
                    _builder.append("\"");
                    String _description_1 = notification.getDescription();
                    _builder.append(_description_1, "        ");
                    _builder.append("\";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Collection<DataSchemaNode> _childNodes = notification.getChildNodes();
                boolean _isNullOrEmpty_1 = isNullOrEmpty(_childNodes);
                boolean _not_1 = (!_isNullOrEmpty_1);
                if (_not_1) {
                    _builder.append("    ");
                    Collection<DataSchemaNode> _childNodes_1 = notification.getChildNodes();
                    CharSequence _writeDataSchemaNodes = writeDataSchemaNodes(_childNodes_1);
                    _builder.append(_writeDataSchemaNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Set<AugmentationSchema> _availableAugmentations = notification.getAvailableAugmentations();
                boolean _isNullOrEmpty_2 = isNullOrEmpty(_availableAugmentations);
                boolean _not_2 = (!_isNullOrEmpty_2);
                if (_not_2) {
                    _builder.append("    ");
                    Set<AugmentationSchema> _availableAugmentations_1 = notification.getAvailableAugmentations();
                    CharSequence _writeAugments = writeAugments(_availableAugmentations_1);
                    _builder.append(_writeAugments, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Set<GroupingDefinition> _groupings = notification.getGroupings();
                boolean _isNullOrEmpty_3 = isNullOrEmpty(_groupings);
                boolean _not_3 = (!_isNullOrEmpty_3);
                if (_not_3) {
                    _builder.append("    ");
                    Set<GroupingDefinition> _groupings_1 = notification.getGroupings();
                    CharSequence _writeGroupingDefs = writeGroupingDefs(_groupings_1);
                    _builder.append(_writeGroupingDefs, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Set<UsesNode> _uses = notification.getUses();
                boolean _isNullOrEmpty_4 = isNullOrEmpty(_uses);
                boolean _not_4 = (!_isNullOrEmpty_4);
                if (_not_4) {
                    _builder.append("    ");
                    Set<UsesNode> _uses_1 = notification.getUses();
                    CharSequence _writeUsesNodes = writeUsesNodes(_uses_1);
                    _builder.append(_writeUsesNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                String _reference = notification.getReference();
                boolean _isNullOrEmpty_5 = Strings.isNullOrEmpty(_reference);
                boolean _not_5 = (!_isNullOrEmpty_5);
                if (_not_5) {
                    _builder.append("    ");
                    _builder.append("reference");
                    _builder.newLine();
                    _builder.append("    ");
                    _builder.append("    ");
                    _builder.append("\"");
                    String _reference_1 = notification.getReference();
                    _builder.append(_reference_1, "        ");
                    _builder.append("\";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                if (isStatusDeprecated) {
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = notification.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeUnknownSchemaNodes(final List<UnknownSchemaNode> unknownSchemaNodes) {
        CharSequence _xblockexpression = null;
        {
            boolean _isNullOrEmpty = isNullOrEmpty(unknownSchemaNodes);
            if (_isNullOrEmpty) {
                return "";
            }
            StringConcatenation _builder = new StringConcatenation();
            {
                for(final UnknownSchemaNode unknownSchemaNode : unknownSchemaNodes) {
                    String _writeUnknownSchemaNode = writeUnknownSchemaNode(unknownSchemaNode);
                    _builder.append(_writeUnknownSchemaNode);
                    _builder.newLineIfNotEmpty();
                }
            }
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static String writeUnknownSchemaNode(final UnknownSchemaNode unknownSchemaNode) {
        return "";
    }

    private static CharSequence writeUsesNodes(final Set<UsesNode> usesNodes) {
        CharSequence _xblockexpression = null;
        {
            boolean _equals = Objects.equal(usesNodes, null);
            if (_equals) {
                return "";
            }
            StringConcatenation _builder = new StringConcatenation();
            {
                for(final UsesNode usesNode : usesNodes) {
                    {
                        boolean _notEquals = (!Objects.equal(usesNode, null));
                        if (_notEquals) {
                            CharSequence _writeUsesNode = writeUsesNode(usesNode);
                            _builder.append(_writeUsesNode);
                            _builder.newLineIfNotEmpty();
                        }
                    }
                }
            }
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeUsesNode(final UsesNode usesNode) {
        CharSequence _xblockexpression = null;
        {
            Map<SchemaPath, SchemaNode> _refines = usesNode.getRefines();
            boolean _isEmpty = _refines.isEmpty();
            final boolean hasRefines = (!_isEmpty);
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("uses ");
            SchemaPath _groupingPath = usesNode.getGroupingPath();
            Iterable<QName> _pathFromRoot = _groupingPath.getPathFromRoot();
            QName _head = Iterables.getFirst(_pathFromRoot, null);
            String _localName = _head.getLocalName();
            _builder.append(_localName);
            {
                if ((!hasRefines)) {
                    _builder.append(";");
                } else {
                    _builder.append(" {");
                }
            }
            _builder.newLineIfNotEmpty();
            {
                if (hasRefines) {
                    _builder.append("    ");
                    Map<SchemaPath, SchemaNode> _refines_1 = usesNode.getRefines();
                    CharSequence _writeRefines = writeRefines(_refines_1);
                    _builder.append(_writeRefines, "    ");
                    _builder.newLineIfNotEmpty();
                    _builder.append("}");
                    _builder.newLine();
                }
            }
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeRefines(final Map<SchemaPath, SchemaNode> refines) {
        StringConcatenation _builder = new StringConcatenation();
        {
            Set<SchemaPath> _keySet = refines.keySet();
            for(final SchemaPath path : _keySet) {
                final SchemaNode schemaNode = refines.get(path);
                _builder.newLineIfNotEmpty();
                CharSequence _writeRefine = writeRefine(path, schemaNode);
                _builder.append(_writeRefine);
                _builder.newLineIfNotEmpty();
            }
        }
        return _builder;
    }

    private static CharSequence writeRefine(final SchemaPath path, final SchemaNode schemaNode) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("refine ");
        Iterable<QName> _pathFromRoot = path.getPathFromRoot();
        QName _last = Iterables.getLast(_pathFromRoot, null);
        _builder.append(_last);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        {
            if ((schemaNode instanceof DataSchemaNode)) {
                _builder.append("    ");
                CharSequence _writeDataSchemaNode = writeDataSchemaNode(((DataSchemaNode)schemaNode));
                _builder.append(_writeDataSchemaNode, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        _builder.append("}");
        _builder.newLine();
        return _builder;
    }

    private static CharSequence writeTypeDefinition(final TypeDefinition<?> typeDefinition) {
        CharSequence _xblockexpression = null;
        {
            Status _status = typeDefinition.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("type ");
            QName _qName = typeDefinition.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            {
                if ((!isStatusDeprecated)) {
                    _builder.append(";");
                } else {
                    _builder.append(" {");
                    _builder.newLineIfNotEmpty();
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = typeDefinition.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                    _builder.append("}");
                    _builder.newLine();
                }
            }
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeIdentities(final Set<IdentitySchemaNode> identities) {
        CharSequence _xblockexpression = null;
        {
            boolean _isNullOrEmpty = isNullOrEmpty(identities);
            if (_isNullOrEmpty) {
                return "";
            }
            StringConcatenation _builder = new StringConcatenation();
            {
                for(final IdentitySchemaNode identity : identities) {
                    CharSequence _writeIdentity = writeIdentity(identity);
                    _builder.append(_writeIdentity);
                    _builder.newLineIfNotEmpty();
                }
            }
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeIdentity(final IdentitySchemaNode identity) {
        CharSequence _xblockexpression = null;
        {
            boolean _equals = Objects.equal(identity, null);
            if (_equals) {
                return "";
            }
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("identity ");
            QName _qName = identity.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            {
                IdentitySchemaNode _baseIdentity = identity.getBaseIdentity();
                boolean _notEquals = (!Objects.equal(_baseIdentity, null));
                if (_notEquals) {
                    _builder.append("    ");
                    _builder.append("base \"(");
                    IdentitySchemaNode _baseIdentity_1 = identity.getBaseIdentity();
                    String _writeIdentityNs = writeIdentityNs(_baseIdentity_1);
                    _builder.append(_writeIdentityNs, "    ");
                    _builder.append(")");
                    IdentitySchemaNode _baseIdentity_2 = identity.getBaseIdentity();
                    _builder.append(_baseIdentity_2, "    ");
                    _builder.append("\";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                String _description = identity.getDescription();
                boolean _isNullOrEmpty = Strings.isNullOrEmpty(_description);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                    _builder.append("    ");
                    _builder.append("description");
                    _builder.newLine();
                    _builder.append("    ");
                    _builder.append("    ");
                    _builder.append("\"");
                    String _description_1 = identity.getDescription();
                    _builder.append(_description_1, "        ");
                    _builder.append("\";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                String _reference = identity.getReference();
                boolean _isNullOrEmpty_1 = Strings.isNullOrEmpty(_reference);
                boolean _not_1 = (!_isNullOrEmpty_1);
                if (_not_1) {
                    _builder.append("    ");
                    _builder.append("reference");
                    _builder.newLine();
                    _builder.append("    ");
                    _builder.append("    ");
                    _builder.append("\"");
                    String _reference_1 = identity.getReference();
                    _builder.append(_reference_1, "        ");
                    _builder.append("\";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Status _status = identity.getStatus();
                boolean _notEquals_1 = (!Objects.equal(_status, null));
                if (_notEquals_1) {
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = identity.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static String writeIdentityNs(final IdentitySchemaNode identity) {
        boolean _equals = Objects.equal(module, null);
        if (_equals) {
            return "";
        }
        QName _qName = identity.getQName();
        final URI identityNs = _qName.getNamespace();
        URI _namespace = module.getNamespace();
        boolean _equals_1 = _namespace.equals(identityNs);
        boolean _not = (!_equals_1);
        if (_not) {
            return (identityNs + ":");
        }
        return "";
    }

    private static CharSequence writeFeatures(final Set<FeatureDefinition> features) {
        StringConcatenation _builder = new StringConcatenation();
        {
            for(final FeatureDefinition feature : features) {
                {
                    boolean _notEquals = (!Objects.equal(feature, null));
                    if (_notEquals) {
                        CharSequence _writeFeature = writeFeature(feature);
                        _builder.append(_writeFeature);
                        _builder.newLineIfNotEmpty();
                    }
                }
            }
        }
        return _builder;
    }

    private static CharSequence writeFeature(final FeatureDefinition featureDef) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("feature ");
        QName _qName = featureDef.getQName();
        String _localName = _qName.getLocalName();
        _builder.append(_localName);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        {
            String _description = featureDef.getDescription();
            boolean _isNullOrEmpty = Strings.isNullOrEmpty(_description);
            boolean _not = (!_isNullOrEmpty);
            if (_not) {
                _builder.append("    ");
                _builder.append("description");
                _builder.newLine();
                _builder.append("    ");
                _builder.append("    ");
                _builder.append("\"");
                String _description_1 = featureDef.getDescription();
                _builder.append(_description_1, "        ");
                _builder.append("\";");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            String _reference = featureDef.getReference();
            boolean _isNullOrEmpty_1 = Strings.isNullOrEmpty(_reference);
            boolean _not_1 = (!_isNullOrEmpty_1);
            if (_not_1) {
                _builder.append("    ");
                _builder.append("reference");
                _builder.newLine();
                _builder.append("    ");
                _builder.append("    ");
                _builder.append("\"");
                String _reference_1 = featureDef.getReference();
                _builder.append(_reference_1, "        ");
                _builder.append("\";");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Status _status = featureDef.getStatus();
            boolean _notEquals = (!Objects.equal(_status, null));
            if (_notEquals) {
                _builder.append("    ");
                _builder.append("status ");
                Status _status_1 = featureDef.getStatus();
                _builder.append(_status_1, "    ");
                _builder.append(";");
                _builder.newLineIfNotEmpty();
            }
        }
        _builder.append("}");
        _builder.newLine();
        return _builder;
    }

    private static CharSequence writeExtensions(final List<ExtensionDefinition> extensions) {
        StringConcatenation _builder = new StringConcatenation();
        {
            for(final ExtensionDefinition anExtension : extensions) {
                {
                    boolean _notEquals = (!Objects.equal(anExtension, null));
                    if (_notEquals) {
                        CharSequence _writeExtension = writeExtension(anExtension);
                        _builder.append(_writeExtension);
                        _builder.newLineIfNotEmpty();
                    }
                }
            }
        }
        return _builder;
    }

    private static CharSequence writeExtension(final ExtensionDefinition extensionDef) {
        CharSequence _xblockexpression = null;
        {
            Status _status = extensionDef.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("extension ");
            QName _qName = extensionDef.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            {
                String _description = extensionDef.getDescription();
                boolean _isNullOrEmpty = Strings.isNullOrEmpty(_description);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                    _builder.append("    ");
                    _builder.append("description");
                    _builder.newLine();
                    _builder.append("    ");
                    _builder.append("    ");
                    _builder.append("\"");
                    String _description_1 = extensionDef.getDescription();
                    _builder.append(_description_1, "        ");
                    _builder.append("\";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                String _argument = extensionDef.getArgument();
                boolean _isNullOrEmpty_1 = Strings.isNullOrEmpty(_argument);
                boolean _not_1 = (!_isNullOrEmpty_1);
                if (_not_1) {
                    _builder.append("    ");
                    _builder.append("argument \"");
                    String _argument_1 = extensionDef.getArgument();
                    _builder.append(_argument_1, "    ");
                    _builder.append("\";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                String _reference = extensionDef.getReference();
                boolean _isNullOrEmpty_2 = Strings.isNullOrEmpty(_reference);
                boolean _not_2 = (!_isNullOrEmpty_2);
                if (_not_2) {
                    _builder.append("    ");
                    _builder.append("reference");
                    _builder.newLine();
                    _builder.append("    ");
                    _builder.append("    ");
                    _builder.append("\"");
                    String _reference_1 = extensionDef.getReference();
                    _builder.append(_reference_1, "        ");
                    _builder.append("\";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Status _status_1 = extensionDef.getStatus();
                boolean _notEquals = (!Objects.equal(_status_1, null));
                if (_notEquals) {
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_2 = extensionDef.getStatus();
                    _builder.append(_status_2, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeDeviations(final Set<Deviation> deviations) {
        StringConcatenation _builder = new StringConcatenation();
        {
            for(final Deviation deviation : deviations) {
                {
                    boolean _notEquals = (!Objects.equal(deviation, null));
                    if (_notEquals) {
                        CharSequence _writeDeviation = writeDeviation(deviation);
                        _builder.append(_writeDeviation);
                        _builder.newLineIfNotEmpty();
                    }
                }
            }
        }
        return _builder;
    }

    private static CharSequence writeDeviation(final Deviation deviation) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("deviation ");
        SchemaPath _targetPath = deviation.getTargetPath();
        _builder.append(_targetPath);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        {
            String _reference = deviation.getReference();
            boolean _isNullOrEmpty = Strings.isNullOrEmpty(_reference);
            boolean _not = (!_isNullOrEmpty);
            if (_not) {
                _builder.append("    ");
                _builder.append("reference");
                _builder.newLine();
                _builder.append("    ");
                _builder.append("    ");
                _builder.append("\"");
                String _reference_1 = deviation.getReference();
                _builder.append(_reference_1, "        ");
                _builder.append("\";");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            boolean _and = false;
            Deviation.Deviate _deviate = deviation.getDeviate();
            boolean _notEquals = (!Objects.equal(_deviate, null));
            if (!_notEquals) {
                _and = false;
            } else {
                Deviation.Deviate _deviate_1 = deviation.getDeviate();
                String _name = _deviate_1.name();
                boolean _isNullOrEmpty_1 = Strings.isNullOrEmpty(_name);
                boolean _not_1 = (!_isNullOrEmpty_1);
                _and = _not_1;
            }
            if (_and) {
                _builder.append("    ");
                _builder.append("deviation ");
                Deviation.Deviate _deviate_2 = deviation.getDeviate();
                String _name_1 = _deviate_2.name();
                _builder.append(_name_1, "    ");
                _builder.append(";");
                _builder.newLineIfNotEmpty();
            }
        }
        _builder.append("}");
        _builder.newLine();
        return _builder;
    }

    private static CharSequence writeAugments(final Set<AugmentationSchema> augments) {
        StringConcatenation _builder = new StringConcatenation();
        {
            for(final AugmentationSchema augment : augments) {
                {
                    boolean _notEquals = (!Objects.equal(augment, null));
                    if (_notEquals) {
                        CharSequence _writeAugment = writeAugment(augment);
                        _builder.append(_writeAugment);
                        _builder.newLineIfNotEmpty();
                    }
                }
            }
        }
        return _builder;
    }

    private static CharSequence writeDataSchemaNodes(final Collection<DataSchemaNode> dataSchemaNodes) {
        StringConcatenation _builder = new StringConcatenation();
        {
            for(final DataSchemaNode schemaNode : dataSchemaNodes) {
                CharSequence _writeDataSchemaNode = writeDataSchemaNode(schemaNode);
                _builder.append(_writeDataSchemaNode);
                _builder.newLineIfNotEmpty();
            }
        }
        return _builder;
    }

    private static CharSequence writeGroupingDefs(final Set<GroupingDefinition> groupingDefs) {
        StringConcatenation _builder = new StringConcatenation();
        {
            for(final GroupingDefinition groupingDef : groupingDefs) {
                {
                    boolean _notEquals = (!Objects.equal(groupingDef, null));
                    if (_notEquals) {
                        CharSequence _writeGroupingDef = writeGroupingDef(groupingDef);
                        _builder.append(_writeGroupingDef);
                        _builder.newLineIfNotEmpty();
                    }
                }
            }
        }
        return _builder;
    }

    private static CharSequence writeAugment(final AugmentationSchema augment) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("augment ");
        SchemaPath _targetPath = augment.getTargetPath();
        Iterable<QName> _pathFromRoot = _targetPath.getPathFromRoot();
        String _formatToAugmentPath = YangTextTemplate.formatToAugmentPath(_pathFromRoot);
        _builder.append(_formatToAugmentPath);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        {
            boolean _and = false;
            RevisionAwareXPath _whenCondition = augment.getWhenCondition();
            boolean _notEquals = (!Objects.equal(_whenCondition, null));
            if (!_notEquals) {
                _and = false;
            } else {
                RevisionAwareXPath _whenCondition_1 = augment.getWhenCondition();
                String _string = _whenCondition_1.toString();
                boolean _isNullOrEmpty = Strings.isNullOrEmpty(_string);
                boolean _not = (!_isNullOrEmpty);
                _and = _not;
            }
            if (_and) {
                _builder.append("    ");
                _builder.append("when \"");
                RevisionAwareXPath _whenCondition_2 = augment.getWhenCondition();
                String _string_1 = _whenCondition_2.toString();
                _builder.append(_string_1, "    ");
                _builder.append("\";");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            String _description = augment.getDescription();
            boolean _isNullOrEmpty_1 = Strings.isNullOrEmpty(_description);
            boolean _not_1 = (!_isNullOrEmpty_1);
            if (_not_1) {
                _builder.append("    ");
                _builder.append("description");
                _builder.newLine();
                _builder.append("    ");
                _builder.append("    ");
                _builder.append("\"");
                String _description_1 = augment.getDescription();
                _builder.append(_description_1, "        ");
                _builder.append("\";");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            String _reference = augment.getReference();
            boolean _isNullOrEmpty_2 = Strings.isNullOrEmpty(_reference);
            boolean _not_2 = (!_isNullOrEmpty_2);
            if (_not_2) {
                _builder.append("    ");
                _builder.append("reference");
                _builder.newLine();
                _builder.append("    ");
                _builder.append("    ");
                _builder.append("\"");
                String _reference_1 = augment.getReference();
                _builder.append(_reference_1, "        ");
                _builder.append("\";");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Status _status = augment.getStatus();
            boolean _notEquals_1 = (!Objects.equal(_status, null));
            if (_notEquals_1) {
                _builder.append("    ");
                _builder.append("status ");
                Status _status_1 = augment.getStatus();
                _builder.append(_status_1, "    ");
                _builder.append(";");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Collection<DataSchemaNode> _childNodes = augment.getChildNodes();
            boolean _isNullOrEmpty_3 = isNullOrEmpty(_childNodes);
            boolean _not_3 = (!_isNullOrEmpty_3);
            if (_not_3) {
                _builder.append("    ");
                Collection<DataSchemaNode> _childNodes_1 = augment.getChildNodes();
                CharSequence _writeDataSchemaNodes = writeDataSchemaNodes(_childNodes_1);
                _builder.append(_writeDataSchemaNodes, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Set<UsesNode> _uses = augment.getUses();
            boolean _isNullOrEmpty_4 = isNullOrEmpty(_uses);
            boolean _not_4 = (!_isNullOrEmpty_4);
            if (_not_4) {
                _builder.append("    ");
                Set<UsesNode> _uses_1 = augment.getUses();
                CharSequence _writeUsesNodes = writeUsesNodes(_uses_1);
                _builder.append(_writeUsesNodes, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        _builder.append("}");
        _builder.newLine();
        return _builder;
    }

    private static CharSequence writeGroupingDef(final GroupingDefinition groupingDef) {
        CharSequence _xblockexpression = null;
        {
            Status _status = groupingDef.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("grouping ");
            QName _qName = groupingDef.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            {
                Set<GroupingDefinition> _groupings = groupingDef.getGroupings();
                boolean _isNullOrEmpty = isNullOrEmpty(_groupings);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                    _builder.append("    ");
                    Set<GroupingDefinition> _groupings_1 = groupingDef.getGroupings();
                    CharSequence _writeGroupingDefs = writeGroupingDefs(_groupings_1);
                    _builder.append(_writeGroupingDefs, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Collection<DataSchemaNode> _childNodes = groupingDef.getChildNodes();
                boolean _isNullOrEmpty_1 = isNullOrEmpty(_childNodes);
                boolean _not_1 = (!_isNullOrEmpty_1);
                if (_not_1) {
                    _builder.append("    ");
                    Collection<DataSchemaNode> _childNodes_1 = groupingDef.getChildNodes();
                    CharSequence _writeDataSchemaNodes = writeDataSchemaNodes(_childNodes_1);
                    _builder.append(_writeDataSchemaNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                if (isStatusDeprecated) {
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = groupingDef.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                List<UnknownSchemaNode> _unknownSchemaNodes = groupingDef.getUnknownSchemaNodes();
                boolean _isNullOrEmpty_2 = isNullOrEmpty(_unknownSchemaNodes);
                boolean _not_2 = (!_isNullOrEmpty_2);
                if (_not_2) {
                    _builder.append("    ");
                    List<UnknownSchemaNode> _unknownSchemaNodes_1 = groupingDef.getUnknownSchemaNodes();
                    CharSequence _writeUnknownSchemaNodes = writeUnknownSchemaNodes(_unknownSchemaNodes_1);
                    _builder.append(_writeUnknownSchemaNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeContSchemaNode(final ContainerSchemaNode contSchemaNode) {
        CharSequence _xblockexpression = null;
        {
            Status _status = contSchemaNode.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("container ");
            QName _qName = contSchemaNode.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            {
                Collection<DataSchemaNode> _childNodes = contSchemaNode.getChildNodes();
                boolean _isNullOrEmpty = isNullOrEmpty(_childNodes);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                    _builder.append("    ");
                    Collection<DataSchemaNode> _childNodes_1 = contSchemaNode.getChildNodes();
                    CharSequence _writeDataSchemaNodes = writeDataSchemaNodes(_childNodes_1);
                    _builder.append(_writeDataSchemaNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Set<AugmentationSchema> _availableAugmentations = contSchemaNode.getAvailableAugmentations();
                boolean _isNullOrEmpty_1 = isNullOrEmpty(_availableAugmentations);
                boolean _not_1 = (!_isNullOrEmpty_1);
                if (_not_1) {
                    _builder.append("    ");
                    Set<AugmentationSchema> _availableAugmentations_1 = contSchemaNode.getAvailableAugmentations();
                    CharSequence _writeAugments = writeAugments(_availableAugmentations_1);
                    _builder.append(_writeAugments, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Set<GroupingDefinition> _groupings = contSchemaNode.getGroupings();
                boolean _isNullOrEmpty_2 = isNullOrEmpty(_groupings);
                boolean _not_2 = (!_isNullOrEmpty_2);
                if (_not_2) {
                    _builder.append("    ");
                    Set<GroupingDefinition> _groupings_1 = contSchemaNode.getGroupings();
                    CharSequence _writeGroupingDefs = writeGroupingDefs(_groupings_1);
                    _builder.append(_writeGroupingDefs, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Set<UsesNode> _uses = contSchemaNode.getUses();
                boolean _isNullOrEmpty_3 = isNullOrEmpty(_uses);
                boolean _not_3 = (!_isNullOrEmpty_3);
                if (_not_3) {
                    _builder.append("    ");
                    Set<UsesNode> _uses_1 = contSchemaNode.getUses();
                    CharSequence _writeUsesNodes = writeUsesNodes(_uses_1);
                    _builder.append(_writeUsesNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                if (isStatusDeprecated) {
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = contSchemaNode.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                List<UnknownSchemaNode> _unknownSchemaNodes = contSchemaNode.getUnknownSchemaNodes();
                boolean _isNullOrEmpty_4 = isNullOrEmpty(_unknownSchemaNodes);
                boolean _not_4 = (!_isNullOrEmpty_4);
                if (_not_4) {
                    _builder.append("    ");
                    List<UnknownSchemaNode> _unknownSchemaNodes_1 = contSchemaNode.getUnknownSchemaNodes();
                    CharSequence _writeUnknownSchemaNodes = writeUnknownSchemaNodes(_unknownSchemaNodes_1);
                    _builder.append(_writeUnknownSchemaNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeAnyXmlSchemaNode(final AnyXmlSchemaNode anyXmlSchemaNode) {
        CharSequence _xblockexpression = null;
        {
            Status _status = anyXmlSchemaNode.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("anyxml ");
            QName _qName = anyXmlSchemaNode.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            {
                if ((!isStatusDeprecated)) {
                    _builder.append(";");
                } else {
                    _builder.append(" {");
                    _builder.newLineIfNotEmpty();
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = anyXmlSchemaNode.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                    _builder.append("}");
                    _builder.newLine();
                }
            }
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeLeafSchemaNode(final LeafSchemaNode leafSchemaNode) {
        CharSequence _xblockexpression = null;
        {
            Status _status = leafSchemaNode.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("leaf ");
            QName _qName = leafSchemaNode.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            _builder.append("    ");
            _builder.append("type ");
            TypeDefinition<?> _type = leafSchemaNode.getType();
            QName _qName_1 = _type.getQName();
            String _localName_1 = _qName_1.getLocalName();
            _builder.append(_localName_1, "    ");
            _builder.append(";");
            _builder.newLineIfNotEmpty();
            {
                if (isStatusDeprecated) {
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = leafSchemaNode.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeLeafListSchemaNode(final LeafListSchemaNode leafListSchemaNode) {
        CharSequence _xblockexpression = null;
        {
            Status _status = leafListSchemaNode.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("leaf-list ");
            QName _qName = leafListSchemaNode.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            _builder.append("    ");
            _builder.append("type ");
            TypeDefinition<? extends TypeDefinition<?>> _type = leafListSchemaNode.getType();
            QName _qName_1 = _type.getQName();
            String _localName_1 = _qName_1.getLocalName();
            _builder.append(_localName_1, "    ");
            _builder.append(";");
            _builder.newLineIfNotEmpty();
            {
                if (isStatusDeprecated) {
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = leafListSchemaNode.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeChoiceCaseNode(final ChoiceCaseNode choiceCaseNode) {
        CharSequence _xblockexpression = null;
        {
            Status _status = choiceCaseNode.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("case ");
            QName _qName = choiceCaseNode.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            {
                Collection<DataSchemaNode> _childNodes = choiceCaseNode.getChildNodes();
                for(final DataSchemaNode childNode : _childNodes) {
                    _builder.append("    ");
                    CharSequence _writeDataSchemaNode = writeDataSchemaNode(childNode);
                    _builder.append(_writeDataSchemaNode, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                if (isStatusDeprecated) {
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = choiceCaseNode.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeChoiceNode(final ChoiceSchemaNode choiceNode) {
        CharSequence _xblockexpression = null;
        {
            Status _status = choiceNode.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("choice ");
            QName _qName = choiceNode.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            {
                Set<ChoiceCaseNode> _cases = choiceNode.getCases();
                for(final ChoiceCaseNode child : _cases) {
                    _builder.append("    ");
                    CharSequence _writeDataSchemaNode = writeDataSchemaNode(child);
                    _builder.append(_writeDataSchemaNode, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                if (isStatusDeprecated) {
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = choiceNode.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeListSchemaNode(final ListSchemaNode listSchemaNode) {
        CharSequence _xblockexpression = null;
        {
            Status _status = listSchemaNode.getStatus();
            boolean isStatusDeprecated = Objects.equal(_status, Status.DEPRECATED);
            StringConcatenation _builder = new StringConcatenation();
            {
                if (isStatusDeprecated) {
                    _builder.append("@deprecated - status DEPRECATED");
                    _builder.newLine();
                }
            }
            _builder.append("list ");
            QName _qName = listSchemaNode.getQName();
            String _localName = _qName.getLocalName();
            _builder.append(_localName);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            _builder.append("    ");
            _builder.append("key ");
            {
                List<QName> _keyDefinition = listSchemaNode.getKeyDefinition();
                boolean _hasElements = false;
                for(final QName listKey : _keyDefinition) {
                    if (!_hasElements) {
                        _hasElements = true;
                    } else {
                        _builder.appendImmediate(" ", "    ");
                    }
                    _builder.append("\"");
                    String _localName_1 = listKey.getLocalName();
                    _builder.append(_localName_1, "    ");
                    _builder.append("\"");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Collection<DataSchemaNode> _childNodes = listSchemaNode.getChildNodes();
                boolean _isNullOrEmpty = isNullOrEmpty(_childNodes);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                    _builder.append("    ");
                    Collection<DataSchemaNode> _childNodes_1 = listSchemaNode.getChildNodes();
                    CharSequence _writeDataSchemaNodes = writeDataSchemaNodes(_childNodes_1);
                    _builder.append(_writeDataSchemaNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Set<AugmentationSchema> _availableAugmentations = listSchemaNode.getAvailableAugmentations();
                boolean _isNullOrEmpty_1 = isNullOrEmpty(_availableAugmentations);
                boolean _not_1 = (!_isNullOrEmpty_1);
                if (_not_1) {
                    _builder.append("    ");
                    Set<AugmentationSchema> _availableAugmentations_1 = listSchemaNode.getAvailableAugmentations();
                    CharSequence _writeAugments = writeAugments(_availableAugmentations_1);
                    _builder.append(_writeAugments, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Set<GroupingDefinition> _groupings = listSchemaNode.getGroupings();
                boolean _isNullOrEmpty_2 = isNullOrEmpty(_groupings);
                boolean _not_2 = (!_isNullOrEmpty_2);
                if (_not_2) {
                    _builder.append("    ");
                    Set<GroupingDefinition> _groupings_1 = listSchemaNode.getGroupings();
                    CharSequence _writeGroupingDefs = writeGroupingDefs(_groupings_1);
                    _builder.append(_writeGroupingDefs, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                Set<UsesNode> _uses = listSchemaNode.getUses();
                boolean _isNullOrEmpty_3 = isNullOrEmpty(_uses);
                boolean _not_3 = (!_isNullOrEmpty_3);
                if (_not_3) {
                    _builder.append("    ");
                    Set<UsesNode> _uses_1 = listSchemaNode.getUses();
                    CharSequence _writeUsesNodes = writeUsesNodes(_uses_1);
                    _builder.append(_writeUsesNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                if (isStatusDeprecated) {
                    _builder.append("    ");
                    _builder.append("status ");
                    Status _status_1 = listSchemaNode.getStatus();
                    _builder.append(_status_1, "    ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                }
            }
            {
                List<UnknownSchemaNode> _unknownSchemaNodes = listSchemaNode.getUnknownSchemaNodes();
                boolean _isNullOrEmpty_4 = isNullOrEmpty(_unknownSchemaNodes);
                boolean _not_4 = (!_isNullOrEmpty_4);
                if (_not_4) {
                    _builder.append("    ");
                    List<UnknownSchemaNode> _unknownSchemaNodes_1 = listSchemaNode.getUnknownSchemaNodes();
                    CharSequence _writeUnknownSchemaNodes = writeUnknownSchemaNodes(_unknownSchemaNodes_1);
                    _builder.append(_writeUnknownSchemaNodes, "    ");
                    _builder.newLineIfNotEmpty();
                }
            }
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    private static CharSequence writeDataSchemaNode(final DataSchemaNode child) {
        StringConcatenation _builder = new StringConcatenation();
        {
            if ((child instanceof ContainerSchemaNode)) {
                CharSequence _writeContSchemaNode = writeContSchemaNode(((ContainerSchemaNode)child));
                _builder.append(_writeContSchemaNode);
                _builder.newLineIfNotEmpty();
            }
        }
        {
            if ((child instanceof AnyXmlSchemaNode)) {
                CharSequence _writeAnyXmlSchemaNode = writeAnyXmlSchemaNode(((AnyXmlSchemaNode)child));
                _builder.append(_writeAnyXmlSchemaNode);
                _builder.newLineIfNotEmpty();
            }
        }
        {
            if ((child instanceof LeafSchemaNode)) {
                CharSequence _writeLeafSchemaNode = writeLeafSchemaNode(((LeafSchemaNode)child));
                _builder.append(_writeLeafSchemaNode, "");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            if ((child instanceof LeafListSchemaNode)) {
                CharSequence _writeLeafListSchemaNode = writeLeafListSchemaNode(((LeafListSchemaNode)child));
                _builder.append(_writeLeafListSchemaNode);
                _builder.newLineIfNotEmpty();
            }
        }
        {
            if ((child instanceof ChoiceCaseNode)) {
                CharSequence _writeChoiceCaseNode = writeChoiceCaseNode(((ChoiceCaseNode)child));
                _builder.append(_writeChoiceCaseNode);
                _builder.newLineIfNotEmpty();
            }
        }
        {
            if ((child instanceof ChoiceSchemaNode)) {
                CharSequence _writeChoiceNode = writeChoiceNode(((ChoiceSchemaNode)child));
                _builder.append(_writeChoiceNode);
                _builder.newLineIfNotEmpty();
            }
        }
        {
            if ((child instanceof ListSchemaNode)) {
                CharSequence _writeListSchemaNode = writeListSchemaNode(((ListSchemaNode)child));
                _builder.append(_writeListSchemaNode);
                _builder.newLineIfNotEmpty();
            }
        }
        return _builder;
    }
}
