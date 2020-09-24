package com.sci.valerie.parser.tree.stmt;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class DeferST extends StatementST {
    public final StatementST code;

    public DeferST(final Scope scope, final StatementST code) {
        super(scope);
        this.code = code;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitDefer(this);
    }
}