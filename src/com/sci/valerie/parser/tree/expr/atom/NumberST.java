package com.sci.valerie.parser.tree.expr.atom;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class NumberST extends ExpressionST {
    public final String number;

    public NumberST(final Scope scope, final String number) {
        super(scope);
        this.number = number;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitNumber(this);
    }
}