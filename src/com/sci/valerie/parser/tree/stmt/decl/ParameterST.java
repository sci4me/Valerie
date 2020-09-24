package com.sci.valerie.parser.tree.stmt.decl;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.type.*;
import com.sci.valerie.parser.tree.expr.atom.*;

public final class ParameterST extends DeclarationST {
    public final IdentST name;
    public final TypeST type;

    public ParameterST(final Scope scope, final IdentST name, final TypeST type) {
        super(scope); 
        this.name = name;
        this.type = type;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitParameter(this);
    }
}