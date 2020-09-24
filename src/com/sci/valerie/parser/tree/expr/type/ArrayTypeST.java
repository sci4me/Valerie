package com.sci.valerie.parser.tree.expr.type;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.expr.atom.*;

public final class ArrayTypeST extends TypeST {
    public final TypeST type;

    public ArrayTypeST(final Scope scope, final TypeST type) {
        super(scope);
        this.type = type;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitArrayType(this);
    }
}