package com.sci.valerie.parser.tree.stmt;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class ForST extends StatementST {
    public final FunctionST function;
    public final StatementST init;
    public final ExpressionST condition;
    public final StatementST afterthought;
    public final StatementST code;

    public ForST(final Scope scope, final FunctionST function, final StatementST init, final ExpressionST condition, final StatementST afterthought, final StatementST code) {
        super(scope);
        this.function = function;
        this.init = init;
        this.condition = condition;
        this.afterthought = afterthought;
        this.code = code;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitFor(this);
    }
}