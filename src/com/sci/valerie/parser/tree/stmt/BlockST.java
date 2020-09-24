package com.sci.valerie.parser.tree.stmt;

import java.util.*;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;

public final class BlockST extends StatementST {
    private final List<StatementST> statements;

    public BlockST(final Scope scope) {
        super(scope);
        this.statements = new ArrayList<>();
    }

    public void addStatement(final StatementST statement) {
        this.statements.add(statement);
    }

    public List<StatementST> getStatements() {
        return this.statements;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitBlock(this);
    }
}