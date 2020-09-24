package com.sci.valerie.lexer;

public enum TokenType {
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACK("["),
    RBRACK("]"),
    
    EQ("=="),
    NE("!="),
    LT("<"),
    GT(">"),
    LTE("<="),
    GTE(">="),
    
    CONCAT(".."),
    
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    POW("**"),
    
    BIT_LSH("<<"),
    BIT_RSH(">>"),
    BIT_ARSH(">>>"),
    
    BIT_AND("&"),
    BIT_OR("|"),
    BIT_XOR("^"),
    BIT_NOT("~"),
    
    AND("&&"),
    OR("||"),
    NOT("!"),
    
    ASSIGN("="),
    
    CONCAT_ASSIGN("..="),
    
    ADD_ASSIGN("+="),
    SUB_ASSIGN("-="),
    MUL_ASSIGN("*="),
    DIV_ASSIGN("/="),
    MOD_ASSIGN("%="),
    POW_ASSIGN("**="),
    
    BIT_LSH_ASSIGN("<<="),
    BIT_RSH_ASSIGN(">>="),
    BIT_ARSH_ASSIGN(">>>="),

    BIT_AND_ASSIGN("&="),
    BIT_OR_ASSIGN("|="),
    BIT_XOR_ASSIGN("^="),
    BIT_NOT_ASSIGN("~="),
    
    AND_ASSIGN("&&="),
    OR_ASSIGN("||="),
    
    INC("++"),
    DEC("--"),
    
    COLON(":"),
    SEMICOLON(";"),
    PERIOD("."),
    COMMA(","),
    POUND("#"),
    QUESTION("?"),
    
    ELLIPSIS("..."),
    
    CONST_DECL("::"),
    ASSIGN_DECL(":="),

    ARROW("->"),
    
    TRUE("true"),
    FALSE("false"),
    NIL("nil"),
    
    FOR("for"),
    WHILE("while"),
    DO("do"),
    BREAK("break"),
    CONTINUE("continue"),
    IF("if"),
    ELSE("else"),
    SWITCH("switch"),
    CASE("case"),
    DEFAULT("default"),
    DEFER("defer"),
    RETURN("return"),
    CAST("cast"),
    STRUCT("struct"),
    MAKE("make"),
    TYPE("type"),

    IDENT(null),
    STRING(null),
    NUMBER(null),

    RUN("#run"),
    IMPORT("#import"),
    FOREIGN("#foreign");
        
    public final String ident;
    
    private TokenType(final String ident) {
        this.ident = ident;
    }
}