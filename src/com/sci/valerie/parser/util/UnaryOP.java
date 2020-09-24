package com.sci.valerie.parser.util;

import com.sci.valerie.lexer.*;

public enum UnaryOP {
    NEG(TokenType.SUB),
    NOT(TokenType.NOT),
    BIT_NOT(TokenType.BIT_NOT),
    RUN(TokenType.RUN),
    LENGTH(TokenType.POUND);
    
    public final TokenType type;
    
    UnaryOP(final TokenType type) {
        this.type = type;
    }
    
    public static UnaryOP fromTokenType(final TokenType type) {
        for(final UnaryOP op : UnaryOP.values()) {
            if(op.type == type) {
                return op;
            }
        }
        return null;
    }
}