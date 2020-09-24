package com.sci.valerie.parser.util;

public interface INodeVisitable {
    void accept(final INodeVisitor visitor);
}