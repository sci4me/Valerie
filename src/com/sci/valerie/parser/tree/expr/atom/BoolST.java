package com.sci.valerie.parser.tree.expr.atom;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class BoolST extends ExpressionST {
    public final boolean value;

    public BoolST(final Scope scope, final boolean value) {
        super(scope);
        this.value = value;
    }

    @Override 
    public void accept(final INodeVisitor visitor) {
        visitor.visitBool(this);
    }
}