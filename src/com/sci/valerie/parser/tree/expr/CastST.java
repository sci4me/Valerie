package com.sci.valerie.parser.tree.expr;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.expr.type.*;

public final class CastST extends ExpressionST {
    public final TypeST type;
    public final ExpressionST value;

    public CastST(final Scope scope, final TypeST type, final ExpressionST value) {
        super(scope);
        this.type = type;
        this.value = value;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitCast(this);
    }
}