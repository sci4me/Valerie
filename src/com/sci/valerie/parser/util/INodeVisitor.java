package com.sci.valerie.parser.util;

import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.stmt.*;
import com.sci.valerie.parser.tree.stmt.decl.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.atom.*;
import com.sci.valerie.parser.tree.expr.type.*;

public interface INodeVisitor {
    void visitFile(final FileST st);

    void visitIdent(final IdentST st);

    void visitString(final StringST st);

    void visitNil(final NilST st);

    void visitBool(final BoolST st);

    void visitNumber(final NumberST st);

    void visitNewArray(final NewArrayST st);

    void visitNewMap(final NewMapST st);

    void visitNewStruct(final NewStructST st);

    void visitParen(final ParenST st);

    void visitUnary(final UnaryST st);

    void visitBinary(final BinaryST st);

    void visitAccess(final AccessST st);

    void visitDot(final DotST st);

    void visitCast(final CastST st);

    void visitCall(final CallST st);

    void visitAssign(final AssignST st);

    void visitInc(final IncST st);

    void visitDec(final DecST st);

    void visitImport(final ImportST st);

    void visitSwitch(final SwitchST st);

    void visitBreak(final BreakST st);

    void visitContinue(final ContinueST st);

    void visitWhile(final WhileST st);

    void visitDo(final DoST st);

    void visitFor(final ForST st);

    void visitIf(final IfST st);

    void visitReturn(final ReturnST st);

    void visitDefer(final DeferST st);

    void visitParameter(final ParameterST st);

    void visitFunctionType(final FunctionTypeST st);

    void visitBasicType(final BasicTypeST st);

    void visitArrayType(final ArrayTypeST st);

    void visitMapType(final MapTypeST st);

    void visitStructType(final StructTypeST st);

    void visitBlock(final BlockST st);

    void visitFunction(final FunctionST st);

    void visitExpressionStatement(final ExpressionStatementST st);

    void visitConstDecl(final ConstDeclST st);

    void visitAssignDecl(final AssignDeclST st);

    void visitVariableDecl(final VariableDeclST st);

    void visitEmpty(final EmptyST st);
}