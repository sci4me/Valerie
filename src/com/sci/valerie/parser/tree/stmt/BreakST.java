package com.sci.valerie.parser.tree.stmt;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;

public final class BreakST extends StatementST {
    public BreakST(final Scope scope) {
        super(scope);
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitBreak(this);
    }
}