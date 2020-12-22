/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.unified.doc.generator

import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.ArrayList
import java.util.Collection
import java.util.IdentityHashMap
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.List
import java.util.Map
import java.util.Optional
import java.util.Set
import org.gaul.modernizer_maven_annotations.SuppressModernizer
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode
import org.opendaylight.yangtools.yang.model.api.MandatoryAware
import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition
import org.opendaylight.yangtools.yang.model.api.SchemaNode
import org.opendaylight.yangtools.yang.model.api.SchemaPath
import org.opendaylight.yangtools.yang.model.api.TypeAware
import org.opendaylight.yangtools.yang.model.api.TypeDefinition
import org.opendaylight.yangtools.yang.model.api.UsesNode
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint
import org.opendaylight.yangtools.yang.model.api.type.LengthRestrictedTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonatype.plexus.build.incremental.BuildContext

@SuppressModernizer
class GeneratorImpl {

    static val Logger LOG = LoggerFactory.getLogger(GeneratorImpl)

    val Map<String, String> imports = new HashMap()
    val Map<TypeDefinition<?>, SchemaPath> types = new IdentityHashMap
    var Module currentModule
    var EffectiveModelContext ctx
    var File path

    StringBuilder augmentChildNodesAsString

    DataSchemaNode lastNodeInTargetPath = null

    new(EffectiveModelContext context) {
        this.ctx = context
        fillTypes(SchemaPath.ROOT, context.moduleStatements.values)
    }

    private def void fillTypes(SchemaPath path, Collection<? extends EffectiveStatement<?, ?>> stmts) {
        for (stmt : stmts) {
            val arg = stmt.argument
            if (arg instanceof QName) {
                val stmtPath = path.createChild(arg)
                if (stmt instanceof TypeDefinition) {
                    types.putIfAbsent(stmt, stmtPath)
                } else if (stmt instanceof TypeAware) {
                    val type = stmt.type
                    val typePath = stmtPath.createChild(type.QName)
                    types.putIfAbsent(type, typePath)
                }

                fillTypes(stmtPath, stmt.effectiveSubstatements)
            }
        }
    }

    def generate(BuildContext buildContext, File targetPath, Set<Module> modulesToGen) throws IOException {
        path = targetPath
        Files.createDirectories(path.getParentFile().toPath())
        val it = new HashSet
        for (module : modulesToGen) {
            add(generateDocumentation(buildContext, module))
        }
        return it;
    }

    def generateDocumentation(BuildContext buildContext, Module module) {
        val destination = new File(path, '''«module.name».html''')
        module.imports.forEach[importModule | this.imports.put(importModule.prefix, importModule.moduleName)]
        var OutputStreamWriter fw
        var BufferedWriter bw
        try {
            fw = new OutputStreamWriter(buildContext.newFileOutputStream(destination), StandardCharsets.UTF_8)
            bw = new BufferedWriter(fw)
            currentModule = module
            bw.append(generate(module, ctx))
        } catch (IOException e) {
            LOG.error("Failed to emit file {}", destination, e);
        } finally {
            if (bw !== null) {
                bw.close();
            }
            if (fw !== null) {
                fw.close();
            }
        }
        return destination
    }

    def generate(Module module, EffectiveModelContext ctx) '''
        <!DOCTYPE html>
        <html lang="en">
          <head>
            <title>«module.name»</title>
          </head>
          <body>
            «body(module, ctx)»
          </body>
        </html>
    '''

    def body(Module module, EffectiveModelContext ctx) '''
        «header(module)»

        «typeDefinitionsSummary(module)»
        «identitiesSummary(module)»
        «groupingsSummary(module)»
        «augmentationsSummary(module, ctx)»
        «objectsSummary(module)»
        «notificationsSummary(module)»
        «rpcsSummary(module)»
        «extensionsSummary(module)»
        «featuresSummary(module)»

        «typeDefinitions(module)»

        «identities(module)»

        «groupings(module)»

        «dataStore(module)»

        «childNodes(module)»

        «notifications(module)»

        «augmentations(module, ctx)»

        «rpcs(module)»

        «extensions(module)»

        «features(module)»

    '''


    private def typeDefinitionsSummary(Module module) {
        val Collection<? extends TypeDefinition<?>> typedefs = module.typeDefinitions
        if (typedefs.empty) {
            return '';
        }
        return '''
        <div>
            <h3>Type Definitions Summary</h3>
            <table>
                <tr>
                    <th>Name</th>
                    <th>Description</th>
                </tr>
                «FOR typedef : typedefs»
                <tr>
                    <td>
                    «anchorLink(typedef.QName.localName, strong(typedef.QName.localName))»
                    </td>
                    <td>
                    «typedef.description»
                    </td>
                </tr>
                «ENDFOR»
            </table>
        </div>
        '''
    }

    def typeDefinitions(Module module) {
        val Collection<? extends TypeDefinition<?>> typedefs = module.typeDefinitions
        if (typedefs.empty) {
            return '';
        }
        return '''
            <h2>Type Definitions</h2>
            <ul>
            «FOR typedef : typedefs»
                <li>
                    <h3 id="«typedef.QName.localName»">«typedef.QName.localName»</h3>
                    <ul>
                    «typedef.descAndRefLi»
                    «typedef.restrictions»
                    </ul>
                </li>
            «ENDFOR»
            </ul>
        '''
    }

    private def identities(Module module) {
        if (module.identities.empty) {
            return '';
        }
        return '''
            <h2>Identities</h2>
            <ul>
            «FOR identity : module.identities»
                <li>
                    <h3 id="«identity.QName.localName»">«identity.QName.localName»</h3>
                    <ul>
                    «identity.descAndRefLi»
                    «IF !identity.baseIdentities.isEmpty»
                        «listItem("base", identity.baseIdentities.get(0).QName.localName)»
                    «ENDIF»
                    </ul>
                </li>
            «ENDFOR»
            </ul>
        '''
    }

    private def identitiesSummary(Module module) {
        if (module.identities.empty) {
            return '';
        }
        return '''
        <h3>Identities Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR identity : module.identities»
            <tr>
                <td>
                «anchorLink(identity.QName.localName, strong(identity.QName.localName))»
                </td>
                <td>
                «identity.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    private def groupings(Module module) {
        if (module.groupings.empty) {
            return '';
        }
        return '''
            <h2>Groupings</h2>
            <ul>
            «FOR grouping : module.groupings»
                <li>
                    <h3 id="«grouping.QName.localName»">«grouping.QName.localName»</h3>
                    <ul>
                        «grouping.descAndRefLi»
                        «FOR childNode : grouping.childNodes»
                            «childNode.printSchemaNodeInfo»
                        «ENDFOR»
                    </ul>
                </li>
            «ENDFOR»
            </ul>
        '''
    }

    private def groupingsSummary(Module module) {
        if (module.groupings.empty) {
            return '';
        }
        return '''
        <h3>Groupings Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR grouping : module.groupings»
            <tr>
                <td>
                «anchorLink(grouping.QName.localName, strong(grouping.QName.localName))»
                </td>
                <td>
                «grouping.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def dataStore(Module module) {
        if (module.childNodes.empty) {
            return '';
        }
        return '''
            <h2>Datastore Structure</h2>
            «tree(module)»
        '''
    }

    def augmentations(Module module, EffectiveModelContext context) {
        if (module.augmentations.empty) {
            return '';
        }
        return '''
            <h2>Augmentations</h2>

            <ul>
            «FOR augment : module.augmentations»
                <li>
                    <h3 id="«schemaPathToString(module, augment.targetPath, context, augment)»">
                    Target [«typeAnchorLink(augment.targetPath.asSchemaPath, schemaPathToString(module, augment.targetPath, context, augment))»]</h3>
                    «augment.description»
                        Status: «strong(String.valueOf(augment.status))»
                    «IF augment.reference !== null»
                        Reference «augment.reference»
                    «ENDIF»
                    «IF augment.whenCondition !== null»
                        When «augment.whenCondition.toString»
                    «ENDIF»
                    «FOR childNode : augment.childNodes»
                        «childNode.printSchemaNodeInfo»
                    «ENDFOR»

                    <h3>Example</h3>
                    «createAugmentChildNodesAsString(new ArrayList(augment.childNodes))»
                    «printNodeChildren(parseTargetPath(augment.targetPath))»
                </li>
            «ENDFOR»
            </ul>
        '''
    }

    private def createAugmentChildNodesAsString(List<DataSchemaNode> childNodes) {
        augmentChildNodesAsString = new StringBuilder();
        augmentChildNodesAsString.append(printNodeChildren(childNodes))
        return ''
    }

    private def parseTargetPath(SchemaNodeIdentifier path) {
        val nodes = new ArrayList<DataSchemaNode>();
        for (QName pathElement : path.nodeIdentifiers) {
            val module = ctx.findModule(pathElement.module)
            if (module.isPresent) {
                var foundNode = module.get.dataChildByName(pathElement)
                if (foundNode === null) {
                    val child = nodes.last
                    if (child instanceof DataNodeContainer) {
                        val dataContNode = child as DataNodeContainer
                        foundNode = findNodeInChildNodes(pathElement, dataContNode.childNodes)
                    }
                }
                if (foundNode !== null) {
                    nodes.add(foundNode);
                }
            }
        }
        if (!nodes.empty) {
            lastNodeInTargetPath = nodes.get(nodes.size() - 1)
        }

        val targetPathNodes = new ArrayList<DataSchemaNode>();
        targetPathNodes.add(lastNodeInTargetPath)

        return targetPathNodes
    }

    private def DataSchemaNode findNodeInChildNodes(QName findingNode, Iterable<? extends DataSchemaNode> childNodes) {
        for (child : childNodes) {
            if (child.QName.equals(findingNode))
                return child;
        }
        // find recursively
        for(child : childNodes) {
            if (child instanceof ContainerSchemaNode) {
                val foundChild = findNodeInChildNodes(findingNode, child.childNodes)
                if (foundChild !== null)
                    return foundChild;
            } else if (child instanceof ListSchemaNode) {
                val foundChild = findNodeInChildNodes(findingNode, child.childNodes)
                if (foundChild !== null)
                    return foundChild;
            }
        }
    }

    private def printNodeChildren(List<DataSchemaNode> childNodes) {
        if (childNodes.empty) {
            return ''
        }

        return
        '''
        <pre>
        «printAugmentedNode(childNodes.get(0))»
        </pre>
        '''
    }

    private def CharSequence printAugmentedNode(DataSchemaNode child) {

        if(child instanceof CaseSchemaNode)
            return ''

        return
        '''
        «IF child instanceof ContainerSchemaNode»
            «printContainerNode(child)»
        «ENDIF»
        «IF child instanceof AnyxmlSchemaNode»
            «printAnyXmlNode(child)»
        «ENDIF»
        «IF child instanceof LeafSchemaNode»
            «printLeafNode(child)»
        «ENDIF»
        «IF child instanceof LeafListSchemaNode»
            «printLeafListNode(child)»
        «ENDIF»
        «IF child instanceof ListSchemaNode»
            «printListNode(child)»
        «ENDIF»
        «IF child instanceof ChoiceSchemaNode»
            «printChoiceNode(child)»
        «ENDIF»
        '''
    }

    private def printChoiceNode(ChoiceSchemaNode child) {
        val cases = new ArrayList(child.cases)
        if (!cases.empty) {
            val CaseSchemaNode aCase = cases.get(0)
            for (caseChildNode : aCase.childNodes)
                printAugmentedNode(caseChildNode)
        }
    }

    private def printListNode(ListSchemaNode listNode) {
        return
        '''
            &lt;«listNode.QName.localName»«IF !listNode.QName.namespace.equals(currentModule.namespace)» xmlns="«listNode.QName.namespace»"«ENDIF»&gt;
                «FOR child : listNode.childNodes»
                    «printAugmentedNode(child)»
                «ENDFOR»
            &lt;/«listNode.QName.localName»&gt;
        '''
    }

    private def printContainerNode(ContainerSchemaNode containerNode) {
        return
        '''
            &lt;«containerNode.QName.localName»«IF !containerNode.QName.namespace.equals(currentModule.namespace)» xmlns="«containerNode.QName.namespace»"«ENDIF»&gt;
                «FOR child : containerNode.childNodes»
                    «printAugmentedNode(child)»
                «ENDFOR»
            &lt;/«containerNode.QName.localName»&gt;
        '''
    }

    private def printLeafListNode(LeafListSchemaNode leafListNode) {
        return
        '''
            &lt;«leafListNode.QName.localName»&gt;. . .&lt;/«leafListNode.QName.localName»&gt;
            &lt;«leafListNode.QName.localName»&gt;. . .&lt;/«leafListNode.QName.localName»&gt;
            &lt;«leafListNode.QName.localName»&gt;. . .&lt;/«leafListNode.QName.localName»&gt;
        '''
    }

    private def printAnyXmlNode(AnyxmlSchemaNode anyXmlNode) {
        return
        '''
            &lt;«anyXmlNode.QName.localName»&gt;. . .&lt;/«anyXmlNode.QName.localName»&gt;
        '''
    }

    private def printLeafNode(LeafSchemaNode leafNode) {
        return
        '''
            &lt;«leafNode.QName.localName»&gt;. . .&lt;/«leafNode.QName.localName»&gt;
        '''
    }

    private def augmentationsSummary(Module module, EffectiveModelContext context) {
        if (module.augmentations.empty) {
            return '';
        }
        return '''
        <h3>Augmentations Summary</h3>
        <table>
            <tr>
                <th>Target</th>
                <th>Description</th>
            </tr>
            «FOR augment : module.augmentations»
            <tr>
                <td>
                «anchorLink(schemaPathToString(module, augment.targetPath, context, augment),
                strong(schemaPathToString(module, augment.targetPath, context, augment)))»
                </td>
                <td>
                «augment.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def notifications(Module module) {
        val Collection<? extends NotificationDefinition> notificationdefs = module.notifications
        if (notificationdefs.empty) {
            return '';
        }

        return '''
            <h2>Notifications</h2>
            «FOR notificationdef : notificationdefs»

                <h3 id="«notificationdef.path.schemaPathToId»">«notificationdef.nodeName»</h3>
                    «notificationdef.descAndRef»
                    «FOR childNode : notificationdef.childNodes»
                        «childNode.printSchemaNodeInfo»
                    «ENDFOR»
            «ENDFOR»
        '''
    }

    private def notificationsSummary(Module module) {
        if (module.notifications.empty) {
            return '';
        }
        return '''
        <h3>Notifications Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR notification : module.notifications»
            <tr>
                <td>
                «anchorLink(notification.path.schemaPathToId, strong(notification.QName.localName))»
                </td>
                <td>
                «notification.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def rpcs(Module module) {
        if (module.rpcs.empty) {
            return '';
        }

        return '''
            <h2>RPC Definitions</h2>
            «FOR rpc : module.rpcs»
                <h3 id="«rpc.QName.localName»">«rpc.nodeName»</h3>
                    <ul>
                        «rpc.descAndRefLi»
                        «rpc.input.printSchemaNodeInfo»
                        «rpc.output.printSchemaNodeInfo»
                    </ul>
            «ENDFOR»
            </ul>
        '''
    }

    private def rpcsSummary(Module module) {
        if (module.rpcs.empty) {
            return '';
        }
        return '''
        <h3>RPCs Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR rpc : module.rpcs»
            <tr>
                <td>
                «anchorLink(rpc.QName.localName, strong(rpc.QName.localName))»
                </td>
                <td>
                «rpc.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def extensions(Module module) {
        if (module.extensionSchemaNodes.empty) {
            return '';
        }
        return '''
            <h2>Extensions</h2>
            «FOR ext : module.extensionSchemaNodes»
                <li>
                    <h3 id="«ext.QName.localName»">«ext.nodeName»</h3>
                </li>
                «extensionInfo(ext)»
            «ENDFOR»
        '''
    }

    private def extensionsSummary(Module module) {
        if (module.extensionSchemaNodes.empty) {
            return '';
        }
        return '''
        <h3>Extensions Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR ext : module.extensionSchemaNodes»
            <tr>
                <td>
                «anchorLink(ext.QName.localName, strong(ext.QName.localName))»
                </td>
                <td>
                «ext.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def features(Module module) {
        if (module.features.empty) {
            return '';
        }
        return '''
            <h2>Features</h2>

            <ul>
            «FOR feature : module.features»
                <li>
                    <h3 id="«feature.QName.localName»">«feature.QName.localName»</h3>
                    <ul>
                        «feature.descAndRefLi»
                    </ul>
                </li>
            «ENDFOR»
            </ul>
        '''
    }

    private def featuresSummary(Module module) {
        if (module.features.empty) {
            return '';
        }
        return '''
        <h3>Features Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR feature : module.features»
            <tr>
                <td>
                «anchorLink(feature.QName.localName, strong(feature.QName.localName))»
                </td>
                <td>
                «feature.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    private def objectsSummary(Module module) {
        if (module.childNodes.empty) {
            return '';
        }
        return '''
        <h3>Child Nodes Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR childNode : module.childNodes»
            <tr>
                <td>
                «anchorLink(childNode.QName.localName, strong(childNode.QName.localName))»
                </td>
                <td>
                «childNode.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def header(Module module)
    '''
        <h1>«module.name»</h1>

        <h2>Base Information</h2>
        <table>
            <tr>
                <td>«strong("prefix")»</td>
                <td>«module.prefix»</td>
            </tr>
            <tr>
                <td>«strong("namespace")»</td>
                <td>«module.namespace»</td>
            </tr>
            <tr>
                «IF module.revision.isPresent»
                <td>«strong("revision")»</td>
                <td>«module.revision.get.toString»</td>
                «ENDIF»
            </tr>
            <tr>
                <td>«strong("description")»</td>
                <td>«module.description»</td>
            </tr>
            <tr>
                <td>«strong("yang-version")»</td>
                <td>«module.yangVersion»</td>
            </tr>
            <tr>
                «FOR imp : module.imports BEFORE '''<td>«strong("imports")»</td><td>''' AFTER '''</td>'''»
                    «imp.prefix»:«imp.moduleName»«IF imp.revision.isPresent» «imp.revision.get.toString»«ENDIF»;
                «ENDFOR»
            </tr>
        </table>
    '''

    def CharSequence schemaPathToId(SchemaPath path) {
        if(path !== null) {
            return '''«FOR qName : path.pathFromRoot SEPARATOR "/"»«qName.localName»«ENDFOR»'''
        }
    }

    def code(String string) '''<code>«string»</code>'''

    def process(Module module) {
        throw new UnsupportedOperationException("TODO: auto-generated method stub")
    }

    def CharSequence tree(Module module) '''
        «strong(module.name)»
        «module.childNodes.treeSet(YangInstanceIdentifier.builder.build())»
    '''

    private def CharSequence tree(ChoiceSchemaNode node, YangInstanceIdentifier path) '''
        «node.nodeName» (choice)
        «casesTree(node.cases, path)»
    '''

    def casesTree(Collection<? extends CaseSchemaNode> nodes, YangInstanceIdentifier path) '''
        <ul>
        «FOR node : nodes»
            <li>
            «node.nodeName»
            «node.childNodes.treeSet(path)»
            </li>
        «ENDFOR»
        </ul>
    '''

    private def CharSequence tree(DataSchemaNode node, YangInstanceIdentifier path) {
        if (node instanceof ChoiceSchemaNode) {
            return tree(node, path)
        } else if (node instanceof ListSchemaNode) {
            return tree(node, path)
        } else if (node instanceof ContainerSchemaNode) {
            return tree(node, path)
        }
        return node.nodeName
    }

    private def CharSequence tree(ListSchemaNode node, YangInstanceIdentifier path) '''
        «val newPath = path.append(node)»
        «localLink(newPath,node.nodeName)»
        «node.childNodes.treeSet(newPath)»
    '''

    private def CharSequence tree(ContainerSchemaNode node,YangInstanceIdentifier path) '''
        «val newPath = path.append(node)»
        «localLink(newPath,node.nodeName)»
        «node.childNodes.treeSet(newPath)»
    '''

    def CharSequence childNodes(Module module) '''
        «val childNodes = module.childNodes»
        «IF !childNodes.nullOrEmpty»
            <h2>Child nodes</h2>

            «childNodes.printChildren(3,YangInstanceIdentifier.builder().build())»
        «ENDIF»
    '''

    def CharSequence printSchemaNodeInfo(DataSchemaNode node) {
        return '''
            <ul>
            «node.printBaseInfo»
            «IF node instanceof DataNodeContainer»
                «val dataNode = node as DataNodeContainer»
                <ul>
                «FOR usesNode : dataNode.uses»
                    «usesNode.printUses»
                «ENDFOR»
                </ul>
                <ul>
                «FOR typeDef : dataNode.typeDefinitions»
                    «typeDef.restrictions»
                «ENDFOR»
                </ul>
                <ul>
                «FOR grouping : dataNode.groupings»
                    «grouping.printGrouping»
                «ENDFOR»
                </ul>
                <ul>
                «FOR child : dataNode.childNodes»
                    «child.printSchemaNodeInfo»
                «ENDFOR»
                </ul>
            «ENDIF»
            </ul>
        '''
    }

    def String typeAnchorLink(SchemaPath path, CharSequence text) {
        if(path !== null) {
            val lastElement = path.lastComponent
            val ns = lastElement.namespace
            if (ns == this.currentModule.namespace) {
                return '''<a href="#«path.schemaPathToId»">«text»</a>'''
            } else {
                return '''(«ns»)«text»'''
                //to enable external (import) links
                //return '''<a href="«module».html#«path.schemaPathToId»">«prefix»:«text»</a>'''
            }
        }
    }

    def CharSequence printBaseInfo(SchemaNode node) {
        if(node instanceof LeafSchemaNode) {
            return '''
                «printInfo(node, "leaf")»
                «listItem("type", typeAnchorLink(types.get(node.type), node.type.QName.localName))»
                «listItem("units", node.type.units.orElse(null))»
                «listItem("default", node.type.defaultValue.map([ Object o | o.toString]).orElse(null))»
                </ul>
            '''
        } else if(node instanceof LeafListSchemaNode) {
            return '''
                «printInfo(node, "leaf-list")»
                «IF node.type !== null»
                    «listItem("type", node.type.QName.localName)»
                «ENDIF»
                </ul>
            '''
        } else if(node instanceof ListSchemaNode) {
            return '''
                «printInfo(node, "list")»
                «FOR keyDef : node.keyDefinition»
                    «listItem("key definition", keyDef.localName)»
                «ENDFOR»
                </ul>
            '''
        } else if(node instanceof ChoiceSchemaNode) {
            return '''
                «printInfo(node, "choice")»
                «listItem("default case", node.defaultCase.map([ CaseSchemaNode n | n.getQName.localName]).orElse(null))»
                «FOR caseNode : node.cases»
                    «caseNode.printSchemaNodeInfo»
                «ENDFOR»
                </ul>
            '''
        } else if(node instanceof CaseSchemaNode) {
            return '''
                «printInfo(node, "case")»
                </ul>
            '''
        } else if(node instanceof ContainerSchemaNode) {
            return '''
                «printInfo(node, "container")»
                </ul>
            '''
        } else if(node instanceof AnyxmlSchemaNode) {
            return '''
                «printInfo(node, "anyxml")»
                </ul>
            '''
        }
    }

    def CharSequence printInfo(SchemaNode node, String nodeType) {
        return '''
            «IF node instanceof AugmentationTarget»
                «IF node !== null»
                    <strong>
                    <li id="«node.path.schemaPathToId»">
                        «nodeType»: «node.QName.localName»
                    </li>
                    </strong>
                «ENDIF»
            «ELSE»
                «strong(listItem(nodeType, node.QName.localName))»
            «ENDIF»
            <ul>
            «listItem("description", node.description.orElse(null))»
            «listItem("reference", node.reference.orElse(null))»
            «IF node instanceof DataSchemaNode»
                «IF node.whenCondition.present»
                «listItem("when condition", node.whenCondition.get.toString)»
                «ENDIF»
            «ENDIF»
            «IF node instanceof ElementCountConstraintAware»
                «IF node.elementCountConstraint.present»
                    «val constraint = node.elementCountConstraint.get»
                    «listItem("min elements", constraint.minElements?.toString)»
                    «listItem("max elements", constraint.maxElements?.toString)»
                «ENDIF»
            «ENDIF»
        '''
    }

    def CharSequence printUses(UsesNode usesNode) {
        return '''
            «strong(listItem("uses", typeAnchorLink(usesNode.sourceGrouping.path, usesNode.sourceGrouping.QName.localName)))»
            <ul>
            <li>refines:
                <ul>
                «FOR sp : usesNode.refines.keySet»
                    «listItem("node name", usesNode.refines.get(sp).QName.localName)»
                «ENDFOR»
                </ul>
            </li>
            «FOR augment : usesNode.augmentations»
                «typeAnchorLink(augment.targetPath.asSchemaPath, schemaPathToString(currentModule, augment.targetPath, ctx, augment))»
            «ENDFOR»
            </ul>
        '''
    }

    def CharSequence printGrouping(GroupingDefinition grouping) {
        return '''
            «strong(listItem("grouping", grouping.QName.localName))»
        '''
    }

    def CharSequence printChildren(Iterable<? extends DataSchemaNode> nodes, int level, YangInstanceIdentifier path) {
        val anyxmlNodes = nodes.filter(AnyxmlSchemaNode)
        val leafNodes = nodes.filter(LeafSchemaNode)
        val leafListNodes = nodes.filter(LeafListSchemaNode)
        val choices = nodes.filter(ChoiceSchemaNode)
        val cases = nodes.filter(CaseSchemaNode)
        val containers = nodes.filter(ContainerSchemaNode)
        val lists = nodes.filter(ListSchemaNode)
        return '''
            «IF ((anyxmlNodes.size + leafNodes.size + leafListNodes.size + containers.size + lists.size) > 0)»
            <h3>Direct children</h3>
            <ul>
            «FOR childNode : anyxmlNodes»
                «childNode.printShortInfo(level,path)»
            «ENDFOR»
            «FOR childNode : leafNodes»
                «childNode.printShortInfo(level,path)»
            «ENDFOR»
            «FOR childNode : leafListNodes»
                «childNode.printShortInfo(level,path)»
            «ENDFOR»
            «FOR childNode : containers»
                «childNode.printShortInfo(level,path)»
            «ENDFOR»
            «FOR childNode : lists»
                «childNode.printShortInfo(level,path)»
            «ENDFOR»
            </ul>
            «ENDIF»

            «IF path.pathArguments.iterator.hasNext»
            <h3>XML example</h3>
            «nodes.xmlExample(path.pathArguments.last.nodeType,path)»
            </h3>
            «ENDIF»
            «FOR childNode : containers»
                «childNode.printInfo(level,path)»
            «ENDFOR»
            «FOR childNode : lists»
                «childNode.printInfo(level,path)»
            «ENDFOR»
            «FOR childNode : choices»
                «childNode.printInfo(level,path)»
            «ENDFOR»
            «FOR childNode : cases»
                «childNode.printInfo(level,path)»
            «ENDFOR»
        '''
    }

    def CharSequence xmlExample(Iterable<? extends DataSchemaNode> nodes, QName name, YangInstanceIdentifier path) '''
    <pre>
        «xmlExampleTag(name,nodes.xmplExampleTags(path))»
    </pre>
    '''

    def CharSequence xmplExampleTags(Iterable<? extends DataSchemaNode> nodes, YangInstanceIdentifier identifier) '''
        <!-- Child nodes -->
        «FOR node : nodes»
        <!-- «node.QName.localName» -->
            «node.asXmlExampleTag(identifier)»
        «ENDFOR»

    '''

    private def CharSequence asXmlExampleTag(DataSchemaNode node, YangInstanceIdentifier identifier) {
        if (node instanceof LeafSchemaNode) {
            return '''«node.QName.xmlExampleTag("...")»'''
        }
        if (node instanceof LeafListSchemaNode) {
            return '''
            &lt!-- This node could appear multiple times --&gt
            «node.QName.xmlExampleTag("...")»
            '''
        }
        if (node instanceof ContainerSchemaNode) {
            return '''
            &lt!-- See «localLink(identifier.append(node),"definition")» for child nodes.  --&gt
            «node.QName.xmlExampleTag("...")»
            '''
        }
        if (node instanceof ListSchemaNode) {
            return '''
            &lt!-- See «localLink(identifier.append(node),"definition")» for child nodes.  --&gt
            &lt!-- This node could appear multiple times --&gt
            «node.QName.xmlExampleTag("...")»
            '''
        }
        return "<!-- noop -->"
    }

    def xmlExampleTag(QName name, CharSequence data) {
        return '''&lt;«name.localName» xmlns="«name.namespace»"&gt;«data»&lt;/«name.localName»&gt;'''
    }

    def header(int level,QName name) '''<h«level»>«name.localName»</h«level»>'''


    def header(int level,YangInstanceIdentifier name) '''
        <h«level» id="«FOR cmp : name.pathArguments SEPARATOR "/"»«cmp.nodeType.localName»«ENDFOR»">
            «FOR cmp : name.pathArguments SEPARATOR "/"»«cmp.nodeType.localName»«ENDFOR»
        </h«level»>
    '''

    private def CharSequence printInfo(ContainerSchemaNode node, int level, YangInstanceIdentifier path) '''
        «val newPath = path.append(node)»
        «header(level,newPath)»
        <dl>
          <dt>XML Path</dt>
          <dd>«newPath.asXmlPath»</dd>
          <dt>Restconf path</dt>
          <dd>«code(newPath.asRestconfPath)»</dd>
        </dl>
        «node.childNodes.printChildren(level,newPath)»
    '''

    private def CharSequence printInfo(ListSchemaNode node, int level, YangInstanceIdentifier path) '''
        «val newPath = path.append(node)»
        «header(level,newPath)»
        <dl>
          <dt>XML Path</dt>
          <dd>«newPath.asXmlPath»</dd>
          <dt>Restconf path</dt>
          <dd>«code(newPath.asRestconfPath)»</dd>
        </dl>
        «node.childNodes.printChildren(level,newPath)»
    '''

    private def CharSequence printInfo(ChoiceSchemaNode node, int level, YangInstanceIdentifier path) '''
        «val Set<DataSchemaNode> choiceCases = new HashSet(node.cases)»
        «choiceCases.printChildren(level, path)»
    '''

    private def CharSequence printInfo(CaseSchemaNode node, int level, YangInstanceIdentifier path) '''
        «node.childNodes.printChildren(level, path)»
    '''



    def CharSequence printShortInfo(ContainerSchemaNode node, int level, YangInstanceIdentifier path) {
        val newPath = path.append(node);
        return '''
            <li>«strong(localLink(newPath,node.QName.localName))» (container)
            <ul>
                «node.configurationDataItem»
            </ul>
            </li>
        '''
    }

    def CharSequence printShortInfo(ListSchemaNode node, int level, YangInstanceIdentifier path) {
        val newPath = path.append(node);
        return '''
            <li>«strong(localLink(newPath,node.QName.localName))» (list)
            <ul>
                «node.configurationDataItem»
            </ul>
            </li>
        '''
    }

    def CharSequence printShortInfo(AnyxmlSchemaNode node, int level, YangInstanceIdentifier path) {
        return '''
            <li>«strong((node.QName.localName))» (anyxml)
            <ul>
                «node.configurationDataItem»
                «node.mandatoryItem»
            </ul>
            </li>
        '''
    }

    def CharSequence printShortInfo(LeafSchemaNode node, int level, YangInstanceIdentifier path) {
        return '''
            <li>«strong((node.QName.localName))» (leaf)
            <ul>
                «node.configurationDataItem»
                «node.mandatoryItem»
            </ul>
            </li>
        '''
    }

    def CharSequence printShortInfo(LeafListSchemaNode node, int level, YangInstanceIdentifier path) {
        return '''
            <li>«strong((node.QName.localName))» (leaf-list)
            <ul>
                «node.configurationDataItem»
            </ul>
            </li>
        '''
    }

    def CharSequence anchorLink(CharSequence anchor, CharSequence text) {
        return '''
            <a href="#«anchor»">«text»</a>
        '''
    }

    def CharSequence localLink(YangInstanceIdentifier identifier, CharSequence text) '''
        <a href="#«FOR cmp : identifier.pathArguments SEPARATOR "/"»«cmp.nodeType.localName»«ENDFOR»">«text»</a>
    '''

    private static def String configurationDataItem(DataSchemaNode node) {
        return node.effectiveConfig
            .map([config | "<li>configuration data: " + strong(String.valueOf(config)) + "</li>"])
            .orElse("")
    }

    private static def CharSequence mandatoryItem(MandatoryAware node) '''
        <li>mandatory: «strong(String.valueOf(node.mandatory))»</li>
    '''

    private def dispatch YangInstanceIdentifier append(YangInstanceIdentifier identifier, ContainerSchemaNode node) {
        return identifier.node(node.QName);
    }

    private def dispatch YangInstanceIdentifier append(YangInstanceIdentifier identifier, ListSchemaNode node) {
        val keyValues = new LinkedHashMap<QName,Object>();
        if(node.keyDefinition !== null) {
            for(definition : node.keyDefinition) {
                keyValues.put(definition,new Object);
            }
        }

        return identifier.node(NodeIdentifierWithPredicates.of(node.QName, keyValues));
    }


    def asXmlPath(YangInstanceIdentifier identifier) {
        return "";
    }

    def asRestconfPath(YangInstanceIdentifier identifier) {
        val it = new StringBuilder();
        append(currentModule.name)
        append(':')
        var previous = false;
        for(arg : identifier.pathArguments) {
            if(previous) append('/')
            append(arg.nodeType.localName);
            previous = true;
            if(arg instanceof NodeIdentifierWithPredicates) {
                for(qname : arg.keySet) {
                    append("/{");
                    append(qname.localName)
                    append('}')
                }
            }
        }

        return it.toString;
    }

    private def String schemaPathToString(Module module, SchemaNodeIdentifier schemaPath, EffectiveModelContext ctx,
            DataNodeContainer dataNode) {
        val path = schemaPath.nodeIdentifiers
        val StringBuilder pathString = new StringBuilder()
        if (schemaPath instanceof Absolute) {
            pathString.append('/')
        }

        val QName qname = path.get(0)
        var Object parent = ctx.findModule(qname.module).orElse(null)

        for (name : path) {
            if (parent instanceof DataNodeContainer) {
                var SchemaNode node = parent.dataChildByName(name)
                if (node === null && (parent instanceof Module)) {
                    val notifications = (parent as Module).notifications;
                    for (notification : notifications) {
                        if (notification.QName.equals(name)) {
                            node = notification
                        }
                    }
                }
                if (node === null && (parent instanceof Module)) {
                    val rpcs = (parent as Module).rpcs;
                    for (rpc : rpcs) {
                        if (rpc.QName.equals(name)) {
                            node = rpc
                        }
                    }
                }

                val pathElementModule = ctx.findModule(name.module).get
                val String moduleName = pathElementModule.name
                pathString.append(moduleName)
                pathString.append(':')
                pathString.append(name.localName)
                pathString.append('/')
                if(node instanceof ChoiceSchemaNode && dataNode !== null) {
                    val DataSchemaNode caseNode = dataNode.childNodes.findFirst[DataSchemaNode e | e instanceof CaseSchemaNode];
                    if(caseNode !== null) {
                        pathString.append("(case)");
                        pathString.append(caseNode.QName.localName);
                    }
                }
                parent = node
            }
        }
        return pathString.toString;
    }


    def CharSequence childNodesInfoTree(Map<SchemaPath, DataSchemaNode> childNodes) '''
        «IF childNodes !== null && !childNodes.empty»
            «FOR child : childNodes.values»
                «childInfo(child, childNodes)»
            «ENDFOR»
        «ENDIF»
    '''

    def CharSequence childInfo(DataSchemaNode node, Map<SchemaPath, DataSchemaNode> childNodes) '''
        «val String path = nodeSchemaPathToPath(node, childNodes)»
        «IF path !== null»
            «code(path)»
                «IF node !== null»
                <ul>
                «node.descAndRefLi»
                </ul>
            «ENDIF»
        «ENDIF»
    '''

    private def CharSequence treeSet(Collection<? extends DataSchemaNode> childNodes, YangInstanceIdentifier path) '''
        «IF childNodes !== null && !childNodes.empty»
            <ul>
            «FOR child : childNodes»
                <li>
                    «child.tree(path)»
                </li>
            «ENDFOR»
            </ul>
        «ENDIF»
    '''

    def listKeys(ListSchemaNode node) '''
        [«FOR key : node.keyDefinition SEPARATOR " "»«key.localName»«ENDFOR»]
    '''

    private def CharSequence extensionInfo(ExtensionDefinition ext) '''
        <ul>
            «ext.descAndRefLi»
            «listItem("Argument", ext.argument)»
        </ul>
    '''


    /* #################### RESTRICTIONS #################### */
    private def restrictions(TypeDefinition<?> type) '''
        «type.baseType.toBaseStmt»
        «type.toLength»
        «type.toRange»
    '''

    private def toLength(TypeDefinition<?> type) '''
        «IF type instanceof LengthRestrictedTypeDefinition»
            «type.lengthConstraint.toLengthStmt»
        «ENDIF»
    '''

    private def toRange(TypeDefinition<?> type) '''
        «IF type instanceof RangeRestrictedTypeDefinition»
            «type.rangeConstraint.toRangeStmt»
        «ENDIF»
    '''

    def toLengthStmt(Optional<LengthConstraint> lengths) '''
        «IF lengths.isPresent»
            «listItem("Length restrictions:")»
            <ul>
            «FOR length : lengths.get.allowedRanges.asRanges»
                <li>
                «IF length.lowerEndpoint == length.upperEndpoint»
                    «length.lowerEndpoint»
                «ELSE»
                    &lt;«length.lowerEndpoint», «length.upperEndpoint»&gt;
                «ENDIF»
                </li>
            «ENDFOR»
            </ul>
        «ENDIF»
    '''

    def toRangeStmt(Optional<? extends RangeConstraint<?>> constraint) '''
        «IF constraint.present»
            «listItem("Range restrictions:")»
            <ul>
            «FOR range : constraint.get.allowedRanges.asRanges»
                <li>
                «IF range.lowerEndpoint == range.upperEndpoint»
                    «range.lowerEndpoint»
                «ELSE»
                    &lt;«range.lowerEndpoint», «range.upperEndpoint»&gt;
                «ENDIF»
                </li>
            «ENDFOR»
            </ul>
        «ENDIF»
    '''

    def toBaseStmt(TypeDefinition<?> baseType) '''
        «IF baseType !== null»
        «listItem("Base type", typeAnchorLink(types.get(baseType), baseType.QName.localName))»
        «ENDIF»
    '''

    /* #################### UTILITY #################### */
    private def static String strong(CharSequence str) '''<strong>«str»</strong>'''
    private def static italic(CharSequence str) '''<i>«str»</i>'''

    def CharSequence descAndRefLi(SchemaNode node) '''
        «listItem("Description", node.description.orElse(null))»
        «listItem("Reference", node.reference.orElse(null))»
    '''

    def CharSequence descAndRef(SchemaNode node) '''
        «node.description»
        «IF node.reference !== null»
            Reference «node.reference»
        «ENDIF»
    '''

    private def listItem(String value) '''
        «IF value !== null && !value.empty»
            <li>
                «value»
            </li>
        «ENDIF»
    '''

    private def listItem(String name, String value) '''
        «IF value !== null && !value.empty»
            <li>
                «name»: «value»
            </li>
        «ENDIF»
    '''

    private def String nodeSchemaPathToPath(DataSchemaNode node, Map<SchemaPath, DataSchemaNode> childNodes) {
        if (node instanceof ChoiceSchemaNode || node instanceof CaseSchemaNode) {
            return null
        }

        val path = node.path.pathFromRoot
        val absolute = node.path.absolute;
        var StringBuilder result = new StringBuilder
        if (absolute) {
            result.append('/')
        }
        if (path !== null && !path.empty) {
            val actual = new ArrayList()
            var i = 0;
            for (pathElement : path) {
                actual.add(pathElement)
                val DataSchemaNode nodeByPath = childNodes.get(SchemaPath.create(actual, absolute))
                if (!(nodeByPath instanceof ChoiceSchemaNode) && !(nodeByPath instanceof CaseSchemaNode)) {
                    result.append(pathElement.localName)
                    if (i != path.size - 1) {
                        result.append('/')
                    }
                }
                i = i + 1
            }
        }
        return result.toString
    }

    private def addedByInfo(SchemaNode node) {
        if (node instanceof DataSchemaNode) {
            return addedByInfo(node)
        }
        return ""
    }

    private def addedByInfo(DataSchemaNode node) '''
        «IF node.augmenting»(A)«ENDIF»«IF node.addedByUses»(U)«ENDIF»
    '''

    private def isAddedBy(SchemaNode node) {
        if (node instanceof DataSchemaNode) {
            return node.augmenting || node.addedByUses
        }
        return false
    }

    private def nodeName(SchemaNode node) {
        if (node instanceof ContainerSchemaNode) {
            return nodeName(node);
        } else if (node instanceof ListSchemaNode) {
            return nodeName(node);
        }
        val addedByInfo = node.addedByInfo
        if (node.isAddedBy) {
            return '''«italic(node.QName.localName)»«addedByInfo»'''
        }
        return '''«node.QName.localName»«addedByInfo»'''
    }

    private def nodeName(ContainerSchemaNode node) '''
        «IF node.isAddedBy»
            «strong(italic(node.QName.localName))»«node.addedByInfo»
        «ELSE»
            «strong(node.QName.localName)»«node.addedByInfo»
        «ENDIF»
    '''

    private def nodeName(ListSchemaNode node) '''
        «IF node.isAddedBy»
            «strong(italic(node.QName.localName))» «IF node.keyDefinition !== null && !node.keyDefinition.empty»«node.listKeys»«ENDIF»«node.addedByInfo»
        «ELSE»
            «strong(node.QName.localName)» «IF node.keyDefinition !== null && !node.keyDefinition.empty»«node.listKeys»«ENDIF»
        «ENDIF»
    '''

}
