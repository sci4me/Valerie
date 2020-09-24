package com.sci.valerie.parser.tree.stmt;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.tree.*;

public abstract class StatementST extends Node {
    public StatementST(final Scope scope) {
        super(scope);
    }
}