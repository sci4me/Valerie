package com.sci.valerie.parser.tree.expr;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;

public final class UnaryST extends ExpressionST {
    public final UnaryOP op;
    public final ExpressionST value;

    public UnaryST(final Scope scope, final UnaryOP op, final ExpressionST value) {
        super(scope);
        this.op = op;
        this.value = value;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitUnary(this);
    }
}