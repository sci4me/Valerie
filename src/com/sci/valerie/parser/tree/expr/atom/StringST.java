package com.sci.valerie.parser.tree.expr.atom;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class StringST extends ExpressionST {
    public final String string;

    public StringST(final Scope scope, final String string) {
        super(scope);
        this.string = string;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitString(this);
    }
}