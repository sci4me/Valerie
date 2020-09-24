package com.sci.valerie.parser.util;

import com.sci.valerie.parser.tree.*;

public abstract class LinearNodeVisitor extends NodeVisitorAdapter {
    private final Node[] nodes;
    private int index;

    public LinearNodeVisitor(final Node[] nodes) {
        this.nodes = nodes;
    }

    public final void visit() {
        if(this.index >= this.nodes.length) {
            throw new IndexOutOfBoundsException();
        }

        this.nodes[this.index].accept(this);
        this.index++;
    }

    public final void prev() {
        this.index--;
    }

    public final int index() {
        return this.index;
    }

    public final int length() {
        return this.nodes.length;
    }

    public final boolean hasNext() {
        return this.index < this.nodes.length;
    }

    public Node[] getNodes() {
        return this.nodes;
    }
}