package com.sci.valerie.parser.tree.expr.atom;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class IdentST extends ExpressionST {
    public final String ident;

    public IdentST(final Scope scope, final String ident) {
        super(scope);
        this.ident = ident;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitIdent(this);
    }

    @Override
    public String toString() {
        return "IDENT(" + this.ident + ")";
    }
}