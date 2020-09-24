package com.sci.valerie.parser.tree;

import java.io.*;
import java.util.*;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.stmt.*;

public final class FileST extends Node {
    public final File file;
    private List<StatementST> statements;

    public FileST(final Scope scope, final File file) {
        super(scope);
        this.file = file;
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
        visitor.visitFile(this);
    }
}