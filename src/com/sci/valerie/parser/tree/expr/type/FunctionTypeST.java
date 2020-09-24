package com.sci.valerie.parser.tree.expr.type;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.stmt.decl.*;

public final class FunctionTypeST extends TypeST {
    public final ParameterST[] parameters;
    public final TypeST returnType;
    public final boolean foreign;

    public FunctionTypeST(final Scope scope, final ParameterST[] parameters, final TypeST returnType, final boolean foreign) {
        super(scope);
        this.parameters = parameters;
        this.returnType = returnType;
        this.foreign = foreign;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitFunctionType(this);
    }
}