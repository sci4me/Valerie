package com.sci.valerie.parser.tree.expr;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;

public final class DotST extends ExpressionST {
    public final ExpressionST value;
    public final String key;

    public DotST(final Scope scope, final ExpressionST value, final String key) {
        super(scope);
        this.value = value;
        this.key = key;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitDot(this);
    }
}