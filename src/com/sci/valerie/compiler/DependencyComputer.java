package com.sci.valerie.compiler;

import java.util.*;

import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.stmt.*;
import com.sci.valerie.parser.tree.stmt.decl.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.atom.*;
import com.sci.valerie.parser.tree.expr.type.*;

public final class DependencyComputer implements INodeVisitor {
    public static List<Node> computeDependencies(final Node node) {
        final DependencyComputer d = new DependencyComputer(node.scope.depth);
        node.accept(d);
        Collections.reverse(d.dependencies);
        return d.dependencies;
    }

    private List<Node> dependencies;
    private int maxScope;

    private DependencyComputer(final int maxScope) {
        this.dependencies = new ArrayList<>();
        this.maxScope = maxScope;
    }

    private boolean isDependency(final Node node) {
        if(node.scope.depth > this.maxScope) {
            return false;
        }
        return node instanceof ConstDeclST || node instanceof AssignDeclST || node instanceof VariableDeclST;
    }

    @Override
    public void visitFile(final FileST st) { 
        st.getStatements().forEach(stmt -> stmt.accept(this));
    }

    @Override
    public void visitIdent(final IdentST st) { 
        final Node value = st.scope.lookup(st.ident);
        final Node declarer = st.scope.findDeclarer(value);
        if(declarer != null && !this.dependencies.contains(declarer) && this.isDependency(declarer)) {
            this.dependencies.add(declarer);
            declarer.accept(this);
        }
    }

    @Override
    public void visitString(final StringST st) { 

    }
    
    @Override
    public void visitNil(final NilST st) {

    }

    @Override
    public void visitBool(final BoolST st) {

    }

    @Override
    public void visitNumber(final NumberST st) {
        
    }

    @Override
    public void visitNewArray(final NewArrayST st) {
        st.type.accept(this);   
    }

    @Override
    public void visitNewMap(final NewMapST st) {
        st.type.accept(this);
    }

    @Override
    public void visitNewStruct(final NewStructST st) {
        st.type.accept(this);   
    }

    @Override
    public void visitParen(final ParenST st) {
        st.value.accept(this);
    }

    @Override
    public void visitUnary(final UnaryST st) { 
        if(st.op == UnaryOP.RUN) return;
        st.value.accept(this);
    }

    @Override
    public void visitBinary(final BinaryST st) { 
        st.left.accept(this);
        st.right.accept(this);
    }

    @Override
    public void visitAccess(final AccessST st) {
        st.value.accept(this);
        st.index.accept(this);   
    }

    @Override
    public void visitDot(final DotST st) {
        st.value.accept(this);   
    }

    @Override
    public void visitCast(final CastST st) {
        st.value.accept(this);
    }

    @Override
    public void visitCall(final CallST st) { 
        st.function.accept(this);

        for(final ExpressionST arg : st.arguments) {
            arg.accept(this);
        }
    }

    @Override
    public void visitAssign(final AssignST st) {
        st.left.accept(this);
        st.right.accept(this);   
    }

    @Override 
    public void visitInc(final IncST st) {
        st.expr.accept(this);
    }

    @Override 
    public void visitDec(final DecST st) {
        st.expr.accept(this);
    }

    @Override
    public void visitSwitch(final SwitchST st) {
        st.value.accept(this);

        for(final Map.Entry<ExpressionST, StatementST> entry : st.getCases().entrySet()) {
            entry.getKey().accept(this);
            entry.getValue().accept(this);
        }    
    }

    @Override
    public void visitImport(final ImportST st) {
        
    }

    @Override 
    public void visitBreak(final BreakST st) {

    }

    @Override 
    public void visitContinue(final ContinueST st) {

    }

    @Override
    public void visitWhile(final WhileST st) {
        st.condition.accept(this);
        st.code.accept(this);
    }

    @Override
    public void visitDo(final DoST st) {
        st.condition.accept(this);
        st.code.accept(this);
    }

    @Override
    public void visitFor(final ForST st) {
        if(st.init != null) {
            st.init.accept(this);
        }
        
        if(st.condition != null) {
            st.condition.accept(this);
        }

        if(st.afterthought != null) {
            st.afterthought.accept(this);
        }
        
        st.code.accept(this);
    }

    @Override
    public void visitIf(final IfST st) {
        st.condition.accept(this);
        st.success.accept(this);

        if(st.failure != null) {
            st.failure.accept(this);
        }
    }

    @Override
    public void visitReturn(final ReturnST st) {
        st.value.accept(this);
    }

    @Override
    public void visitDefer(final DeferST st) {
        st.code.accept(this);
    }

    @Override
    public void visitParameter(final ParameterST st) { 

    }

    @Override
    public void visitFunctionType(final FunctionTypeST st) { 

    }

    @Override
    public void visitBasicType(final BasicTypeST st) { 
        st.name.accept(this);
    }

    @Override
    public void visitArrayType(final ArrayTypeST st) {
        st.type.accept(this);
    }

    @Override
    public void visitMapType(final MapTypeST st) {
        st.valueType.accept(this);
        st.keyType.accept(this);
    }

    @Override
    public void visitStructType(final StructTypeST st) {
        for(final Map.Entry<String, TypeST> field : st.getFields().entrySet()) {
            field.getValue().accept(this);
        }
    }

    @Override
    public void visitBlock(final BlockST st) { 
        st.getStatements().forEach(stmt -> stmt.accept(this));
    }

    @Override
    public void visitFunction(final FunctionST st) { 
        st.type.accept(this);
        
        if(!st.type.foreign) {
            st.code.accept(this);
        }
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatementST st) { 
        st.expr.accept(this);
    }

    @Override
    public void visitConstDecl(final ConstDeclST st) { 
        st.value.accept(this);
    }

    @Override
    public void visitAssignDecl(final AssignDeclST st) {
        st.value.accept(this);
    }

    @Override 
    public void visitVariableDecl(final VariableDeclST st) {
        st.name.accept(this);
        st.type.accept(this);
        
        if(st.defaultValue != null) {
            st.defaultValue.accept(this);
        }
    }

    @Override
    public void visitEmpty(final EmptyST st) {
        
    }
}