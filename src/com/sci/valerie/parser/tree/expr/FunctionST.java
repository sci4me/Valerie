package com.sci.valerie.parser.tree.expr;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.stmt.*;
import com.sci.valerie.parser.tree.expr.type.*;

public final class FunctionST extends ExpressionST {
    public final FunctionTypeST type;
    public final BlockST code;

    private boolean hasDefer;

    public FunctionST(final Scope scope, final FunctionTypeST type, final BlockST code) {
        super(scope);
        this.type = type;
        this.code = code;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitFunction(this);
    }
    
    public void markHasDefer() {
        this.hasDefer = true;
    }

    public boolean hasDefer() {
        return this.hasDefer;
    }
}