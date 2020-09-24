package com.sci.valerie.parser.util;

import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.stmt.*;
import com.sci.valerie.parser.tree.stmt.decl.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.atom.*;
import com.sci.valerie.parser.tree.expr.type.*;

public abstract class NodeVisitorAdapter implements INodeVisitor {
    @Override
    public void visitFile(final FileST st) { 

    }

    @Override
    public void visitIdent(final IdentST st) { 

    }

    @Override
    public void visitString(final StringST st) { 

    }
    
    @Override
    public void visitNil(final NilST st) {

    }

    @Override
    public void visitBool(final BoolST st) {

    }

    @Override
    public void visitNumber(final NumberST st) {
        
    }

    @Override
    public void visitNewArray(final NewArrayST st) {
        
    }

    @Override
    public void visitNewMap(final NewMapST st) {
        
    }

    @Override
    public void visitNewStruct(final NewStructST st) {
        
    }

    @Override
    public void visitParen(final ParenST st) {
        
    }

    @Override
    public void visitUnary(final UnaryST st) { 

    }

    @Override
    public void visitBinary(final BinaryST st) { 

    }

    @Override
    public void visitAccess(final AccessST st) {
        
    }

    @Override
    public void visitDot(final DotST st) {
        
    }

    @Override
    public void visitCast(final CastST st) {
        
    }

    @Override
    public void visitCall(final CallST st) { 

    }

    @Override
    public void visitAssign(final AssignST st) {
        
    }

    @Override 
    public void visitInc(final IncST st) {

    }

    @Override 
    public void visitDec(final DecST st) {

    }

    @Override
    public void visitSwitch(final SwitchST st) {
        
    }

    @Override
    public void visitImport(final ImportST st) {
        
    }

    @Override 
    public void visitBreak(final BreakST st) {

    }

    @Override 
    public void visitContinue(final ContinueST st) {

    }

    @Override
    public void visitWhile(final WhileST st) {
        
    }

    @Override
    public void visitDo(final DoST st) {
        
    }

    @Override
    public void visitFor(final ForST st) {
        
    }

    @Override
    public void visitIf(final IfST st) {
        
    }

    @Override
    public void visitReturn(final ReturnST st) {
        
    }

    @Override
    public void visitDefer(final DeferST st) {
        
    }

    @Override
    public void visitParameter(final ParameterST st) { 

    }

    @Override
    public void visitFunctionType(final FunctionTypeST st) { 

    }

    @Override
    public void visitBasicType(final BasicTypeST st) { 

    }

    @Override
    public void visitArrayType(final ArrayTypeST st) {
        
    }

    @Override
    public void visitMapType(final MapTypeST st) {
        
    }

    @Override
    public void visitStructType(final StructTypeST st) {
        
    }

    @Override
    public void visitBlock(final BlockST st) { 

    }

    @Override
    public void visitFunction(final FunctionST st) { 

    }

    @Override
    public void visitExpressionStatement(final ExpressionStatementST st) { 

    }

    @Override
    public void visitConstDecl(final ConstDeclST st) { 

    }

    @Override
    public void visitAssignDecl(final AssignDeclST st) {
        
    }

    @Override 
    public void visitVariableDecl(final VariableDeclST st) {
        
    }

    @Override
    public void visitEmpty(final EmptyST st) {
        
    }
}