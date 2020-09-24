package com.sci.valerie.parser.tree.stmt;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.atom.*;

public final class ImportST extends StatementST {
    public final StringST name;

    public ImportST(final Scope scope, final StringST name) {
        super(scope);
        this.name = name;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitImport(this);
    }
}