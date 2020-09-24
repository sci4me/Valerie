package com.sci.valerie.compiler;

import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.stmt.*;
import com.sci.valerie.parser.tree.stmt.decl.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.atom.*;
import com.sci.valerie.parser.tree.expr.type.*;

import com.sci.valerie.parser.util.*;

public abstract class LinearNodeWorker extends LinearNodeVisitor implements Worker {
    public final CompileManager compiler;

    private Node waitingOn;
    private WaitType waitType;

    public LinearNodeWorker(final CompileManager compiler, final Node[] nodes) {
        super(nodes);
        this.compiler = compiler;
    }

    public void waitOn(final Node node, final WaitType waitType) {
        if(this.waitingOn != null) {
            throw new RuntimeException("Already waiting!");
        }

        this.waitingOn = node;
        this.waitType = waitType;
    }

    @Override 
    public void notify(final Node node, final WaitType waitType) {
        this.waitingOn = null;
    }

    @Override 
    public WorkStatus work() {
        while(this.hasNext()) {
            this.visit();

            if(this.waitingOn != null) {
                this.prev();
                break;
            }
        }

        return new WorkStatus(!this.hasNext(), this.waitingOn, this.waitType);
    }

    @Override 
    public void finish() {

    }
}