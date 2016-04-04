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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
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
            sc.append(writeDataSchemaNode(((DataSchemaNode)schemaNode)), "");
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof EnumTypeDefinition.EnumPair) {
            sc.append(writeEnumPair(((EnumTypeDefinition.EnumPair)schemaNode)), "");
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof ExtensionDefinition) {
            sc.append(writeExtension(((ExtensionDefinition)schemaNode)), "");
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof FeatureDefinition) {
            sc.append(writeFeature(((FeatureDefinition)schemaNode)), "");
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof GroupingDefinition) {
            sc.append(writeGroupingDef(((GroupingDefinition)schemaNode)), "");
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof IdentitySchemaNode) {
            sc.append(writeIdentity(((IdentitySchemaNode)schemaNode)), "");
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof NotificationDefinition) {
            sc.append(writeNotification(((NotificationDefinition)schemaNode)), "");
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof RpcDefinition) {
            CharSequence _writeRPC = writeRPC(((RpcDefinition)schemaNode));
            sc.append(_writeRPC, "");
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof TypeDefinition<?>) {
            sc.append(writeTypeDefinition(((TypeDefinition<?>)schemaNode)), "");
            sc.newLineIfNotEmpty();
        }
        if (schemaNode instanceof UnknownSchemaNode) {
            sc.append(writeUnknownSchemaNode(((UnknownSchemaNode)schemaNode)), "");
            sc.newLineIfNotEmpty();
        }
        return sc.toString();
    }

    static String generateYangSnipet(final Set<? extends SchemaNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return "";
        }

        final StringConcatenation sc = new StringConcatenation();
        for (final SchemaNode node : nodes) {
            if (node instanceof NotificationDefinition) {
                sc.append(writeNotification(((NotificationDefinition)node)), "");
                sc.newLineIfNotEmpty();
            } else if (node instanceof RpcDefinition) {
                sc.append(writeRPC(((RpcDefinition) node)), "");
                sc.newLineIfNotEmpty();
            }
        }
        return sc.toString();
    }

    // FIXME: below lies unaudited xtend-generated code

    private static CharSequence writeEnumPair(final EnumTypeDefinition.EnumPair pair) {
        CharSequence _xblockexpression = null;
        {
            Integer _value = pair.getValue();
            boolean hasEnumPairValue = (!Objects.equal(_value, null));
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("enum ");
            String _name = pair.getName();
            _builder.append(_name, "");
            {
                if ((!hasEnumPairValue)) {
                    _builder.append(";");
                } else {
                    _builder.append("{");
                    _builder.newLineIfNotEmpty();
                    _builder.append("    ");
                    _builder.append("value ");
                    Integer _value_1 = pair.getValue();
                    _builder.append(_value_1, "    ");
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

    private static String writeModuleImports(final Set<ModuleImport> moduleImports) {
        String _xblockexpression = null;
        {
            boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(moduleImports);
            if (_isNullOrEmpty) {
                return "";
            }
            StringConcatenation _builder = new StringConcatenation();
            {
                boolean _hasElements = false;
                for(final ModuleImport moduleImport : moduleImports) {
                    if (!_hasElements) {
                        _hasElements = true;
                    } else {
                        _builder.appendImmediate("\n", "");
                    }
                    {
                        boolean _and = false;
                        boolean _notEquals = (!Objects.equal(moduleImport, null));
                        if (!_notEquals) {
                            _and = false;
                        } else {
                            String _moduleName = moduleImport.getModuleName();
                            boolean _isNullOrEmpty_1 = Strings.isNullOrEmpty(_moduleName);
                            boolean _not = (!_isNullOrEmpty_1);
                            _and = _not;
                        }
                        if (_and) {
                            _builder.append("import ");
                            String _moduleName_1 = moduleImport.getModuleName();
                            _builder.append(_moduleName_1, "");
                            _builder.append(" { prefix \"");
                            String _prefix = moduleImport.getPrefix();
                            _builder.append(_prefix, "");
                            _builder.append("\"; }");
                            _builder.newLineIfNotEmpty();
                        }
                    }
                }
            }
            _xblockexpression = _builder.toString();
        }
        return _xblockexpression;
    }

    private static CharSequence writeRevision(final Date moduleRevision, final String moduleDescription) {
        CharSequence _xblockexpression = null;
        {
            final int revisionIndent = 12;
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("revision ");
            SimpleDateFormat _revisionFormat = SimpleDateFormatUtil.getRevisionFormat();
            String _format = _revisionFormat.format(moduleRevision);
            _builder.append(_format, "");
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            _builder.append("    ");
            _builder.append("description \"");
            String _formatToParagraph = YangTextTemplate.formatToParagraph(moduleDescription, revisionIndent);
            _builder.append(_formatToParagraph, "    ");
            _builder.append("\";");
            _builder.newLineIfNotEmpty();
            _builder.append("}");
            _builder.newLine();
            _xblockexpression = _builder;
        }
        return _xblockexpression;
    }

    public static String generateYangSnipet(final Module module) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("module ");
        String _name = module.getName();
        _builder.append(_name, "");
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append("yang-version ");
        String _yangVersion = module.getYangVersion();
        _builder.append(_yangVersion, "    ");
        _builder.append(";");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append("namespace \"");
        QNameModule _qNameModule = module.getQNameModule();
        URI _namespace = _qNameModule.getNamespace();
        String _string = _namespace.toString();
        _builder.append(_string, "    ");
        _builder.append("\";");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append("prefix \"");
        String _prefix = module.getPrefix();
        _builder.append(_prefix, "    ");
        _builder.append("\";");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        {
            Set<ModuleImport> _imports = module.getImports();
            boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(_imports);
            boolean _not = (!_isNullOrEmpty);
            if (_not) {
                _builder.append("    ");
                Set<ModuleImport> _imports_1 = module.getImports();
                String _writeModuleImports = writeModuleImports(_imports_1);
                _builder.append(_writeModuleImports, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Date _revision = module.getRevision();
            boolean _notEquals = (!Objects.equal(_revision, null));
            if (_notEquals) {
                _builder.append("    ");
                Date _revision_1 = module.getRevision();
                String _description = module.getDescription();
                CharSequence _writeRevision = writeRevision(_revision_1, _description);
                _builder.append(_writeRevision, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Collection<DataSchemaNode> _childNodes = module.getChildNodes();
            boolean _isNullOrEmpty_1 = IterableExtensions.isNullOrEmpty(_childNodes);
            boolean _not_1 = (!_isNullOrEmpty_1);
            if (_not_1) {
                _builder.newLine();
                _builder.append("    ");
                Collection<DataSchemaNode> _childNodes_1 = module.getChildNodes();
                CharSequence _writeDataSchemaNodes = writeDataSchemaNodes(_childNodes_1);
                _builder.append(_writeDataSchemaNodes, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Set<GroupingDefinition> _groupings = module.getGroupings();
            boolean _isNullOrEmpty_2 = IterableExtensions.isNullOrEmpty(_groupings);
            boolean _not_2 = (!_isNullOrEmpty_2);
            if (_not_2) {
                _builder.newLine();
                _builder.append("    ");
                Set<GroupingDefinition> _groupings_1 = module.getGroupings();
                CharSequence _writeGroupingDefs = writeGroupingDefs(_groupings_1);
                _builder.append(_writeGroupingDefs, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Set<AugmentationSchema> _augmentations = module.getAugmentations();
            boolean _isNullOrEmpty_3 = IterableExtensions.isNullOrEmpty(_augmentations);
            boolean _not_3 = (!_isNullOrEmpty_3);
            if (_not_3) {
                _builder.newLine();
                _builder.append("    ");
                Set<AugmentationSchema> _augmentations_1 = module.getAugmentations();
                CharSequence _writeAugments = writeAugments(_augmentations_1);
                _builder.append(_writeAugments, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Set<Deviation> _deviations = module.getDeviations();
            boolean _isNullOrEmpty_4 = IterableExtensions.isNullOrEmpty(_deviations);
            boolean _not_4 = (!_isNullOrEmpty_4);
            if (_not_4) {
                _builder.newLine();
                _builder.append("    ");
                Set<Deviation> _deviations_1 = module.getDeviations();
                CharSequence _writeDeviations = writeDeviations(_deviations_1);
                _builder.append(_writeDeviations, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            List<ExtensionDefinition> _extensionSchemaNodes = module.getExtensionSchemaNodes();
            boolean _isNullOrEmpty_5 = IterableExtensions.isNullOrEmpty(_extensionSchemaNodes);
            boolean _not_5 = (!_isNullOrEmpty_5);
            if (_not_5) {
                _builder.newLine();
                _builder.append("    ");
                List<ExtensionDefinition> _extensionSchemaNodes_1 = module.getExtensionSchemaNodes();
                CharSequence _writeExtensions = writeExtensions(_extensionSchemaNodes_1);
                _builder.append(_writeExtensions, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Set<FeatureDefinition> _features = module.getFeatures();
            boolean _isNullOrEmpty_6 = IterableExtensions.isNullOrEmpty(_features);
            boolean _not_6 = (!_isNullOrEmpty_6);
            if (_not_6) {
                _builder.newLine();
                _builder.append("    ");
                Set<FeatureDefinition> _features_1 = module.getFeatures();
                CharSequence _writeFeatures = writeFeatures(_features_1);
                _builder.append(_writeFeatures, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Set<IdentitySchemaNode> _identities = module.getIdentities();
            boolean _isNullOrEmpty_7 = IterableExtensions.isNullOrEmpty(_identities);
            boolean _not_7 = (!_isNullOrEmpty_7);
            if (_not_7) {
                _builder.newLine();
                _builder.append("    ");
                Set<IdentitySchemaNode> _identities_1 = module.getIdentities();
                CharSequence _writeIdentities = writeIdentities(_identities_1);
                _builder.append(_writeIdentities, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Set<NotificationDefinition> _notifications = module.getNotifications();
            boolean _isNullOrEmpty_8 = IterableExtensions.isNullOrEmpty(_notifications);
            boolean _not_8 = (!_isNullOrEmpty_8);
            if (_not_8) {
                _builder.newLine();
                _builder.append("    ");
                Set<NotificationDefinition> _notifications_1 = module.getNotifications();
                CharSequence _writeNotifications = writeNotifications(_notifications_1);
                _builder.append(_writeNotifications, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Set<RpcDefinition> _rpcs = module.getRpcs();
            boolean _isNullOrEmpty_9 = IterableExtensions.isNullOrEmpty(_rpcs);
            boolean _not_9 = (!_isNullOrEmpty_9);
            if (_not_9) {
                _builder.newLine();
                _builder.append("    ");
                Set<RpcDefinition> _rpcs_1 = module.getRpcs();
                CharSequence _writeRPCs = writeRPCs(_rpcs_1);
                _builder.append(_writeRPCs, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            List<UnknownSchemaNode> _unknownSchemaNodes = module.getUnknownSchemaNodes();
            boolean _isNullOrEmpty_10 = IterableExtensions.isNullOrEmpty(_unknownSchemaNodes);
            boolean _not_10 = (!_isNullOrEmpty_10);
            if (_not_10) {
                _builder.newLine();
                _builder.append("    ");
                List<UnknownSchemaNode> _unknownSchemaNodes_1 = module.getUnknownSchemaNodes();
                CharSequence _writeUnknownSchemaNodes = writeUnknownSchemaNodes(_unknownSchemaNodes_1);
                _builder.append(_writeUnknownSchemaNodes, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            Set<UsesNode> _uses = module.getUses();
            boolean _isNullOrEmpty_11 = IterableExtensions.isNullOrEmpty(_uses);
            boolean _not_11 = (!_isNullOrEmpty_11);
            if (_not_11) {
                _builder.newLine();
                _builder.append("    ");
                Set<UsesNode> _uses_1 = module.getUses();
                CharSequence _writeUsesNodes = writeUsesNodes(_uses_1);
                _builder.append(_writeUsesNodes, "    ");
                _builder.newLineIfNotEmpty();
            }
        }
        _builder.append("}");
        _builder.newLine();
        return _builder.toString();
    }

    private static CharSequence writeRPCs(final Set<RpcDefinition> rpcDefs) {
        StringConcatenation _builder = new StringConcatenation();
        {
            for(final RpcDefinition rpc : rpcDefs) {
                {
                    boolean _notEquals = (!Objects.equal(rpc, null));
                    if (_notEquals) {
                        CharSequence _writeRPC = writeRPC(rpc);
                        _builder.append(_writeRPC, "");
                        _builder.newLineIfNotEmpty();
                    }
                }
            }
        }
        return _builder;
    }

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
            _builder.append(_localName, "");
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
                boolean _isNullOrEmpty_1 = IterableExtensions.isNullOrEmpty(_groupings);
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
                boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(_childNodes);
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
                boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(_childNodes);
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
                        _builder.append(_writeNotification, "");
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
            _builder.append(_localName, "");
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
                boolean _isNullOrEmpty_1 = IterableExtensions.isNullOrEmpty(_childNodes);
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
                boolean _isNullOrEmpty_2 = IterableExtensions.isNullOrEmpty(_availableAugmentations);
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
                boolean _isNullOrEmpty_3 = IterableExtensions.isNullOrEmpty(_groupings);
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
                boolean _isNullOrEmpty_4 = IterableExtensions.isNullOrEmpty(_uses);
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
            boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(unknownSchemaNodes);
            if (_isNullOrEmpty) {
                return "";
            }
            StringConcatenation _builder = new StringConcatenation();
            {
                for(final UnknownSchemaNode unknownSchemaNode : unknownSchemaNodes) {
                    String _writeUnknownSchemaNode = writeUnknownSchemaNode(unknownSchemaNode);
                    _builder.append(_writeUnknownSchemaNode, "");
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
                            _builder.append(_writeUsesNode, "");
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
            QName _head = IterableExtensions.<QName>head(_pathFromRoot);
            String _localName = _head.getLocalName();
            _builder.append(_localName, "");
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
                _builder.append(_writeRefine, "");
                _builder.newLineIfNotEmpty();
            }
        }
        return _builder;
    }

    private static CharSequence writeRefine(final SchemaPath path, final SchemaNode schemaNode) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("refine ");
        Iterable<QName> _pathFromRoot = path.getPathFromRoot();
        QName _last = IterableExtensions.<QName>last(_pathFromRoot);
        _builder.append(_last, "");
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
            _builder.append(_localName, "");
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
            boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(identities);
            if (_isNullOrEmpty) {
                return "";
            }
            StringConcatenation _builder = new StringConcatenation();
            {
                for(final IdentitySchemaNode identity : identities) {
                    CharSequence _writeIdentity = writeIdentity(identity);
                    _builder.append(_writeIdentity, "");
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
            _builder.append(_localName, "");
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
                        _builder.append(_writeFeature, "");
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
        _builder.append(_localName, "");
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
                        _builder.append(_writeExtension, "");
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
            _builder.append(_localName, "");
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
                        _builder.append(_writeDeviation, "");
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
        _builder.append(_targetPath, "");
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
                        _builder.append(_writeAugment, "");
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
                _builder.append(_writeDataSchemaNode, "");
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
                        _builder.append(_writeGroupingDef, "");
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
        _builder.append(_formatToAugmentPath, "");
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
            boolean _isNullOrEmpty_3 = IterableExtensions.isNullOrEmpty(_childNodes);
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
            boolean _isNullOrEmpty_4 = IterableExtensions.isNullOrEmpty(_uses);
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
            _builder.append(_localName, "");
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            {
                Set<GroupingDefinition> _groupings = groupingDef.getGroupings();
                boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(_groupings);
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
                boolean _isNullOrEmpty_1 = IterableExtensions.isNullOrEmpty(_childNodes);
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
                boolean _isNullOrEmpty_2 = IterableExtensions.isNullOrEmpty(_unknownSchemaNodes);
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
            _builder.append(_localName, "");
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            {
                Collection<DataSchemaNode> _childNodes = contSchemaNode.getChildNodes();
                boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(_childNodes);
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
                boolean _isNullOrEmpty_1 = IterableExtensions.isNullOrEmpty(_availableAugmentations);
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
                boolean _isNullOrEmpty_2 = IterableExtensions.isNullOrEmpty(_groupings);
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
                boolean _isNullOrEmpty_3 = IterableExtensions.isNullOrEmpty(_uses);
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
                boolean _isNullOrEmpty_4 = IterableExtensions.isNullOrEmpty(_unknownSchemaNodes);
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
            _builder.append(_localName, "");
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
            _builder.append(_localName, "");
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
            _builder.append(_localName, "");
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
            _builder.append(_localName, "");
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
            _builder.append(_localName, "");
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
            _builder.append(_localName, "");
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
                boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(_childNodes);
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
                boolean _isNullOrEmpty_1 = IterableExtensions.isNullOrEmpty(_availableAugmentations);
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
                boolean _isNullOrEmpty_2 = IterableExtensions.isNullOrEmpty(_groupings);
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
                boolean _isNullOrEmpty_3 = IterableExtensions.isNullOrEmpty(_uses);
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
                boolean _isNullOrEmpty_4 = IterableExtensions.isNullOrEmpty(_unknownSchemaNodes);
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
                _builder.append(_writeContSchemaNode, "");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            if ((child instanceof AnyXmlSchemaNode)) {
                CharSequence _writeAnyXmlSchemaNode = writeAnyXmlSchemaNode(((AnyXmlSchemaNode)child));
                _builder.append(_writeAnyXmlSchemaNode, "");
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
                _builder.append(_writeLeafListSchemaNode, "");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            if ((child instanceof ChoiceCaseNode)) {
                CharSequence _writeChoiceCaseNode = writeChoiceCaseNode(((ChoiceCaseNode)child));
                _builder.append(_writeChoiceCaseNode, "");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            if ((child instanceof ChoiceSchemaNode)) {
                CharSequence _writeChoiceNode = writeChoiceNode(((ChoiceSchemaNode)child));
                _builder.append(_writeChoiceNode, "");
                _builder.newLineIfNotEmpty();
            }
        }
        {
            if ((child instanceof ListSchemaNode)) {
                CharSequence _writeListSchemaNode = writeListSchemaNode(((ListSchemaNode)child));
                _builder.append(_writeListSchemaNode, "");
                _builder.newLineIfNotEmpty();
            }
        }
        return _builder;
    }
}
