package com.sci.valerie.parser.tree.expr.atom;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class NilST extends ExpressionST {
    public NilST(final Scope scope) {
        super(scope);
    }

    @Override 
    public void accept(final INodeVisitor visitor) {
        visitor.visitNil(this);
    }
}