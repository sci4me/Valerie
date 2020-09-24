package com.sci.valerie.parser.tree.stmt;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class IncST extends StatementST {
    public final ExpressionST expr;

    public IncST(final Scope scope, final ExpressionST expr) {
        super(scope);
        this.expr = expr;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitInc(this);
    }
}