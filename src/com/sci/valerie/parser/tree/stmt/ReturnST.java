package com.sci.valerie.parser.tree.stmt;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class ReturnST extends StatementST {
    public final ExpressionST value;
    public final FunctionST function;

    public ReturnST(final Scope scope, final ExpressionST value, final FunctionST function) {
        super(scope);
        this.value = value;
        this.function = function;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitReturn(this);
    }
}