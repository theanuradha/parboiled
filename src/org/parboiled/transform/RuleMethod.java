/*
 * Copyright (c) 2009-2010 Ken Wenzel and Mathias Doenitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.parboiled.transform;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;
import org.parboiled.support.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.parboiled.transform.AsmUtils.*;

class RuleMethod extends MethodNode implements Opcodes, Types {

    private final List<InstructionGroup> groups = new ArrayList<InstructionGroup>();
    private final List<LabelNode> usedLabels = new ArrayList<LabelNode>();

    private int parameterCount;
    private boolean containsImplicitActions; // calls to Boolean.valueOf(boolean)
    private boolean containsExplicitActions; // calls to BaseParser.ACTION(boolean)
    private boolean containsCaptures; // calls to BaseParser.CAPTURE(boolean)
    private boolean hasExplicitActionOnlyAnnotation;
    private boolean hasCachedAnnotation;
    private boolean hasLabelAnnotation;
    private boolean hasSuppressNodeAnnotation;
    private boolean hasSuppressSubnodesAnnotation;
    private boolean hasSkipNodeAnnotation;
    private boolean hasSkipActionsInPredicatesAnnotation;
    private int numberOfReturns;
    private InstructionGraphNode returnInstructionNode;
    private List<InstructionGraphNode> graphNodes;
    private List<LocalVariableNode> localVarVariables;

    public RuleMethod(int access, String name, String desc, String signature, String[] exceptions,
                      boolean hasExplicitActionOnlyAnno, boolean hasDontLabelAnnotation,
                      boolean hasSkipActionsInPredicates) {
        super(access, name, desc, signature, exceptions);

        parameterCount = Type.getArgumentTypes(desc).length;
        hasCachedAnnotation = parameterCount == 0;
        hasLabelAnnotation = parameterCount == 0 && !hasDontLabelAnnotation;
        hasExplicitActionOnlyAnnotation = hasExplicitActionOnlyAnno;
        hasSkipActionsInPredicatesAnnotation = hasSkipActionsInPredicates;
    }

    public List<InstructionGroup> getGroups() {
        return groups;
    }

    public List<LabelNode> getUsedLabels() {
        return usedLabels;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public boolean containsImplicitActions() {
        return containsImplicitActions;
    }

    public void setContainsImplicitActions(boolean containsImplicitActions) {
        this.containsImplicitActions = containsImplicitActions;
    }

    public boolean containsExplicitActions() {
        return containsExplicitActions;
    }

    public void setContainsExplicitActions(boolean containsExplicitActions) {
        this.containsExplicitActions = containsExplicitActions;
    }

    public boolean containsCaptures() {
        return containsCaptures;
    }

    public boolean hasCachedAnnotation() {
        return hasCachedAnnotation;
    }

    public boolean hasLabelAnnotation() {
        return hasLabelAnnotation;
    }

    public boolean hasSuppressNodeAnnotation() {
        return hasSuppressNodeAnnotation;
    }

    public boolean hasSuppressSubnodesAnnotation() {
        return hasSuppressSubnodesAnnotation;
    }

    public boolean hasSkipActionsInPredicatesAnnotation() {
        return hasSkipActionsInPredicatesAnnotation;
    }

    public boolean hasSkipNodeAnnotation() {
        return hasSkipNodeAnnotation;
    }

    public int getNumberOfReturns() {
        return numberOfReturns;
    }

    public InstructionGraphNode getReturnInstructionNode() {
        return returnInstructionNode;
    }

    public void setReturnInstructionNode(InstructionGraphNode returnInstructionNode) {
        this.returnInstructionNode = returnInstructionNode;
    }

    public List<InstructionGraphNode> getGraphNodes() {
        return graphNodes;
    }

    public List<LocalVariableNode> getLocalVarVariables() {
        return localVarVariables;
    }

    public InstructionGraphNode setGraphNode(AbstractInsnNode insn, BasicValue resultValue, List<Value> predecessors) {
        if (graphNodes == null) {
            // initialize with a list of null values
            graphNodes = new ArrayList<InstructionGraphNode>(
                    Arrays.asList(new InstructionGraphNode[instructions.size()]));
        }
        int index = instructions.indexOf(insn);
        InstructionGraphNode node = graphNodes.get(index);
        if (node == null) {
            node = new InstructionGraphNode(insn, resultValue);
            graphNodes.set(index, node);
        }
        node.addPredecessors(predecessors);
        return node;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (EXPLICIT_ACTIONS_ONLY_DESC.equals(desc)) {
            hasExplicitActionOnlyAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (CACHED_DESC.equals(desc)) {
            hasCachedAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (SUPPRESS_NODE_DESC.equals(desc)) {
            hasSuppressNodeAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (SUPPRESS_SUBNODES_DESC.equals(desc)) {
            hasSuppressSubnodesAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (SUPPRESS_SUBNODES_DESC.equals(desc)) {
            hasSuppressSubnodesAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (SKIP_NODE_DESC.equals(desc)) {
            hasSkipNodeAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (SKIP_ACTIONS_IN_PREDICATES_DESC.equals(desc)) {
            hasSkipActionsInPredicatesAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (DONT_SKIP_ACTIONS_IN_PREDICATES_DESC.equals(desc)) {
            hasSkipActionsInPredicatesAnnotation = false;
            return null; // we do not need to record this annotation
        }
        if (DONT_LABEL_DESC.equals(desc)) {
            hasLabelAnnotation = false;
            return null; // we do not need to record this annotation
        }
        if (LABEL_DESC.equals(desc)) {
            hasLabelAnnotation = true;
        }
        return visible ? super.visitAnnotation(desc, true) : null; // only keep visible annotations
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (!hasExplicitActionOnlyAnnotation && opcode == INVOKESTATIC && isBooleanValueOfZ(owner, name, desc)) {
            containsImplicitActions = true;
        }
        if (opcode == INVOKESTATIC && isActionRoot(owner, name)) {
            containsExplicitActions = true;
        }
        if (opcode == INVOKESTATIC && isCaptureRoot(owner, name)) {
            containsCaptures = true;
        }
        super.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == ARETURN) numberOfReturns++;
        super.visitInsn(opcode);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        usedLabels.add(getLabelNode(label));
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        // do not record line numbers
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        // only remember the local variables of Type org.parboiled.support.Var that are not parameters
        if (index > parameterCount && Var.class.isAssignableFrom(getClassForType(Type.getType(desc)))) {
            if (localVarVariables == null) localVarVariables = new ArrayList<LocalVariableNode>();
            localVarVariables.add(new LocalVariableNode(name, desc, null, null, null, index));
        }
    }

    @Override
    public String toString() {
        return name;
    }

}
