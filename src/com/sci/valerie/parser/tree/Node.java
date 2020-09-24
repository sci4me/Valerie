package com.sci.valerie.parser.tree;

import java.util.*;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.util.*;

public abstract class Node implements INodeVisitable {
    public final Scope scope;

    private int deducedType;

    private Map<String, String> code;

    public Node(final Scope scope) {
        this.scope = scope;
        this.deducedType = Types.UNKNOWN;
        this.code = new HashMap<>();
    }

    public int type() {
        return this.deducedType;
    }

    public void setType(final int type) {
        this.deducedType = type;
    }

    public String code(final String key) {
        return this.code.get(key);
    }

    public void setCode(final String key, final String code) {
        this.code.put(key, code);
    }
}