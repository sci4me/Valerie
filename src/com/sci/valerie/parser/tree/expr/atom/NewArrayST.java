package com.sci.valerie.parser.tree.expr.atom;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.type.*;

public final class NewArrayST extends ExpressionST {
    public final ArrayTypeST type;
    public final ExpressionST size;

    public NewArrayST(final Scope scope, final ArrayTypeST type, final ExpressionST size) {
        super(scope);
        this.type = type;
        this.size = size;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitNewArray(this);
    }
}