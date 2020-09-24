package com.sci.valerie.compiler;

import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.stmt.*;
import com.sci.valerie.parser.tree.stmt.decl.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.atom.*;
import com.sci.valerie.parser.tree.expr.type.*;

public final class NameBinder extends LinearNodeWorker implements Worker {
    public NameBinder(final CompileManager compiler, final Node[] nodes) {
        super(compiler, nodes);
    }   

    @Override
    public void visitParameter(final ParameterST st) {
        st.scope.bind(st.name.ident, st, st, true);
    }

    @Override
    public void visitConstDecl(final ConstDeclST st) {
        st.scope.bind(st.name.ident, st.value, st, true);
        this.compiler.notify(st.name, WaitType.NAME_BIND);
    }

    @Override
    public void visitAssignDecl(final AssignDeclST st) {
        st.scope.bind(st.name.ident, st.value, st, false);
        this.compiler.notify(st.name, WaitType.NAME_BIND);
    }

    @Override
    public void visitVariableDecl(final VariableDeclST st) {
        st.scope.bind(st.name.ident, st, st, false);
        this.compiler.notify(st.name, WaitType.NAME_BIND);
    }

    @Override 
    public void finish() {
        this.compiler.queueWorker(new TypeDeducer(this.compiler, this.getNodes()));
    }
}