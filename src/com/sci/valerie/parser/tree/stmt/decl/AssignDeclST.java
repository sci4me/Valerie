package com.sci.valerie.parser.tree.stmt.decl;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.atom.*;

public final class AssignDeclST extends DeclarationST {
    public final IdentST name;
    public final ExpressionST value;

    public AssignDeclST(final Scope scope, final IdentST name, final ExpressionST value) {
        super(scope);
        this.name = name;
        this.value = value;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitAssignDecl(this);
    }
}