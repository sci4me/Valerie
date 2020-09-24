package com.sci.valerie.parser.tree.expr;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.util.*;

public final class BinaryST extends ExpressionST {
    public final BinaryOP op;
    public final ExpressionST left;
    public final ExpressionST right;

    public BinaryST(final Scope scope, final BinaryOP op, final ExpressionST left, final ExpressionST right) {
        super(scope);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitBinary(this);
    }
}