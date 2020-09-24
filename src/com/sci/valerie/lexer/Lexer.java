package com.sci.valerie.lexer;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class Lexer {
    private static boolean isDigit(final char c) {
        return c >= '0' && c <= '9';
    }
    
    private static boolean isLetter(final char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
    
    private static boolean isAlpha(final char c) {
        return Lexer.isDigit(c) || Lexer.isLetter(c);
    }
    
    private static boolean isIdent(final char c) {
        return Lexer.isAlpha(c) || c == '_';
    }
    
    private static boolean isWhitespace(final char c) {
        return c == ' ' || c == '\t' || c == '\r';
    }

    private String source;
    private int start;
    private int pos;
    private List<Token> tokens;
    private int line;
    private int column;

    public Lexer(final File file) throws IOException {
        this.source = new String(Files.readAllBytes(file.toPath()));   
        this.tokens = new ArrayList<>();
    }

    private String current() {
        return this.source.substring(this.start, this.pos);
    }
    
    private void emit(final TokenType type) {
        this.tokens.add(new Token(type, this.current(), this.line + 1, this.column - (this.pos - this.start) + 1));
        this.start = this.pos;    
    }
    
    private char next() {
        if(!this.more()) {
            return 0;
        }
        
        final char c = this.source.charAt(this.pos);
        this.pos++;
        this.column++;
        return c;
    }
    
    private void prev() {
        this.pos--;
        this.column--;
    }
    
    private char peek() {
        if(!this.more()) {
            return 0;
        }
        
        return this.source.charAt(this.pos);
    }
    
    private boolean more() {
        return this.pos < this.source.length();
    }

    private void ignore() {
        this.start = this.pos;
    }
    
    private boolean accept(final String valid) {
        if(!this.more()) {
            return false;
        }
        
        if(valid.contains(String.valueOf(this.next()))) {
            return true;
        }
        this.prev();
        return false;
    }
    
    private void acceptRun(final String valid) {
        while(this.more() && valid.contains(String.valueOf(this.peek()))) {
            this.next();
        }
    }
    
    private boolean acceptSeq(final String seq) {
        final int savedPos = this.pos;
        final int savedColumn = this.column;
        
        for(int i = 0; i < seq.length(); i++) {
            if(seq.charAt(i) != this.next()) {
                this.pos = savedPos;
                this.column = savedColumn;
                return false;
            }
        }
        
        return true;
    }

    public List<Token> tokenize() throws LexException {
        boolean running = true;
        while(running) {
            final char c = this.next();
            switch(c) {
                case 0:
                    running = false;
                    break;
                case '\n':
                    this.ignore();
                    this.line++;
                    this.column = 0;
                    break;
                
                case '(':
                    this.emit(TokenType.LPAREN);
                    break;
                case ')':
                    this.emit(TokenType.RPAREN);
                    break;
                case '{':
                    this.emit(TokenType.LBRACE);
                    break;
                case '}':
                    this.emit(TokenType.RBRACE);
                    break;
                case '[':
                    this.emit(TokenType.LBRACK);
                    break;
                case ']':
                    this.emit(TokenType.RBRACK);
                    break;
                case '=':
                    if(this.accept("=")) {
                        this.emit(TokenType.EQ);
                    } else {
                        this.emit(TokenType.ASSIGN);
                    }
                    break;
                case '!':
                    if(this.accept("=")) {
                        this.emit(TokenType.NE);
                    } else {
                        this.emit(TokenType.NOT);
                    }
                    break;
                case '<':
                    if(this.accept("<")) {
                        this.emit(TokenType.BIT_LSH);
                    } else if(this.accept("=")) {
                        this.emit(TokenType.LTE);
                    } else {
                        this.emit(TokenType.LT);
                    }
                    break;
                case '>':
                    if(this.accept(">")) {
                        if(this.accept(">")) {
                            this.emit(TokenType.BIT_ARSH);
                        } else {
                            this.emit(TokenType.BIT_RSH);
                        }
                    } else if(this.accept("=")) {
                        this.emit(TokenType.GTE);
                    } else {
                        this.emit(TokenType.GT);
                    }
                    break;
                case '.':
                    if(this.accept(".")) {
                        if(this.accept(".")) {
                            this.emit(TokenType.ELLIPSIS);
                        } else if(this.accept("=")) { 
                            this.emit(TokenType.CONCAT_ASSIGN);
                        } else {
                            this.emit(TokenType.CONCAT);
                        }
                    } else {
                        this.emit(TokenType.PERIOD);
                    }
                    break;
                case ',':
                    this.emit(TokenType.COMMA);
                    break;
                case '#':
                    if(this.acceptSeq("run")) {
                        this.emit(TokenType.RUN);
                    } else if(this.acceptSeq("import")) {
                        this.emit(TokenType.IMPORT);
                    } else if(this.acceptSeq("foreign")) {
                        this.emit(TokenType.FOREIGN);
                    } else  {
                        this.emit(TokenType.POUND);
                    }
                    break;
                case '?':
                    this.emit(TokenType.QUESTION);
                    break;
                case '+':
                    if(this.accept("+")) {
                        this.emit(TokenType.INC);
                    } else if(this.accept("=")) {
                        this.emit(TokenType.ADD_ASSIGN);
                    } else {
                        this.emit(TokenType.ADD);
                    }
                    break;
                case '-':
                    if(this.accept("-")) {
                        this.emit(TokenType.DEC);
                    } else if(this.accept("=")) {
                        this.emit(TokenType.SUB_ASSIGN);
                    } else if(this.accept(">")) {
                        this.emit(TokenType.ARROW);
                    } else {
                        this.emit(TokenType.SUB);
                    }
                    break;
                case '*':
                    if(this.accept("*")) {
                        this.emit(TokenType.POW);
                    } else if(this.accept("=")) {
                        this.emit(TokenType.MUL_ASSIGN);
                    } else {
                        this.emit(TokenType.MUL);
                    }
                    break;
                case '/':
                    if(this.accept("=")) {
                        this.emit(TokenType.DIV_ASSIGN);
                    } else if(this.accept("/")) {
                        while(this.more() && this.peek() != '\n') {
                            this.next();
                        }
                        this.ignore();
                    } else if(this.accept("*")) {
                        int depth = 1;
                        
                        this.next();
                        while(this.more() && depth > 0) {
                            if(this.acceptSeq("/*")) {
                                depth++;
                            } else if(this.acceptSeq("*/")) {
                                depth--;
                            }
                            
                            this.next();
                        }
                        
                        if(depth != 0) {
                            throw new LexException("Block comment not ended");
                        }
                    } else {
                        this.emit(TokenType.DIV);
                    }
                    break;
                case '%':
                    if(this.accept("=")) {
                        this.emit(TokenType.MOD_ASSIGN);
                    } else {
                        this.emit(TokenType.MOD);
                    }
                    break;
                case '&':
                    if(this.accept("&")) {
                        if(this.accept("=")) {
                            this.emit(TokenType.AND_ASSIGN);
                        } else {
                            this.emit(TokenType.AND);
                        }
                    } else if(this.accept("=")) {
                        this.emit(TokenType.BIT_AND_ASSIGN);
                    } else {
                        this.emit(TokenType.BIT_AND);
                    }
                    break;
                case '|':
                    if(this.accept("|")) {
                        if(this.accept("=")) {
                            this.emit(TokenType.OR_ASSIGN);
                        } else {
                            this.emit(TokenType.OR);
                        }
                    } else if(this.accept("=")) {
                        this.emit(TokenType.BIT_OR_ASSIGN);
                    } else {
                        this.emit(TokenType.BIT_OR);
                    }
                    break;
                case '^':
                    if(this.accept("=")) {
                        this.emit(TokenType.BIT_XOR_ASSIGN);
                    } else {
                        this.emit(TokenType.BIT_XOR);
                    }
                    break;                
                case '~':
                    if(this.accept("=")) {
                        this.emit(TokenType.BIT_NOT_ASSIGN);
                    } else {
                        this.emit(TokenType.BIT_NOT);
                    }
                    break;
                case ':':
                    if(this.accept(":")) {
                        this.emit(TokenType.CONST_DECL);
                    } else if(this.accept("=")) {
                        this.emit(TokenType.ASSIGN_DECL);
                    } else {
                        this.emit(TokenType.COLON);
                    }
                    break;
                case ';':
                    this.emit(TokenType.SEMICOLON);
                    break;
                case '"':
                    final StringBuilder sb = new StringBuilder();

                    while(this.more() && this.peek() != '"') {
                        if(this.peek() == '\\') {
                            this.next();

                            if(this.peek() == '\\') {
                                sb.append('\\');
                            } else if(this.peek() == '\"') { 
                                sb.append('\"');
                            } else if(this.peek() == 'n') {
                                sb.append('\n');
                            } else {
                                throw new LexException("Invalid escape character: " + this.peek());
                            }

                            this.next();                            
                        } else {
                            sb.append(this.peek());
                            this.next();
                        }
                    } 

                    this.next();
                    this.ignore();

                    this.tokens.add(new Token(TokenType.STRING, sb.toString(), this.line + 1, this.column - (this.pos - this.start) + 1));
                    this.start = this.pos; 
                    break;    
                    
                default:
                    if(Lexer.isLetter(c) || c == '_') {
                        while(this.more() && Lexer.isIdent(this.peek())) {
                            this.next();
                        }
                        
                        final String ident = this.current();
                        switch(ident) {
                            case "true":
                                this.emit(TokenType.TRUE);
                                break;
                            case "false":
                                this.emit(TokenType.FALSE);
                                break;
                            case "nil":
                                this.emit(TokenType.NIL);
                                break;
                            case "for":
                                this.emit(TokenType.FOR);
                                break;
                            case "while":
                                this.emit(TokenType.WHILE);
                                break;
                            case "do":
                                this.emit(TokenType.DO);
                                break;
                            case "break":
                                this.emit(TokenType.BREAK);
                                break;
                            case "continue":
                                this.emit(TokenType.CONTINUE);
                                break;
                            case "if":
                                this.emit(TokenType.IF);
                                break;
                            case "else":
                                this.emit(TokenType.ELSE);
                                break;
                            case "switch":
                                this.emit(TokenType.SWITCH);
                                break;
                            case "case":
                                this.emit(TokenType.CASE);
                                break;
                            case "default":
                                this.emit(TokenType.DEFAULT);
                                break;
                            case "defer":
                                this.emit(TokenType.DEFER);
                                break;
                            case "return":
                                this.emit(TokenType.RETURN);
                                break;
                            case "cast":
                                this.emit(TokenType.CAST);
                                break;
                            case "struct":
                                this.emit(TokenType.STRUCT);
                                break;
                            case "make":
                                this.emit(TokenType.MAKE);
                                break;
                            case "type":
                                this.emit(TokenType.TYPE);
                                break;
                            default:
                                this.emit(TokenType.IDENT);
                                break;
                        }
                    } else if(Lexer.isDigit(c)) {
                        // TODO hex, binary, exponential number literals
                        final String digits = "0123456789";
                        
                        this.acceptRun(digits);
                        if(this.accept(".")) {
                            this.acceptRun(digits);
                        }
                        
                        this.emit(TokenType.NUMBER);
                    } else if(Lexer.isWhitespace(c)) {
                        while(this.more() && Lexer.isWhitespace(this.peek())) {
                            this.next();
                        }
                        this.ignore();
                    } else {
                        throw new LexException("Unexpected character: " + c + " at line " + this.line + ", column " + this.column); 
                    }
                    break;
            }
        }

        return this.tokens;
    }
}