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

package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Checks;
import org.parboiled.support.Converter;
import static org.parboiled.support.ParseTreeUtils.collectNodesByLabel;
import static org.parboiled.support.ParseTreeUtils.collectNodesByPath;
import org.parboiled.utils.Preconditions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates parameters passed to parser actions.
 */
abstract class ActionParameter<V> {

    // the type of the action method parameter that is to be provided by this instance
    protected Class<?> expectedParameterType;

    public void setExpectedType(Class<?> parameterType) {
        expectedParameterType = parameterType;
    }

    abstract Object getValue(@NotNull MatcherContext<V> context);

    //////////////////////////////////// SPECIALIZATION //////////////////////////////////////////

    /**
     * The base class of all ActionParameters that operate on Node paths.
     */
    abstract static class PathBasedActionParameter<V> extends ActionParameter<V> {
        protected final String path;

        protected PathBasedActionParameter(String path) {
            this.path = path;
        }

        protected ArrayList<org.parboiled.Node<V>> collectPathNodes(MatcherContext<V> context) {
            return collectNodesByPath(context.getSubNodes(), path, new ArrayList<org.parboiled.Node<V>>());
        }
    }

    //////////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////////

    static class Node<V> extends PathBasedActionParameter<V> {
        public Node(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext<V> context) {
            Checks.ensure(expectedParameterType.isAssignableFrom(org.parboiled.Node.class),
                    "Illegal action argument in '%s', expected %s instead of %s",
                    context.getPath(), expectedParameterType, org.parboiled.Node.class);
            Preconditions.checkState(expectedParameterType.isAssignableFrom(org.parboiled.Node.class));
            return context.getNodeByPath(path);
        }
    }

    static class Nodes<V> extends PathBasedActionParameter<V> {
        public Nodes(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext<V> context) {
            Class<?> componentType = expectedParameterType.getComponentType();
            Checks.ensure(expectedParameterType.isArray() && componentType.isAssignableFrom(org.parboiled.Node.class),
                    "Illegal action argument in '%s', expected %s instead of %s",
                    context.getPath(), expectedParameterType, org.parboiled.Node[].class);
            List<org.parboiled.Node<V>> list = collectPathNodes(context);
            return list.toArray((org.parboiled.Node[]) Array.newInstance(componentType, list.size()));
        }
    }

    static class NodeWithLabel<V> extends PathBasedActionParameter<V> {
        public NodeWithLabel(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext<V> context) {
            Checks.ensure(expectedParameterType.isAssignableFrom(org.parboiled.Node.class),
                    "Illegal action argument in '%s', expected %s instead of %s",
                    context.getPath(), expectedParameterType, org.parboiled.Node.class);
            Preconditions.checkState(expectedParameterType.isAssignableFrom(org.parboiled.Node.class));
            return context.getNodeByLabel(path);
        }
    }

    static class NodesWithLabel<V> extends PathBasedActionParameter<V> {
        public NodesWithLabel(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext<V> context) {
            Class<?> componentType = expectedParameterType.getComponentType();
            Checks.ensure(expectedParameterType.isArray() && componentType.isAssignableFrom(org.parboiled.Node.class),
                    "Illegal action argument in '%s', expected %s instead of %s",
                    context.getPath(), expectedParameterType, org.parboiled.Node[].class);
            List<org.parboiled.Node<V>> list = collectNodesByLabel(context.getSubNodes(), path,
                    new ArrayList<org.parboiled.Node<V>>());
            return list.toArray((org.parboiled.Node[]) Array.newInstance(componentType, list.size()));
        }
    }

    static class Value<V> extends PathBasedActionParameter<V> {
        public Value(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext<V> context) {
            org.parboiled.Node<V> node = context.getNodeByPath(path);
            if (node == null || node.getValue() == null) return null;
            V value = node.getValue();
            Checks.ensure(expectedParameterType.isAssignableFrom(value.getClass()),
                    "Illegal action argument in '%s', cannot cast %s to %s",
                    context.getPath(), value.getClass(), expectedParameterType);
            return value;
        }
    }

    static class Values<V> extends PathBasedActionParameter<V> {
        public Values(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext<V> context) {
            Class<?> componentType = expectedParameterType.getComponentType();
            Preconditions.checkState(expectedParameterType.isArray() && !componentType.isPrimitive());
            List<org.parboiled.Node<V>> nodes = collectPathNodes(context);
            Object array = Array.newInstance(componentType, nodes.size());
            for (int i = 0; i < nodes.size(); i++) {
                V value = nodes.get(i).getValue();
                if (value == null) continue;
                Checks.ensure(componentType.isAssignableFrom(value.getClass()),
                        "Illegal action argument in '%s', cannot cast value array component from %s to %s",
                        context.getPath(), value.getClass(), componentType);
                Array.set(array, i, value);
            }
            return array;
        }
    }

    static class Text<V> extends PathBasedActionParameter<V> {
        public Text(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext<V> context) {
            Preconditions.checkState(expectedParameterType.isAssignableFrom(String.class));
            org.parboiled.Node<V> node = context.getNodeByPath(path);
            return context.getNodeText(node);
        }
    }

    static class Texts<V> extends PathBasedActionParameter<V> {
        public Texts(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext<V> context) {
            Preconditions.checkState(
                    expectedParameterType.isArray() && expectedParameterType.getComponentType() == String.class);
            List<org.parboiled.Node<V>> nodes = collectPathNodes(context);
            String[] texts = new String[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                texts[i] = context.getNodeText(nodes.get(i));
            }
            return texts;
        }
    }

    static class Char<V> extends PathBasedActionParameter<V> {
        public Char(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext<V> context) {
            Preconditions.checkState(expectedParameterType.isAssignableFrom(Character.class));
            org.parboiled.Node<V> node = context.getNodeByPath(path);
            return context.getNodeChar(node);
        }
    }

    static class Chars<V> extends PathBasedActionParameter<V> {
        public Chars(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext<V> context) {
            Preconditions.checkState(
                    expectedParameterType.isArray() && expectedParameterType.getComponentType() == Character.class);
            List<org.parboiled.Node<V>> nodes = collectPathNodes(context);
            Character[] chars = new Character[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                chars[i] = context.getNodeChar(nodes.get(i));
            }
            return chars;
        }
    }

    static class FirstOfNonNull<V> extends ActionParameter<V> {
        private final Object[] args;

        public FirstOfNonNull(@NotNull Object[] args) {
            this.args = args;
        }

        @Override
        public void setExpectedType(Class<?> parameterType) {
            super.setExpectedType(parameterType);
            for (Object arg : args) {
                if (arg instanceof ActionParameter) {
                    ((ActionParameter) arg).setExpectedType(parameterType);
                }
            }
        }

        @SuppressWarnings({"unchecked"})
        Object getValue(@NotNull MatcherContext<V> context) {
            for (Object arg : args) {
                if (arg instanceof ActionParameter) {
                    ActionParameter<V> param = (ActionParameter<V>) arg;
                    arg = param.getValue(context);
                }
                if (arg != null) {
                    Checks.ensure(expectedParameterType.isAssignableFrom(arg.getClass()),
                            "Illegal action argument in firstOfNonNull(...) in '%s', cannot cast %s to %s",
                            context.getPath(), arg.getClass(), expectedParameterType);
                    return arg;
                }
            }
            return null;
        }
    }

    static class Convert<V,T> extends ActionParameter<V> {
        private final Object arg;
        private final Converter<T> converter;

        public Convert(Object arg, @NotNull Converter<T> converter) {
            this.arg = arg;
            this.converter = converter;
        }

        @Override
        public void setExpectedType(Class<?> parameterType) {
            super.setExpectedType(parameterType);
            if (arg instanceof ActionParameter) {
                ((ActionParameter) arg).setExpectedType(String.class);
            }
        }

        @SuppressWarnings({"unchecked"})
        Object getValue(@NotNull MatcherContext<V> context) {
            String text;
            if (arg instanceof ActionParameter) {
                ActionParameter<V> param = (ActionParameter<V>) arg;
                text = (String) param.getValue(context);
            } else {
                Preconditions.checkState(arg instanceof String);
                text = (String) arg;
            }
            return text == null ? null : converter.parse(text);
        }
    }

}
