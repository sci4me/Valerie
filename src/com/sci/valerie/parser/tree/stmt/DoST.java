package com.sci.valerie.parser.tree.stmt;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class DoST extends StatementST {
    public final FunctionST function;
    public final ExpressionST condition;
    public final StatementST code;

    public DoST(final Scope scope, final FunctionST function, final ExpressionST condition, final StatementST code) {
        super(scope);
        this.function = function;
        this.condition = condition;
        this.code = code;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitDo(this);
    }
}