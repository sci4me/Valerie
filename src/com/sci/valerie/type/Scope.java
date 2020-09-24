package com.sci.valerie.type;

import java.util.*;

import com.sci.valerie.parser.tree.*;

public final class Scope {
    public final Scope parent;  
    public final int depth;

    public final int functionDepth;
    public final int loopDepth;

    private Map<String, Node> bindings;
    private Map<Node, Node> declarers;
    private Map<String, Boolean> isConstant;

    public Scope(final int functionDepth, final int loopDepth) {   
        this(null, functionDepth, loopDepth);
    }

    public Scope(final Scope parent, final int functionDepth, final int loopDepth) {
        this.parent = parent;
        
        if(this.parent == null) {
            this.depth = 1;
        } else {
            this.depth = this.parent.depth + 1;
        }

        this.functionDepth = functionDepth;
        this.loopDepth = loopDepth;

        this.bindings = new HashMap<>();
        this.declarers = new HashMap<>();
        this.isConstant = new HashMap<>();
    }

    public Map<String, Node> getBindings() {
        return this.bindings;
    }

    public void bind(final String name, final Node node, final Node declarer, final boolean constant) {
        if(this.hasBinding(name, false)) {
            throw new RuntimeException("Cannot rebind value: " + name);
        }

        this.bindings.put(name, node);
        this.declarers.put(node, declarer);
        this.isConstant.put(name, constant);
    }

    public boolean hasBinding(final String name) {
        return this.hasBinding(name, true);
    }

    public boolean hasBinding(final String name, final boolean checkParent) {
        if(this.bindings.containsKey(name)) {
            return true;
        }

        if(checkParent) {
            if(this.parent != null) {
                return this.parent.hasBinding(name);
            }
        }

        return false;
    }

    public Node lookup(final String name) {
        if(this.bindings.containsKey(name)) {
            return this.bindings.get(name);
        }

        if(this.parent != null) {
            return this.parent.lookup(name);
        }

        return null;
    }

    public Node findDeclarer(final Node node) {
        if(this.declarers.containsKey(node)) {
            return this.declarers.get(node);
        }

        if(this.parent != null) {
            return this.parent.findDeclarer(node);
        }

        return null;
    }

    public boolean isConstant(final String name) {
        if(!this.isConstant.containsKey(name)) {
            if(this.parent != null) {
                return this.parent.isConstant(name);
            } else {
                return false;
            }
        }
        return this.isConstant.get(name);
    }
}