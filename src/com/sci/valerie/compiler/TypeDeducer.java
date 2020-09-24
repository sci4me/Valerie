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

public final class TypeDeducer extends LinearNodeWorker implements Worker {
    public TypeDeducer(final CompileManager compiler, final Node[] nodes) {
        super(compiler, nodes);
    }   

    @Override 
    public void visitIdent(final IdentST st) { 
        final int type = Types.getType(st.ident);
        if(type == Types.UNKNOWN) {
            if(st.scope.hasBinding(st.ident)) {
                Node boundValue = st.scope.lookup(st.ident);

                // TODO(sci4me): hacky code galore
                if(boundValue instanceof FunctionST) {
                    boundValue = ((FunctionST) boundValue).type;
                } else if(boundValue instanceof VariableDeclST) {
                    boundValue = ((VariableDeclST) boundValue).type;
                }

                if(boundValue.type() == Types.UNKNOWN) {
                    this.waitOn(boundValue, WaitType.TYPE_DEDUCE);
                } else {
                    st.setType(boundValue.type());
                }
            } else {
                this.waitOn(st, WaitType.NAME_BIND);
            }
        } else {
            st.setType(type);
            this.compiler.notify(st, WaitType.TYPE_DEDUCE);
        }
    }

    @Override 
    public void visitString(final StringST st) { 
        st.setType(Types.STRING);
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitNil(final NilST st) {
        st.setType(Types.ANY);
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitBool(final BoolST st) {
        st.setType(Types.BOOL);
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitNumber(final NumberST st) {
        if(st.number.contains(".")) {
            st.setType(Types.FLOAT);
        } else {
            st.setType(Types.INT);
        }

        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitNewArray(final NewArrayST st) {
        if(st.type.type() == Types.UNKNOWN) {
            this.waitOn(st.type, WaitType.TYPE_DEDUCE);
            return;
        }   
        
        st.setType(st.type.type());
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitNewMap(final NewMapST st) {
        if(st.type.type() == Types.UNKNOWN) {
            this.waitOn(st.type, WaitType.TYPE_DEDUCE);
            return;
        }

        st.setType(st.type.type());
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitNewStruct(final NewStructST st) {
        if(st.type.type() == Types.UNKNOWN) {
            this.waitOn(st.type, WaitType.TYPE_DEDUCE);
            return;
        }

        final TypeST type = Types.getTypeST(st.type.type());

        if(type instanceof StructTypeST) {
            st.setType(type.type());
            this.compiler.notify(st, WaitType.TYPE_DEDUCE);
        } else {
            throw new RuntimeException("Attempt to initialize struct from something that is not a struct");
        }
    }

    @Override
    public void visitParen(final ParenST st) {
        st.setType(st.value.type());
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
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
                st.setType(type);
                break;
            case NOT:
                st.setType(Types.BOOL);
                break;
            case BIT_NOT:
                st.setType(Types.INT);
                break;
            case RUN:
                st.setType(type);
                break;
            case LENGTH:
                st.setType(Types.INT);
                break;
        }

        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override 
    public void visitBinary(final BinaryST st) { 
        switch(st.op) {
            case EQ:
            case NE:
            case LT:
            case GT:
            case LTE:
            case GTE:
                st.setType(Types.BOOL);
                break;
            case CONCAT:
                st.setType(Types.STRING);
                break;
            case ADD:
            case SUB:
            case MUL:
            case DIV:
                final int leftType = st.left.type();
                final int rightType = st.right.type();

                if(leftType == Types.UNKNOWN) {
                    this.waitOn(st.left, WaitType.TYPE_DEDUCE);
                    return;
                }

                if(rightType == Types.UNKNOWN) {
                    this.waitOn(st.right, WaitType.TYPE_DEDUCE);
                    return;
                }

                int type = Types.INT;

                if(leftType == Types.FLOAT || rightType == Types.FLOAT) {
                    type = Types.FLOAT;
                }

                st.setType(type);
                break;
            case MOD:
            case POW:
            case BIT_LSH:
            case BIT_RSH:
            case BIT_ARSH:
            case BIT_AND:
            case BIT_OR:
            case BIT_XOR:
                st.setType(Types.INT);
                break;
            case AND:
            case OR:
                st.setType(Types.BOOL);
                break;
        }

        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitAccess(final AccessST st) {
        final int valueType = st.value.type();
        if(valueType == Types.UNKNOWN) {
            this.waitOn(st.value, WaitType.TYPE_DEDUCE);
            return;
        }
        
        if(valueType == Types.STRING) {
            st.setType(Types.STRING);
            this.compiler.notify(st, WaitType.TYPE_DEDUCE);
            return;
        }

        final TypeST type = Types.getTypeST(valueType);

        if(type instanceof ArrayTypeST) {
            st.setType(((ArrayTypeST) type).type.type());
        } else if(type instanceof MapTypeST) {
            st.setType(((MapTypeST) type).valueType.type());
        } else {
            throw new RuntimeException("Attempt to index something that is not an array or map; got " + Types.getName(valueType));
        }

        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitDot(final DotST st) {
        final int valueType = st.value.type();
        if(valueType == Types.UNKNOWN) {
            this.waitOn(st.value, WaitType.TYPE_DEDUCE);
            return;
        }   

        final TypeST type = Types.getTypeST(valueType);

        if(type instanceof StructTypeST) {
            final StructTypeST struct = (StructTypeST) type;

            final TypeST structValueType = struct.getFieldType(st.key);
            if(structValueType.type() == Types.UNKNOWN) {
                this.waitOn(structValueType, WaitType.TYPE_DEDUCE);
                return;
            }

            st.setType(structValueType.type());
            this.compiler.notify(st, WaitType.TYPE_DEDUCE);
        } else {
            throw new RuntimeException("Dot operator only works on structs; got " + Types.getName(valueType));
        }
    }

    @Override
    public void visitCast(final CastST st) {
        st.setType(st.type.type());
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
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
        
        final FunctionTypeST functionTypeST = (FunctionTypeST) typeST;

        if(functionTypeST.returnType == null) {
            st.setType(Types.UNKNOWN);
        } else {
            final int functionReturnType = functionTypeST.returnType.type();
            if(functionReturnType == Types.UNKNOWN) {
                this.waitOn(functionTypeST.returnType, WaitType.TYPE_DEDUCE);
                return;
            }
            st.setType(functionReturnType);
        }

        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override 
    public void visitParameter(final ParameterST st) {
        st.setType(st.type.type());
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override 
    public void visitFunctionType(final FunctionTypeST st) { 
        final StringBuilder sb = new StringBuilder();

        sb.append("function(");
        for(int i = 0; i < st.parameters.length; i++) {
            final ParameterST param = st.parameters[i];

            if(param.type() == Types.UNKNOWN) {
                throw new RuntimeException();
            }

            sb.append(Types.getName(param.type()));

            if(i < st.parameters.length - 1) {
                sb.append(",");
            }
        }
        sb.append(")->");

        if(st.returnType != null) {
            sb.append(Types.getName(st.returnType.type()));
        } else {
            sb.append("void");
        }

        final String typeName = sb.toString();

        int type = Types.getType(typeName);
        if(type == Types.UNKNOWN) {
            type = Types.createType(typeName, st);
        }

        st.setType(type);
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override 
    public void visitBasicType(final BasicTypeST st) { 
        if(st.name.type() == Types.UNKNOWN) {
            this.waitOn(st.name, WaitType.TYPE_DEDUCE);
            return;
        }
    
        st.setType(st.name.type());
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitArrayType(final ArrayTypeST st) {
        if(st.type.type() == Types.UNKNOWN) {
            this.waitOn(st.type, WaitType.TYPE_DEDUCE);
            return;
        }
        
        final String typeName = "[]" + Types.getName(st.type.type()); 
        
        int type = Types.getType(typeName);
        if(type == Types.UNKNOWN) {
            type = Types.createType(typeName, st);
        }

        st.setType(type);
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitMapType(final MapTypeST st) {
        if(st.keyType.type() == Types.UNKNOWN) {
            this.waitOn(st.keyType, WaitType.TYPE_DEDUCE);
            return;
        }

        if(st.valueType.type() == Types.UNKNOWN) {
            this.waitOn(st.valueType, WaitType.TYPE_DEDUCE);
            return;
        }
        
        final String typeName = Types.getName(st.valueType.type()) + "[" + Types.getName(st.keyType.type()) + "]"; 
        
        int type = Types.getType(typeName);
        if(type == Types.UNKNOWN) {
            type = Types.createType(typeName, st);
        }

        st.setType(type);
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitStructType(final StructTypeST st) {
        final StringBuilder sb = new StringBuilder();

        sb.append("struct{");

        int i = 0;
        for(final Map.Entry<String, TypeST> field : st.getFields().entrySet()) {
            sb.append(field.getKey());
            sb.append(":");

            final int fieldType = field.getValue().type();
            if(fieldType == Types.UNKNOWN) {
                this.waitOn(field.getValue(), WaitType.TYPE_DEDUCE);
                return;
            }

            sb.append(Types.getName(fieldType));

            i++;
            if(i < st.getFields().size()) {
                sb.append(",");
            }
        }

        sb.append("}");

        final String typeName = sb.toString();
        int type = Types.getType(typeName);
        if(type == Types.UNKNOWN) {
            type = Types.createType(typeName, st);
        }

        st.setType(type);
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override 
    public void visitFunction(final FunctionST st) { 
        st.setType(st.type.type());
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override
    public void visitVariableDecl(final VariableDeclST st) {
        st.setType(st.type.type());
        this.compiler.notify(st, WaitType.TYPE_DEDUCE);
    }

    @Override 
    public void finish() {                
        this.compiler.queueWorker(new TypeChecker(this.compiler, this.getNodes()));
    }
}