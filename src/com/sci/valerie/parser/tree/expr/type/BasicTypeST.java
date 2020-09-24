package com.sci.valerie.parser.tree.expr.type;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.expr.atom.*;

public final class BasicTypeST extends TypeST {
    public final IdentST name;

    public BasicTypeST(final Scope scope, final IdentST name) {
        super(scope);
        this.name = name;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitBasicType(this);
    }
}