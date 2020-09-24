package com.sci.valerie.parser.tree.expr.type;

import java.util.*;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.expr.atom.*;

public final class StructTypeST extends TypeST {
    private static int currentId = 0;

    public final int id;

    private Map<String, TypeST> fields; 

    public StructTypeST(final Scope scope) {
        super(scope);
        this.id = StructTypeST.currentId++;
        this.fields = new HashMap<>();
    }

    public void addField(final String name, final TypeST type) {
        this.fields.put(name, type);
    }

    public TypeST getFieldType(final String name) {
        return this.fields.get(name);
    }

    public Map<String, TypeST> getFields() {
        return this.fields;
    }

    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitStructType(this);
    }
}