package com.sci.valerie.compiler;

import com.sci.valerie.parser.tree.*;

public interface Worker {
    public abstract void notify(final Node node, final WaitType waitType);

    public abstract WorkStatus work();

    public abstract void finish();
}