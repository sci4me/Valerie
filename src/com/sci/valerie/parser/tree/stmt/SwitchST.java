package com.sci.valerie.parser.tree.stmt;

import java.util.*;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.expr.*;

public final class SwitchST extends StatementST {
    public final ExpressionST value;

    private final Map<ExpressionST, StatementST> cases;
    private StatementST defaultCase;

    public SwitchST(final Scope scope, final ExpressionST value) {
        super(scope);
        this.value = value;
        this.cases = new HashMap<>();
    }

    public void addCase(final ExpressionST key, final StatementST code) {
        this.cases.put(key, code);
    }

    public void setDefaultCase(final StatementST defaultCase) {
        this.defaultCase = defaultCase;
    }

    public StatementST getDefaultCase() {
        return this.defaultCase;
    }

    public Map<ExpressionST, StatementST> getCases() {
        return this.cases;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitSwitch(this);
    }
}