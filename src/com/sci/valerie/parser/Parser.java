package com.sci.valerie.parser;

import java.io.*;
import java.util.*;

import com.sci.valerie.lexer.*;
import com.sci.valerie.type.*;

import com.sci.valerie.parser.util.*;

import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.stmt.*;
import com.sci.valerie.parser.tree.stmt.decl.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.atom.*;
import com.sci.valerie.parser.tree.expr.type.*;

import static com.sci.valerie.lexer.TokenType.*;

public final class Parser {
    private List<Token> tokens;
    private int index;

    private Scope scope;
    private Stack<Scope> scopes;

    private FunctionST function;
    private Stack<FunctionST> functionStack;

    private int functionDepth;
    private int loopDepth;

    private File file;

    public Parser(final File file, final List<Token> tokens) {
        this.tokens = tokens;   

        this.file = file;

        this.scope = new Scope(0, 0);
        this.scopes = new Stack<>();

        this.functionStack = new Stack<>();
    }

    private void pushLoop() {
        this.loopDepth++;
    }

    private void popLoop() {
        this.loopDepth--;
    }

    private void pushFunction() {
        this.functionDepth++;
    }

    private void popFunction() {
        this.functionDepth--;
    }

    private void pushScope() {
        this.scopes.push(this.scope);
        this.scope = new Scope(this.scope, this.functionDepth, this.loopDepth);
    }

    private void popScope() {
        this.scope = this.scopes.pop();
    }

    private void next() {
        this.index++;
    }
    
    private void prev() {
        this.index--;
    }
    
    private Token current() {
        return this.tokens.get(this.index);
    }
    
    private boolean more() {
        return this.index < this.tokens.size();
    }
    
    private boolean accept(final TokenType... types) {
        if(!this.more()) {
            return false;
        }
        
        for(int i = 0; i < types.length; i++) {
            if(this.current().type == types[i]) {
                return true;
            }
        }
        
        return false;
    }
    
    private void expect(final TokenType... types) throws ParseException {
        if(!this.accept(types)) {
            final StringBuilder possibilities = new StringBuilder();

            for(final TokenType type : types) {
                possibilities.append(type);
                possibilities.append(" ");
            }

            throw new ParseException("Unexpected token: " + this.current() + " Possibilities: " + possibilities);
        }
    }

    private void expectNot(final TokenType... types) throws ParseException {
        if(this.accept(types)) {
            throw new ParseException("Unexpected token: " + this.current());
        }
    }

    private ExpressionST parseBinary(final ExprParser next, final TokenType... types) throws ParseException {
        final ExpressionST left = next.parse();
        
        if(this.accept(types)) {
            ExpressionST ret = left;
            
            while(this.accept(types)) {
                final BinaryOP op = BinaryOP.fromTokenType(this.current().type);
                this.next();
                
                ret = new BinaryST(this.scope, op, ret, next.parse());
            }
            
            return ret;
        }
        
        return left;
    }

    private IdentST parseIdent() throws ParseException {
        this.expect(IDENT);
        final IdentST st = new IdentST(this.scope, this.current().data);
        this.next();
        return st;
    }

    private StringST parseString() throws ParseException {
        this.expect(STRING);
        final StringST st = new StringST(this.scope, this.current().data);
        this.next();
        return st;
    }

    private BoolST parseBool() throws ParseException {
        this.expect(TRUE, FALSE);
        final BoolST st = new BoolST(this.scope, this.current().type == TRUE);
        this.next();
        return st;
    }

    private NumberST parseNumber() throws ParseException {
        this.expect(NUMBER);
        final NumberST st = new NumberST(this.scope, this.current().data);
        this.next();
        return st;
    }
    
    private TypeST parseType() throws ParseException {
        if(this.accept(LPAREN)) {
            return this.parseFunctionType(false);
        } else if(this.accept(LBRACK)) {
            this.next();
            this.expect(RBRACK);
            this.next();
            return new ArrayTypeST(this.scope, this.parseType());
        } else {
            final TypeST type = new BasicTypeST(this.scope, this.parseIdent());

            if(this.accept(LBRACK)) {
                this.next();

                final TypeST key = this.parseType();

                this.expect(RBRACK);
                this.next();

                return new MapTypeST(this.scope, key, type);
            }

            return type;
        }
    }

    private FunctionTypeST parseFunctionType() throws ParseException {
        return this.parseFunctionType(true);
    }

    private FunctionTypeST parseFunctionType(final boolean allowForeign) throws ParseException {
        final List<ParameterST> parameters = new ArrayList<>();
        
        this.expect(LPAREN);
        this.next();

        while(!this.accept(RPAREN)) {
            final IdentST name = this.parseIdent();

            this.expect(COLON);
            this.next();

            parameters.add(new ParameterST(this.scope, name, this.parseType()));

            this.expect(COMMA, RPAREN);
            if(this.accept(COMMA)) {
                this.next();
                this.expectNot(RPAREN);
            }
        }

        this.next();

        final ParameterST[] params = parameters.toArray(new ParameterST[parameters.size()]);

        TypeST rtype = null;
        if(this.accept(ARROW)) {
            this.next();
            rtype = this.parseType();
        } 

        boolean foreign = false;

        if(allowForeign) {
            if(this.accept(FOREIGN)) {
                this.next();
                foreign = true;
            }
        }
    
        return new FunctionTypeST(this.scope, params, rtype, foreign);
    }
    
    private FunctionST parseFunction() throws ParseException {
        this.pushScope();
        this.pushFunction();

        final FunctionTypeST type = this.parseFunctionType();

        final BlockST block;
        if(type.foreign) {
            block = null;
        } else {
            block = new BlockST(this.scope);
        }

        final FunctionST function = new FunctionST(this.scope.parent, type, block);

        this.functionStack.push(this.function);
        this.function = function;

        if(!type.foreign) {
            this.expect(LBRACE);
            this.next();

            while(!this.accept(RBRACE)) {
                block.addStatement(this.parseStatement());
            }

            this.next();
        }

        this.popScope();
        this.popFunction();

        this.function = this.functionStack.pop();

        return function;
    }

    private ExpressionST parseMake() throws ParseException {
        this.expect(MAKE);
        this.next();

        this.expect(LPAREN);
        this.next();

        final TypeST type = this.parseType();

        if(type instanceof ArrayTypeST) {
            this.expect(COMMA);
            this.next();

            final ExpressionST size = this.parseExpression();

            this.expect(RPAREN);
            this.next();

            return new NewArrayST(this.scope, (ArrayTypeST) type, size);
        } else if(type instanceof MapTypeST) {
            this.expect(RPAREN);
            this.next();

            return new NewMapST(this.scope, (MapTypeST) type);
        } else if(type instanceof BasicTypeST) {
            this.expect(RPAREN);
            this.next();

            return new NewStructST(this.scope, type);
        } else {
            throw new AssertionError();
        }
    }

    private StructTypeST parseStruct() throws ParseException {
        this.expect(STRUCT);
        this.next();

        this.expect(LBRACE);
        this.next();

        final StructTypeST struct = new StructTypeST(this.scope);

        while(!this.accept(RBRACE)) {
            final IdentST name = this.parseIdent();

            this.expect(COLON);
            this.next();
            
            final TypeST type = this.parseType();

            struct.addField(name.ident, type);

            this.expect(SEMICOLON);
            this.next();
        }

        this.next();

        return struct;
    }

    private ExpressionST parseAtom() throws ParseException {
        this.expect(LPAREN, STRING, NUMBER, IDENT, TRUE, FALSE, NIL, MAKE, STRUCT);
        if(this.accept(LPAREN)) {
            boolean isFunction;
            {
                // TODO(sci4me): fix this piece of shit code

                final int savedIndex = this.index; 
                try {
                    this.parseFunctionType();
                    isFunction = true;
                } catch(final ParseException e) {
                    isFunction = false;
                }
                this.index = savedIndex;
            }

            if(isFunction) {
                return this.parseFunction();
            } else {
                this.next();
                final ParenST st = new ParenST(this.scope, this.parseExpression());
                this.expect(RPAREN);
                this.next();
                return st;
            }
        } else if(this.accept(STRING)) {
            return this.parseString();
        } else if(this.accept(NUMBER)) {
            return this.parseNumber();
        } else if(this.accept(IDENT)) {
            return this.parseIdent();
        } else if(this.accept(TRUE, FALSE)) {
            return this.parseBool();
        } else if(this.accept(NIL)) {
            this.next();
            return new NilST(this.scope);
        } else if(this.accept(MAKE)) {
            return this.parseMake();
        } else if(this.accept(STRUCT)) {
            return this.parseStruct();
        }

        throw new AssertionError(current().toString());
    }

    private ExpressionST parseDots() throws ParseException {
        final ExpressionST left = this.parseAtom();

        if(this.accept(PERIOD)) {
            ExpressionST ret = left;

            while(this.accept(PERIOD)) {
                this.next();

                final IdentST key = this.parseIdent();

                ret = new DotST(this.scope, ret, key.ident);
            }

            return ret;
        }

        return left;
    }

    private ExpressionST parseBrackets() throws ParseException {
        final ExpressionST left = this.parseDots();
        
        if(this.accept(LPAREN, LBRACK)) {
            ExpressionST ret = left;
            
            while(this.accept(LPAREN, LBRACK)) {
                if(this.accept(LPAREN)) {
                    final int line = this.current().line;
                    this.next();
                    
                    final List<ExpressionST> args = new ArrayList<>();
                    
                    while(!this.accept(RPAREN)) {
                        args.add(this.parseExpression());
                        
                        this.expect(COMMA, RPAREN);
                        if(this.accept(COMMA)) {
                            this.next();
                        }
                    }
                    this.next();
                    
                    final CallST call = new CallST(this.scope, ret, args.toArray(new ExpressionST[args.size()]));

                    if(ret instanceof IdentST) {
                        call.line = Integer.toString(line);
                        call.name = ((IdentST) ret).ident;
                        call.file = this.file.getName();
                    }

                    ret = call;
                } else {
                    this.next();

                    final ExpressionST index = this.parseExpression();

                    this.expect(RBRACK);
                    this.next();

                    ret = new AccessST(this.scope, ret, index);
                }
            }
            
            return ret;
        }
        
        return left;
    }

    private ExpressionST parsePower() throws ParseException {
        return this.parseBinary(this::parseBrackets, POW);
    }

    private ExpressionST parseUnary() throws ParseException {
        UnaryOP op = UnaryOP.fromTokenType(this.current().type);
        if(op != null) {
            final List<UnaryOP> ops = new ArrayList<>();
            
            while(op != null) {
                this.next();
                ops.add(op);
                op = UnaryOP.fromTokenType(this.current().type);
            }
            
            ExpressionST ret = this.parsePower();
            
            for(int i = ops.size() - 1; i >= 0; i--) {
                ret = new UnaryST(this.scope, ops.get(i), ret);
            }
            
            return ret;
        } else {
            return this.parsePower();    
        } 
    }

    private ExpressionST parseCast() throws ParseException {
        if(this.accept(CAST)) {
            final List<TypeST> types = new ArrayList<>();

            while(this.accept(CAST)) {
                this.next();

                this.expect(LPAREN);
                this.next();

                types.add(this.parseType());

                this.expect(RPAREN);
                this.next();
            }

            ExpressionST ret = this.parseUnary();

            for(int i = types.size() - 1; i >= 0; i--) {
                ret = new CastST(this.scope, types.get(i), ret);
            }

            return ret;
        } else {
            return this.parseUnary();
        }
    }

    private ExpressionST parseBinaryMul() throws ParseException {
        return this.parseBinary(this::parseCast, MUL, DIV, MOD);
    }
    
    private ExpressionST parseBinaryAdd() throws ParseException {
        return this.parseBinary(this::parseBinaryMul, ADD, SUB);
    }
    
    private ExpressionST parseBinaryShift() throws ParseException {
        return this.parseBinary(this::parseBinaryAdd, BIT_LSH, BIT_RSH, BIT_ARSH);    
    }
    
    private ExpressionST parseBinaryComparison() throws ParseException {
        return this.parseBinary(this::parseBinaryShift, LT, GT, LTE, GTE);
    }
    
    private ExpressionST parseBinaryEquality() throws ParseException {
        return this.parseBinary(this::parseBinaryComparison, EQ, NE);
    }
    
    private ExpressionST parseBinaryAnd() throws ParseException {
        return this.parseBinary(this::parseBinaryEquality, BIT_AND);
    }
    
    private ExpressionST parseBinaryXor() throws ParseException {
        return this.parseBinary(this::parseBinaryAnd, BIT_XOR);
    }
    
    private ExpressionST parseBinaryOr() throws ParseException {
        return this.parseBinary(this::parseBinaryXor, BIT_OR);
    }
    
    private ExpressionST parseBinaryLogicalAnd() throws ParseException {
        return this.parseBinary(this::parseBinaryOr, AND);
    }
    
    private ExpressionST parseBinaryLogicalOr() throws ParseException {
        return this.parseBinary(this::parseBinaryLogicalAnd, OR);
    }

    private ExpressionST parseConcat() throws ParseException {
        return this.parseBinary(this::parseBinaryLogicalOr, CONCAT);
    }

    private ExpressionST parseExpression() throws ParseException {
        return this.parseConcat();    
    }

    private BlockST parseBlock() throws ParseException {
        this.expect(LBRACE);
        this.next();

        final BlockST block = new BlockST(this.scope);

        this.pushScope();

        while(!this.accept(RBRACE)) {
            block.addStatement(this.parseStatement());
        }

        this.popScope();

        this.next();

        return block;
    }

    private IfST parseIf() throws ParseException {
        this.expect(IF);
        this.next();

        final ExpressionST condition = this.parseExpression();
        final StatementST success = this.parseStatement();
        final StatementST failure;

        if(this.accept(ELSE)) {
            this.next();
            failure = this.parseStatement();
        } else {
            failure = null;
        }

        return new IfST(this.scope, condition, success, failure);
    }

    private ReturnST parseReturn() throws ParseException {
        this.expect(RETURN);
        this.next();

        if(this.accept(SEMICOLON)) {
            return new ReturnST(this.scope, null, this.function);
        }

        return new ReturnST(this.scope, this.parseExpression(), this.function);
    }
    
    private DeferST parseDefer() throws ParseException {
        this.expect(DEFER);
        this.next();

        this.function.markHasDefer();

        return new DeferST(this.scope, this.parseStatement());
    }

    private WhileST parseWhile() throws ParseException {
        this.expect(WHILE);
        this.next();

        this.pushLoop();
        this.pushScope();
        
        final ExpressionST condition = this.parseExpression();
        final StatementST code = this.parseStatement();
        final WhileST st = new WhileST(this.scope, this.function, condition, code);
        
        this.popScope();
        this.popLoop();
        
        return st;
    }

    private DoST parseDo() throws ParseException {
        this.expect(DO);
        this.next();

        this.pushLoop();
        this.pushScope();

        final StatementST code = this.parseStatement();

        this.expect(WHILE);
        this.next();

        final ExpressionST condition = this.parseExpression();

        final DoST st = new DoST(this.scope, this.function, condition, code);

        this.popScope();
        this.popLoop();

        return st;
    }

    private ForST parseFor() throws ParseException {
        this.expect(FOR);
        this.next();

        this.pushLoop();
        this.pushScope();

        final StatementST init = this.parseStatement();
        final ExpressionST condition;
        
        if(this.accept(SEMICOLON)) {
            condition = null;
        } else {
            condition = this.parseExpression();
        } 

        this.expect(SEMICOLON);
        this.next();

        final StatementST afterthought = this.parseStatement(false);
        final StatementST code = this.parseStatement();

        final ForST st = new ForST(this.scope, this.function, init, condition, afterthought, code);

        this.popScope();
        this.popLoop();

        return st;
    }

    private ImportST parseImport() throws ParseException {
        this.expect(IMPORT);
        this.next();

        return new ImportST(this.scope, this.parseString());
    }

    private SwitchST parseSwitch() throws ParseException {
        this.expect(SWITCH);
        this.next();

        final ExpressionST value = this.parseExpression();
        final SwitchST st = new SwitchST(this.scope, value);

        this.expect(LBRACE);
        this.next();

        while(!this.accept(RBRACE)) {
            if(st.getDefaultCase() == null) {
                this.expect(CASE, DEFAULT);
            } else {
                this.expect(CASE);
            }

            if(this.accept(CASE)) {
                this.next();

                final ExpressionST key = this.parseExpression();

                this.expect(COLON);
                this.next();

                final StatementST code = this.parseStatement();

                st.addCase(key, code);
            } else {
                this.next();
                this.expect(COLON);
                this.next();

                st.setDefaultCase(this.parseStatement());
            }
        }

        this.next();

        return st;
    }   

    private StatementST parseStatement() throws ParseException {
        return this.parseStatement(true);
    }

    private StatementST parseStatement(final boolean requireSemicolon) throws ParseException {
        if(this.accept(RUN)) {
            final ExpressionST run = this.parseExpression();

            if(!(run instanceof UnaryST) || ((UnaryST) run).op != UnaryOP.RUN) {
                throw new AssertionError();
            }

            if(requireSemicolon) {
                this.expect(TokenType.SEMICOLON);
                this.next();
            }

            return new ExpressionStatementST(this.scope, run);
        } else if(this.accept(IDENT)) {
            final IdentST ident = this.parseIdent();
            final ExpressionST left;

            if(this.accept(LBRACK)) {
                this.index--;

                left = this.parseExpression();

                if(left instanceof CallST) {
                    final CallST call = (CallST) left;

                    if(this.accept(SEMICOLON)) {
                        this.next();
                        return new ExpressionStatementST(this.scope, call);
                    }
                }

                this.expectNot(CONST_DECL, ASSIGN_DECL, COLON);
            } else if(this.accept(PERIOD)) {
                this.index--;

                left = this.parseExpression();
            } else {
                left = ident;
            }

            this.expect(LPAREN, CONST_DECL, ASSIGN_DECL, COLON, INC, DEC,
                        ASSIGN, CONCAT_ASSIGN, ADD_ASSIGN, SUB_ASSIGN, 
                        MUL_ASSIGN, DIV_ASSIGN, MOD_ASSIGN, POW_ASSIGN, 
                        BIT_LSH_ASSIGN, BIT_RSH_ASSIGN, BIT_ARSH_ASSIGN,     
                        BIT_AND_ASSIGN, BIT_OR_ASSIGN, BIT_XOR_ASSIGN, 
                        BIT_NOT_ASSIGN, AND_ASSIGN, OR_ASSIGN);
            if(this.accept(LPAREN)) {
                this.index--;

                final ExpressionST expr = this.parseExpression();

                if(!(expr instanceof CallST)) {
                    throw new ParseException("Invalid call");
                }

                final CallST call = (CallST) expr;

                if(requireSemicolon) {
                    this.expect(TokenType.SEMICOLON);
                    this.next();
                }

                return new ExpressionStatementST(this.scope, call);
            } else if(this.accept(CONST_DECL)) {
                this.next();

                final ExpressionST value = this.parseExpression();

                if(!(value instanceof FunctionST || value instanceof StructTypeST)) {
                    if(requireSemicolon) {
                        this.expect(TokenType.SEMICOLON);
                        this.next();
                    }
                }

                return new ConstDeclST(this.scope, ident, value);
            } else if(this.accept(ASSIGN_DECL)) {
                this.next();

                final ExpressionST value = this.parseExpression();

                if(requireSemicolon) {
                    this.expect(TokenType.SEMICOLON);
                    this.next();
                }

                return new AssignDeclST(this.scope, ident, value);
            } else if(this.accept(COLON)) {
                this.next();

                final TypeST type = this.parseType();

                final ExpressionST defaultValue;

                if(this.accept(ASSIGN)) {
                    this.next();
                    defaultValue = this.parseExpression();
                } else {
                    defaultValue = null;
                }

                if(requireSemicolon) {
                    this.expect(TokenType.SEMICOLON);
                    this.next();
                }

                return new VariableDeclST(this.scope, ident, type, defaultValue);
            } else if(this.accept(ASSIGN, CONCAT_ASSIGN, ADD_ASSIGN, SUB_ASSIGN, 
                                MUL_ASSIGN, DIV_ASSIGN, MOD_ASSIGN, POW_ASSIGN, 
                                BIT_LSH_ASSIGN, BIT_RSH_ASSIGN, BIT_ARSH_ASSIGN,     
                                BIT_AND_ASSIGN, BIT_OR_ASSIGN, BIT_XOR_ASSIGN, 
                                BIT_NOT_ASSIGN, AND_ASSIGN, OR_ASSIGN)) {
                final AssignOP op = AssignOP.fromTokenType(this.current().type);
                    
                this.next();

                final AssignST assign = new AssignST(this.scope, op, left, this.parseExpression());

                if(requireSemicolon) {
                    this.expect(TokenType.SEMICOLON);
                    this.next();
                }

                return assign;
            } else if(this.accept(INC)) {
                this.next();

                if(requireSemicolon) {
                    this.expect(TokenType.SEMICOLON);
                    this.next();
                }

                return new IncST(this.scope, left);
            } else if(this.accept(DEC)) {
                this.next();

                if(requireSemicolon) {
                    this.expect(TokenType.SEMICOLON);
                    this.next();
                }

                return new DecST(this.scope, left);
            }
        } else if(this.accept(BREAK)) {
            this.next();
            if(requireSemicolon) {
                this.expect(TokenType.SEMICOLON);
                this.next();
            }
            return new BreakST(this.scope);
        } else if(this.accept(CONTINUE)) {
            this.next();
            if(requireSemicolon) {
                this.expect(TokenType.SEMICOLON);
                this.next();
            }
            return new ContinueST(this.scope);
        } else if(this.accept(IF)) {
            return this.parseIf();
        } else if(this.accept(LBRACE)) {
            return this.parseBlock();
        } else if(this.accept(RETURN)) {
            final ReturnST st = this.parseReturn();

            if(requireSemicolon) {
                this.expect(TokenType.SEMICOLON);
                this.next();
            }

            return st;  
        } else if(this.accept(DEFER)) {
            final DeferST st = this.parseDefer();

            //this.expect(SEMICOLON);
            //this.next();

            return st;
        } else if(this.accept(WHILE)) {
            return this.parseWhile();
        } else if(this.accept(DO)) {
            final DoST st = this.parseDo();

            if(requireSemicolon) {
                this.expect(TokenType.SEMICOLON);
                this.next();
            }

            return st;
        } else if(this.accept(FOR)) {
            return this.parseFor();
        } else if(this.accept(SEMICOLON)) {
            this.next();
            return new EmptyST(this.scope);
        } else if(this.accept(SWITCH)) {
            return this.parseSwitch();
        }

        throw new RuntimeException();
    }

    public FileST parse() throws ParseException {
        final FileST file = new FileST(this.scope, this.file);

        while(this.more()) {
            this.expect(RUN, IMPORT, IDENT);
            
            if(this.accept(IMPORT)) {
                file.addStatement(this.parseImport());
            } else {
                file.addStatement(this.parseStatement());
            }
        }

        return file;
    }

    @FunctionalInterface
    private static interface ExprParser {
        public ExpressionST parse() throws ParseException;
    }
}