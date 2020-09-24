package com.sci.valerie.parser.util;

import com.sci.valerie.lexer.*;

public enum BinaryOP {
    EQ(TokenType.EQ),
    NE(TokenType.NE),
    LT(TokenType.LT),
    GT(TokenType.GT),
    LTE(TokenType.LTE),
    GTE(TokenType.GTE),
    
    CONCAT(TokenType.CONCAT),
    
    ADD(TokenType.ADD),
    SUB(TokenType.SUB),
    MUL(TokenType.MUL),
    DIV(TokenType.DIV),
    MOD(TokenType.MOD),
    POW(TokenType.POW),
    
    BIT_LSH(TokenType.BIT_LSH),
    BIT_RSH(TokenType.BIT_RSH),
    BIT_ARSH(TokenType.BIT_ARSH),
    
    BIT_AND(TokenType.BIT_AND),
    BIT_OR(TokenType.BIT_OR),
    BIT_XOR(TokenType.BIT_XOR),
    
    AND(TokenType.AND),
    OR(TokenType.OR);
    
    public final TokenType type;
    
    BinaryOP(final TokenType type) {
        this.type = type;
    }
    
    public static BinaryOP fromTokenType(final TokenType type) {
        for(final BinaryOP op : BinaryOP.values()) {
            if(op.type == type) {
                return op;
            }
        }
        return null;
    }
}