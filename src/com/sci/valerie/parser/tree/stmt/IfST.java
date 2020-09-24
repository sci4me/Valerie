package com.sci.valerie.parser.tree.stmt;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class IfST extends StatementST {
    public final ExpressionST condition;
    public final StatementST success;
    public final StatementST failure;

    public IfST(final Scope scope, final ExpressionST condition, final StatementST success) {
        this(scope, condition, success, null);    
    }

    public IfST(final Scope scope, final ExpressionST condition, final StatementST success, final StatementST failure) {
        super(scope);
        this.condition = condition;
        this.success = success;
        this.failure = failure;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitIf(this);
    }
}