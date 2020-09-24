package com.sci.valerie.parser.tree.stmt.decl;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.atom.*;
import com.sci.valerie.parser.tree.expr.type.*;

public final class VariableDeclST extends DeclarationST {
    public final IdentST name;
    public final TypeST type;
    public final ExpressionST defaultValue;

    public VariableDeclST(final Scope scope, final IdentST name, final TypeST type, final ExpressionST defaultValue) {
        super(scope);
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitVariableDecl(this);
    }
}