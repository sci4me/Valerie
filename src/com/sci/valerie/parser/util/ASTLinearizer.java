package com.sci.valerie.parser.util;

import java.util.*;
import java.util.function.*;

import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.stmt.*;
import com.sci.valerie.parser.tree.stmt.decl.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.atom.*;
import com.sci.valerie.parser.tree.expr.type.*;

public final class ASTLinearizer {
    public static Node[] linearizeAsArray(final Node node) {
        return ASTLinearizer.linearizeAsArray(node, n -> true);
    }

    public static Node[] linearizeAsArray(final Node node, final Predicate<Node> filter) {
        final List<Node> nodes = ASTLinearizer.linearize(node, filter);
        return nodes.toArray(new Node[nodes.size()]);
    }

    public static List<Node> linearize(final Node node) {
        return ASTLinearizer.linearize(node, n -> true);
    }

    public static List<Node> linearize(final Node node, final Predicate<Node> filter) {
        final List<Node> nodes = new ArrayList<>();

        node.accept(new INodeVisitor() {
            @Override 
            public void visitFile(final FileST st) {
                for(final StatementST statement : st.getStatements()) {
                    statement.accept(this);
                }
                
                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitIdent(final IdentST st) {
                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitString(final StringST st) {
                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitNil(final NilST st) {
                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitBool(final BoolST st) {
                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitNumber(final NumberST st) {
                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitNewArray(final NewArrayST st) {
                st.type.accept(this);
                st.size.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }   
            }

            @Override
            public void visitNewMap(final NewMapST st) {
                st.type.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitNewStruct(final NewStructST st) {
                st.type.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }             
            }

            @Override
            public void visitParen(final ParenST st) {
                st.value.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }    
            }

            @Override 
            public void visitUnary(final UnaryST st) {
                st.value.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitBinary(final BinaryST st) {
                st.left.accept(this);
                st.right.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }
            
            @Override
            public void visitAccess(final AccessST st) {
                st.value.accept(this);
                st.index.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }   
            }

            @Override
            public void visitDot(final DotST st) {
                st.value.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }    
            }

            @Override
            public void visitCast(final CastST st) {
                st.type.accept(this);
                st.value.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }
 
            @Override 
            public void visitCall(final CallST st) {
                for(final ExpressionST arg : st.arguments) {
                    arg.accept(this);
                }

                st.function.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }
            
            @Override
            public void visitAssign(final AssignST st) {
                st.left.accept(this);
                st.right.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }    
            }

            @Override 
            public void visitInc(final IncST st) {
                st.expr.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitDec(final DecST st) {
                st.expr.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitSwitch(final SwitchST st) {
                st.value.accept(this);

                for(final Map.Entry<ExpressionST, StatementST> entry : st.getCases().entrySet()) {
                    entry.getKey().accept(this);
                    entry.getValue().accept(this);
                }

                if(st.getDefaultCase() != null) {
                    st.getDefaultCase().accept(this);
                }

                if(filter.test(st)) {
                    nodes.add(st);
                }   
            }

            @Override
            public void visitImport(final ImportST st) {
                st.name.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }    
            }

            @Override 
            public void visitBreak(final BreakST st) {
                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitContinue(final ContinueST st) {
                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitWhile(final WhileST st) {
                st.condition.accept(this);
                st.code.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }   
            }
            
            @Override
            public void visitDo(final DoST st) {
                st.condition.accept(this);
                st.code.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }    
            }

            @Override
            public void visitFor(final ForST st) {
                st.init.accept(this);

                if(st.condition != null) {
                    st.condition.accept(this);
                }
                
                st.afterthought.accept(this);
                st.code.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }   
            }

            @Override
            public void visitIf(final IfST st) {
                st.condition.accept(this);
                st.success.accept(this);
                    
                if(st.failure != null) {
                    st.failure.accept(this);
                }

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitReturn(final ReturnST st) {
                if(st.value != null) {
                    st.value.accept(this);
                }

                if(filter.test(st)) {
                    nodes.add(st);
                }   
            }

            @Override
            public void visitDefer(final DeferST st) {
                st.code.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitParameter(final ParameterST st) {
                st.type.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitFunctionType(final FunctionTypeST st) {
                for(final ParameterST param : st.parameters) {
                    param.accept(this);
                }

                if(st.returnType != null) {
                    st.returnType.accept(this);
                }

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitArrayType(final ArrayTypeST st) {
                st.type.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }   
            }

            @Override
            public void visitMapType(final MapTypeST st) {
                st.valueType.accept(this);   
                st.keyType.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitBasicType(final BasicTypeST st) {
                st.name.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitStructType(final StructTypeST st) {
                for(final Map.Entry<String, TypeST> field : st.getFields().entrySet()) {
                    field.getValue().accept(this);
                }

                if(filter.test(st)) {
                    nodes.add(st);
                }   
            }

            @Override 
            public void visitBlock(final BlockST st) {
                for(final StatementST statement : st.getStatements()) {
                    statement.accept(this);
                }

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitFunction(final FunctionST st) {
                st.type.accept(this);
                
                if(!st.type.foreign) {
                    st.code.accept(this);
                }

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitExpressionStatement(final ExpressionStatementST st) {
                st.expr.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitConstDecl(final ConstDeclST st) {
                st.value.accept(this);
                st.name.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitAssignDecl(final AssignDeclST st) {
                st.value.accept(this);
                st.name.accept(this);

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override 
            public void visitVariableDecl(final VariableDeclST st) {
                st.type.accept(this);
                st.name.accept(this);

                if(st.defaultValue != null) {
                    st.defaultValue.accept(this);
                }

                if(filter.test(st)) {
                    nodes.add(st);
                }
            }

            @Override
            public void visitEmpty(final EmptyST st) {
                if(filter.test(st)) {
                    nodes.add(st);
                }
            }
        });

        return nodes;
    }
    
    private ASTLinearizer() {

    }
}