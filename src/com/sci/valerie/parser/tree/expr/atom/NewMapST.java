package com.sci.valerie.parser.tree.expr.atom;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.type.*;

public final class NewMapST extends ExpressionST {
    public final MapTypeST type;

    public NewMapST(final Scope scope, final MapTypeST type) {
        super(scope);
        this.type = type;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitNewMap(this);
    }
}