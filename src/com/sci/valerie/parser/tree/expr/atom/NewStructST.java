package com.sci.valerie.parser.tree.expr.atom;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.type.*;

public final class NewStructST extends ExpressionST {
    public final TypeST type;

    public NewStructST(final Scope scope, final TypeST type) {
        super(scope);
        this.type = type;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitNewStruct(this);
    }
}