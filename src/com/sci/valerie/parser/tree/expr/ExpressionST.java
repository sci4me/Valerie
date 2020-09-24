package com.sci.valerie.parser.tree.expr;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.tree.*;

public abstract class ExpressionST extends Node {
    public ExpressionST(final Scope scope) {
        super(scope);
    }
}