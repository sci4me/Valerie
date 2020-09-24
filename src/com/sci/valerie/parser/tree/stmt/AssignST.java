package com.sci.valerie.parser.tree.stmt;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.expr.*;

public final class AssignST extends StatementST {
    public final AssignOP op;
    public final ExpressionST left;
    public final ExpressionST right;

    public AssignST(final Scope scope, final AssignOP op, final ExpressionST left, final ExpressionST right) {
        super(scope);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitAssign(this);
    }
}