package com.sci.valerie.lexer;

public final class Token {
    public final TokenType type;
    public final String data;
    public final int line;
    public final int column;

    public Token(final TokenType type, final String data, final int line, final int column) {
        this.type = type;
        this.data = data;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return this.type + "(" + this.data + ")@(" + this.line + ", " + this.column + ")";
    }
}