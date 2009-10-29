/*
 * Copyright (C) 2009 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.support;

import org.jetbrains.annotations.NotNull;
import org.parboiled.Node;
import org.parboiled.ParsingResult;
import static org.parboiled.utils.DGraphUtils.hasChildren;
import static org.parboiled.utils.DGraphUtils.printTree;
import org.parboiled.utils.StringUtils;

import java.util.Collection;

/**
 * General utility methods for operating on parse trees.
 */
public class ParseTreeUtils {

    private ParseTreeUtils() {}

    /**
     * Returns the first Node underneath the given parent that matches the given path.
     * The path is a '/' separated list of Node label prefixes describing the ancestor chain of the sought for Node
     * relative to the given parent.
     * If parent is null or no node is found the method returns null.
     *
     * @param parent the parent Node
     * @param path   the path to the Node being searched for
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findNodeByPath(Node<V> parent, @NotNull String path) {
        return parent != null && hasChildren(parent) ? findNodeByPath(parent.getChildren(), path) : null;
    }

    /**
     * Returns the first Node underneath the given parents that matches the given path.
     * The path is a '/' separated list of Node label prefixes describing the ancestor chain of the sought for Node
     * relative to each of the given parent nodes.
     * If the given collections of parents is null or empty or no node is found the method returns null.
     *
     * @param parents the parent Nodes to look through
     * @param path    the path to the Node being searched for
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findNodeByPath(Collection<Node<V>> parents, @NotNull String path) {
        if (parents != null && !parents.isEmpty()) {
            int separatorIndex = path.indexOf('/');
            String prefix = separatorIndex != -1 ? path.substring(0, separatorIndex) : path;
            for (Node<V> child : parents) {
                if (StringUtils.startsWith(child.getLabel(), prefix)) {
                    return separatorIndex == -1 ? child : findNodeByPath(child, path.substring(separatorIndex + 1));
                }
            }
        }
        return null;
    }

    /**
     * Collects all Nodes underneath the given parent that match the given path.
     * The path is a '/' separated list of Node label prefixes describing the ancestor chain of the sought for Nodes
     * relative to the given parent.
     *
     * @param parent     the parent Node
     * @param path       the path to the Nodes being searched for
     * @param collection the collection to collect the found Nodes into
     * @return the same collection instance passed as a parameter
     */
    public static <V, C extends Collection<Node<V>>> C collectNodesByPath(Node<V> parent,
                                                                          @NotNull String path,
                                                                          @NotNull C collection) {
        return parent != null && hasChildren(parent) ?
                collectNodesByPath(parent.getChildren(), path, collection) : collection;
    }

    /**
     * Collects all Nodes underneath the given parents that match the given path.
     * The path is a '/' separated list of Node label prefixes describing the ancestor chain of the sought for Nodes
     * relative to each of the given parent nodes.
     *
     * @param parents    the parent Nodes to look through
     * @param path       the path to the Nodes being searched for
     * @param collection the collection to collect the found Nodes into
     * @return the same collection instance passed as a parameter
     */
    public static <V, C extends Collection<Node<V>>> C collectNodesByPath(Collection<Node<V>> parents,
                                                                          @NotNull String path,
                                                                          @NotNull C collection) {
        if (parents != null && !parents.isEmpty()) {
            int separatorIndex = path.indexOf('/');
            String prefix = separatorIndex != -1 ? path.substring(0, separatorIndex) : path;
            for (Node<V> child : parents) {
                if (StringUtils.startsWith(child.getLabel(), prefix)) {
                    if (separatorIndex == -1) {
                        collection.add(child);
                    } else {
                        collectNodesByPath(child, path.substring(separatorIndex + 1), collection);
                    }
                }
            }
        }
        return collection;
    }

    /**
     * Returns the first Node underneath the given parent that matches the given label prefix.
     * If parent is null or no node is found the method returns null.
     *
     * @param parent the parent Node
     * @param label  the label to look for
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findNodeByLabel(Node<V> parent, @NotNull String label) {
        if (parent != null) {
            if (StringUtils.startsWith(parent.getLabel(), label)) return parent;
            if (hasChildren(parent)) {
                Node<V> found = findNodeByLabel(parent.getChildren(), label);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Returns the first Node underneath the given parents that matches the given label prefix.
     * If parents is null or empty or no node is found the method returns null.
     *
     * @param parents the parent Nodes to look through
     * @param label   the label to look for
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findNodeByLabel(Collection<Node<V>> parents, @NotNull String label) {
        if (parents != null && !parents.isEmpty()) {
            for (Node<V> child : parents) {
                Node<V> found = findNodeByLabel(child, label);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Collects all Nodes underneath the given parent that match the given label (prefix).
     *
     * @param parent      the parent Node
     * @param labelPrefix the path to the Nodes being searched for
     * @param collection  the collection to collect the found Nodes into
     * @return the same collection instance passed as a parameter
     */
    public static <V, C extends Collection<Node<V>>> C collectNodesByLabel(Node<V> parent,
                                                                           @NotNull String labelPrefix,
                                                                           @NotNull C collection) {
        return parent != null && hasChildren(parent) ?
                collectNodesByPath(parent.getChildren(), labelPrefix, collection) : collection;
    }

    /**
     * Collects all Nodes underneath the given parents that match the given label (prefix).
     *
     * @param parents     the parent Nodes to look through
     * @param labelPrefix the path to the Nodes being searched for
     * @param collection  the collection to collect the found Nodes into
     * @return the same collection instance passed as a parameter
     */
    public static <V, C extends Collection<Node<V>>> C collectNodesByLabel(Collection<Node<V>> parents,
                                                                     @NotNull String labelPrefix,
                                                                     @NotNull C collection) {
        if (parents != null && !parents.isEmpty()) {
            for (Node<V> child : parents) {
                if (StringUtils.startsWith(child.getLabel(), labelPrefix)) {
                    collection.add(child);
                }
                collectNodesByPath(child, labelPrefix, collection);
            }
        }
        return collection;
    }

    /**
     * Returns the input text matched by the given Node.
     *
     * @param node        the node
     * @param inputBuffer the underlying inputBuffer
     * @return null if node is null otherwise a string with the matched input text (which can be empty)
     */
    public static String getNodeText(Node<?> node, @NotNull InputBuffer inputBuffer) {
        return node != null ? inputBuffer.extract(node.getStartLocation().index, node.getEndLocation().index) : null;
    }

    /**
     * Returns the first input character matched by the given Node.
     *
     * @param node        the node
     * @param inputBuffer the underlying inputBuffer
     * @return null if node is null or did not match at least one character otherwise the first matched input char
     */
    public static Character getNodeChar(Node<?> node, InputBuffer inputBuffer) {
        return node != null && node.getEndLocation().index > node.getStartLocation().index ?
                inputBuffer.charAt(node.getStartLocation().index) : null;
    }

    /**
     * Creates a readable string represenation of the parse tree in thee given ParsingResult object.
     *
     * @param parsingResult the parsing result containing the parse tree
     * @return a new String
     */
    public static String printNodeTree(@NotNull ParsingResult<?> parsingResult) {
        return printTree(parsingResult.root, new NodeFormatter(parsingResult.inputBuffer));
    }

}

