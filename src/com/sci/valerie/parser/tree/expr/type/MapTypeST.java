package com.sci.valerie.parser.tree.expr.type;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.expr.atom.*;

public final class MapTypeST extends TypeST {
    public final TypeST keyType;
    public final TypeST valueType;

    public MapTypeST(final Scope scope, final TypeST keyType, final TypeST valueType) {
        super(scope);
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitMapType(this);
    }
}