package com.sci.valerie.parser.tree.expr;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.util.*;

public final class AccessST extends ExpressionST {
    public final ExpressionST value;
    public final ExpressionST index;

    public AccessST(final Scope scope, final ExpressionST value, final ExpressionST index) {
        super(scope); 
        this.value = value;
        this.index = index;
    }   

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitAccess(this);
    }
}