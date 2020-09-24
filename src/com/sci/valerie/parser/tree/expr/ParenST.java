package com.sci.valerie.parser.tree.expr;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;

public final class ParenST extends ExpressionST {
    public final ExpressionST value;

    public ParenST(final Scope scope, final ExpressionST value) {
        super(scope);
        this.value = value;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitParen(this);
    }
}