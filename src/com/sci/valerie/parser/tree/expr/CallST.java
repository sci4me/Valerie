package com.sci.valerie.parser.tree.expr;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.util.*;

public final class CallST extends ExpressionST {
    public final ExpressionST function;
    public final ExpressionST[] arguments;

    public String file = "unknown";
    public String line = "unknown";
    public String name = "unknown";

    public CallST(final Scope scope, final ExpressionST function, final ExpressionST[] arguments) {
        super(scope); 
        this.function = function;
        this.arguments = arguments;
    }   

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitCall(this);
    }
}