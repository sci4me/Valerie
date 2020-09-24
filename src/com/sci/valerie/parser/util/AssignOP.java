package com.sci.valerie.parser.util;

import com.sci.valerie.lexer.*;

public enum AssignOP {
    ASSIGN(TokenType.ASSIGN),

    CONCAT_ASSIGN(TokenType.CONCAT_ASSIGN),

    ADD_ASSIGN(TokenType.ADD_ASSIGN),
    SUB_ASSIGN(TokenType.SUB_ASSIGN),
    MUL_ASSIGN(TokenType.MUL_ASSIGN),
    DIV_ASSIGN(TokenType.DIV_ASSIGN),
    MOD_ASSIGN(TokenType.MOD_ASSIGN),
    POW_ASSIGN(TokenType.POW_ASSIGN),

    BIT_LSH_ASSIGN(TokenType.BIT_LSH_ASSIGN),
    BIT_RSH_ASSIGN(TokenType.BIT_RSH_ASSIGN),
    BIT_ARSH_ASSIGN(TokenType.BIT_ARSH_ASSIGN),

    BIT_AND_ASSIGN(TokenType.BIT_AND_ASSIGN),
    BIT_OR_ASSIGN(TokenType.BIT_OR_ASSIGN),
    BIT_XOR_ASSIGN(TokenType.BIT_XOR_ASSIGN),

    AND_ASSIGN(TokenType.AND_ASSIGN),
    OR_ASSIGN(TokenType.OR_ASSIGN);

    public final TokenType type;
    
    AssignOP(final TokenType type) {
        this.type = type;
    }
    
    public static AssignOP fromTokenType(final TokenType type) {
        for(final AssignOP op : AssignOP.values()) {
            if(op.type == type) {
                return op;
            }
        }
        return null;
    }
}