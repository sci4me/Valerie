package com.sci.valerie.compiler;

import java.util.*;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.stmt.*;
import com.sci.valerie.parser.tree.stmt.decl.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.atom.*;
import com.sci.valerie.parser.tree.expr.type.*;

import static com.sci.valerie.util.Asserts.*;

public final class TypeChecker extends LinearNodeWorker implements Worker {
    public TypeChecker(final CompileManager compiler, final Node[] nodes) {
        super(compiler, nodes);
    }   

    private void checkIsNotConstant(final IdentST ident) {
        if(ident.scope.isConstant(ident.ident)) {
            throw new RuntimeException("Attempt to modify constant " + ident.ident);
        }
    }

    @Override 
    public void visitUnary(final UnaryST st) { 
        final int type = st.value.type();

        if(type == Types.UNKNOWN) {
            this.waitOn(st.value, WaitType.TYPE_DEDUCE);
            return;
        }

        switch(st.op) {
            case NEG:
                assertIs("Unary - only works on ints or floats; got " + Types.getName(type), type, Types.INT, Types.FLOAT);
                break;
            case NOT:
                assertIs("Unary ! only works on bools; got " + Types.getName(type), type, Types.BOOL);
                break;
            case BIT_NOT:
                assertIs("Unary ~ only works on ints; got " + Types.getName(type), type, Types.INT, Types.FLOAT);
                break;
            case RUN:
                // TODO(sci4me)
                break;
            case LENGTH:
                // TODO(sci4me): check that this is an array
                break;
        }
    }

    @Override 
    public void visitBinary(final BinaryST st) { 
        final int left = st.left.type();
        final int right = st.right.type();

        if(left == Types.UNKNOWN) {
            this.waitOn(st.left, WaitType.TYPE_DEDUCE);
            return;
        }

        if(right == Types.UNKNOWN) {
            this.waitOn(st.right, WaitType.TYPE_DEDUCE);
            return;
        }

        switch(st.op) {
            case EQ:
            case NE:
                if(left == Types.ANY || right == Types.ANY) {
                    break;
                }
            case LT:
            case GT:
            case LTE:
            case GTE:
                assertIs("Type mismatch in binary expression; got " + Types.getName(left) + " and " + Types.getName(right), left, right);
                break;
            case CONCAT:
                break;
            case ADD:
            case SUB:
            case MUL:
            case DIV:
                assertIs("Binary arithmetic operator expects an int or a float; got " + Types.getName(left), left, Types.INT, Types.FLOAT);
                assertIs("Binary arithmetic operator expects an int or a float; got " + Types.getName(right), right, Types.INT, Types.FLOAT);
                break;
            case MOD:
            case POW:
                assertIs("Binary arithmetic operator expects an int; got " + Types.getName(left), left, Types.INT);
                assertIs("Binary arithmetic operator expects an int; got " + Types.getName(right), right, Types.INT);
                break;
            case BIT_LSH:
            case BIT_RSH:
            case BIT_ARSH:
            case BIT_AND:
            case BIT_OR:
            case BIT_XOR:
                assertIs("Binary bitwise operator expects an int; got " + Types.getName(left), left, Types.INT);
                assertIs("Binary bitwise operator expects an int; got " + Types.getName(right), right, Types.INT);
                break;
            case AND:
            case OR:
                assertIs("Binary boolean operator expects an bool; got " + Types.getName(left), left, Types.BOOL);
                assertIs("Binary boolean operator expects an bool; got " + Types.getName(right), right, Types.BOOL);
                break;
        }
    }

    @Override
    public void visitAccess(final AccessST st) {
        final int valueType = st.value.type();
        if(valueType == Types.UNKNOWN) {
            this.waitOn(st.value, WaitType.TYPE_DEDUCE);
            return;
        }

        final TypeST type = Types.getTypeST(valueType);

        if(type instanceof ArrayTypeST) {
            final int indexType = st.index.type();
            if(indexType == Types.UNKNOWN) {
                this.waitOn(st.index, WaitType.TYPE_DEDUCE);
                return;
            }   

            assertIs("Array index must be of type int; got " + Types.getName(indexType), indexType, Types.INT);
        }
    }

    @Override 
    public void visitCall(final CallST st) { 
        final int functionType = st.function.type();
        if(functionType == Types.UNKNOWN) {
            this.waitOn(st.function, WaitType.TYPE_DEDUCE);
            return;
        }

        final TypeST typeST = Types.getTypeST(functionType);
        
        if(!(typeST instanceof FunctionTypeST)) {
            throw new AssertionError();
        }
        
        final FunctionTypeST type = (FunctionTypeST) typeST;

        if(type.type() == Types.UNKNOWN) {
            this.waitOn(st, WaitType.TYPE_DEDUCE);
            return;
        }

        if(st.arguments.length != type.parameters.length) {
            throw new RuntimeException("Incorrect number of arguments in call to " + st.function);
        }

        for(int i = 0; i < st.arguments.length; i++) {
            if(st.arguments[i].type() == Types.UNKNOWN) {
                this.waitOn(st.arguments[i], WaitType.TYPE_DEDUCE);
                return;
            }

            if(!Types.areCompatible(st.arguments[i].type(), type.parameters[i].type())) {
                throw new RuntimeException("Incorrect argument type at argument index " + i + "; got " + Types.getName(st.arguments[i].type()) + " expected " + Types.getName(type.parameters[i].type()));
            }
        }
    }

    @Override
    public void visitAssign(final AssignST st) {
        final int left = st.left.type();
        final int right = st.right.type();

        if(left == Types.UNKNOWN) {
            this.waitOn(st.left, WaitType.TYPE_DEDUCE);
            return;
        }

        if(st.left instanceof IdentST) {
            this.checkIsNotConstant((IdentST) st.left);
        }

        if(Types.areCompatible(left, right)) {
            return;
        }

        if(right == Types.UNKNOWN) {
            this.waitOn(st.right, WaitType.TYPE_DEDUCE);
            return;
        }

        if(st.op == AssignOP.CONCAT_ASSIGN) {
            return;
        }

        assertIs("Type mismatch in assignment; got " + Types.getName(left) + " and " + Types.getName(right), left, right);
    }

    @Override 
    public void visitInc(final IncST st) {
        final int type = st.expr.type();

        if(type == Types.UNKNOWN) {
            this.waitOn(st.expr, WaitType.TYPE_DEDUCE);
            return;
        }

        if(st.expr instanceof IdentST) {
            this.checkIsNotConstant((IdentST) st.expr);
        }

        assertIs("Increment operator expects ints or floats; got " + Types.getName(type), type, Types.INT, Types.FLOAT);
    }

    @Override 
    public void visitDec(final DecST st) {
        final int type = st.expr.type();

        if(type == Types.UNKNOWN) {
            this.waitOn(st.expr, WaitType.TYPE_DEDUCE);
            return;
        }

        if(st.expr instanceof IdentST) {
            this.checkIsNotConstant((IdentST) st.expr);
        }

        assertIs("Decrement operator expects ints or floats; got " + Types.getName(type), type, Types.INT, Types.FLOAT);
    }

    @Override
    public void visitSwitch(final SwitchST st) {
        final int expectedType = st.value.type();
        if(expectedType == Types.UNKNOWN) {
            this.waitOn(st.value, WaitType.TYPE_DEDUCE);
            return;
        }    

        for(final Map.Entry<ExpressionST, StatementST> entry : st.getCases().entrySet()) {
            final int keyType = entry.getKey().type();
            if(keyType == Types.UNKNOWN) {
                this.waitOn(entry.getKey(), WaitType.TYPE_DEDUCE);
                return;
            }

            if(keyType != expectedType) {
                throw new RuntimeException("Type mismatch in case statement; got " + Types.getName(keyType) + " expected " + Types.getName(expectedType));
            }
        }
    }

    @Override
    public void visitWhile(final WhileST st) {
        final int type = st.condition.type();
        
        if(type == Types.UNKNOWN) {
            this.waitOn(st.condition, WaitType.TYPE_DEDUCE);
            return;
        }   

        assertIs("While loop condition must be of type bool; got " + Types.getName(type), type, Types.BOOL);
    }

    @Override
    public void visitDo(final DoST st) {
        final int type = st.condition.type();
        
        if(type == Types.UNKNOWN) {
            this.waitOn(st.condition, WaitType.TYPE_DEDUCE);
            return;
        }   

        assertIs("Do loop condition must be of type bool; got " + Types.getName(type), type, Types.BOOL);
    }

    @Override
    public void visitFor(final ForST st) {
        if(st.condition == null) {
            return;
        }

        final int type = st.condition.type();
        
        if(type == Types.UNKNOWN) {
            this.waitOn(st.condition, WaitType.TYPE_DEDUCE);
            return;
        }   

        assertIs("For loop condition must be of type bool; got " + Types.getName(type), type, Types.BOOL);
    }

    @Override
    public void visitIf(final IfST st) {
        final int type = st.condition.type(); 

        if(type == Types.UNKNOWN) {
            this.waitOn(st.condition, WaitType.TYPE_DEDUCE);
            return;
        }
        
        assertIs("If statement condition must be of type bool; got " + Types.getName(type), type, Types.BOOL);   
    }

    @Override
    public void visitReturn(final ReturnST st) {
        if(st.function.type.returnType == null) {
            if(st.value == null) {
                return;       
            } else {
                throw new RuntimeException("Attempt to return value from function with no return type");
            }
        }

        final int type = st.value.type();

        if(type == Types.UNKNOWN) {
            this.waitOn(st.value, WaitType.TYPE_DEDUCE);
            return;
        }

        final int expected = st.function.type.returnType.type();

        if(expected == Types.UNKNOWN) {
            this.waitOn(st.function.type.returnType, WaitType.TYPE_DEDUCE);
            return;
        }

        if(st.value instanceof NilST) {
            assertIsNot("Return type may not be nil", type, Types.INT, Types.FLOAT, Types.BOOL);
            return;
        }

        assertIs("Return type does not match that of function; expected " + Types.getName(expected) + ", got " + Types.getName(type), type, expected);
    }

    @Override
    public void visitVariableDecl(final VariableDeclST st) {
        if(st.defaultValue == null) {
            return;
        }

        final int expected = st.type.type();
        if(expected == Types.UNKNOWN) {
            this.waitOn(st.type, WaitType.TYPE_DEDUCE);
            return;
        }
        
        final int type = st.defaultValue.type();
        if(type == Types.UNKNOWN) {
            this.waitOn(st.defaultValue, WaitType.TYPE_DEDUCE);
            return;
        }

        if(st.defaultValue instanceof NilST) {
            assertIsNot("Variable type may not be nil", expected, Types.INT, Types.FLOAT, Types.BOOL);
            return;
        }

        assertIs("Type mismatch is declaration assignment; got " + Types.getName(type) + ", expected " + Types.getName(expected), type, expected);
    }

    @Override 
    public void finish() {
        this.compiler.queueWorker(new CodeGenerator(this.compiler, this.getNodes()));
    }
}