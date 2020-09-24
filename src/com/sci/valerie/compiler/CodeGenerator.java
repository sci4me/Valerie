package com.sci.valerie.compiler;

import java.util.*;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;

import com.sci.valerie.type.*;

import com.sci.valerie.parser.util.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.stmt.*;
import com.sci.valerie.parser.tree.stmt.decl.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.expr.atom.*;
import com.sci.valerie.parser.tree.expr.type.*;

public final class CodeGenerator extends LinearNodeWorker implements Worker {
    private int counter;

    private LuaValue ctceGlobals;
    private LuaValue ctceLoadstring;

    public CodeGenerator(final CompileManager compiler, final Node[] nodes) {
        super(compiler, nodes);

        this.ctceGlobals = JsePlatform.debugGlobals();
        this.ctceLoadstring = this.ctceGlobals.get("loadstring");

        this.ctceGlobals.set("collectgarbage", LuaValue.NIL);
		this.ctceGlobals.set("dofile", LuaValue.NIL);
		this.ctceGlobals.set("loadfile", LuaValue.NIL);
		this.ctceGlobals.set("module", LuaValue.NIL);
		this.ctceGlobals.set("require", LuaValue.NIL);
		this.ctceGlobals.set("package", LuaValue.NIL);
		this.ctceGlobals.set("io", LuaValue.NIL);
		this.ctceGlobals.set("os", LuaValue.NIL);
		this.ctceGlobals.set("luajava", LuaValue.NIL);
		this.ctceGlobals.set("debug", LuaValue.NIL);
		this.ctceGlobals.set("newproxy", LuaValue.NIL);
    }   

    private void compileTimeExecute(final UnaryST st) {
        final List<Node> dependencies = DependencyComputer.computeDependencies(st.value);

        for(final Node dep : dependencies) {
            final String code = dep.code("main");
            if(code == null) {
                this.waitOn(dep, WaitType.CODE_GENERATE);
                return;
            }
        }

        final StringBuilder sb = new StringBuilder();

        sb.append(this.compiler.globalHeader);

        dependencies.forEach(dep -> { 
            final String code = dep.code("preamble");
            if(code != null) {
                sb.append(code);  
            }
        });

        dependencies.forEach(dep -> {
            final Node node;
            if(dep instanceof ConstDeclST) {
                node = ((ConstDeclST) dep).value;
            } else {
                return;
            }

            final String code = node.code("initializer_preamble");
            if(code != null) {
                sb.append(code);
            }
        });

        dependencies.forEach(dep -> {
            final Node node;
            if(dep instanceof ConstDeclST) {
                node = ((ConstDeclST) dep).value;
            } else {
                return;
            }

            final String code = node.code("initializer");
            if(code != null) {
                sb.append(code);
            }
        });

        dependencies.forEach(dep -> { 
            final String code = dep.code("main_separate");
            if(code != null) {
                sb.append(code);  
            }
        });

        sb.append("return ");
        sb.append(st.value.code("main"));

        final LuaValue c = this.ctceLoadstring.call(LuaValue.valueOf(sb.toString()));
        final Varargs r = c.invoke();

        if(r.narg() == 0) {
            st.setCode("main", "nil");
        } else if(r.narg() == 1) {
            st.setCode("main", r.arg(1).toString());
        } else {
            throw new AssertionError();
        }
    }

    @Override 
    public void visitFile(final FileST st) {

    }

    @Override 
    public void visitIdent(final IdentST st) {
        st.setCode("main", st.ident);
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitString(final StringST st) {
        st.setCode("main", "\"" + st.string + "\"");
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitNil(final NilST st) {
        st.setCode("main", "nil");
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitBool(final BoolST st) {
        st.setCode("main", Boolean.toString(st.value));
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitNumber(final NumberST st) {
        st.setCode("main", st.number);
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitNewArray(final NewArrayST st) {
        final String defaultValue;

        final ArrayTypeST arrayType = st.type;

        final int type = arrayType.type.type();
        if(type == Types.INT || type == Types.FLOAT) {
            defaultValue = "0";
        } else if(type == Types.BOOL) {
            defaultValue = "false";
        } else if(type == Types.STRING) {
            defaultValue = "\"\"";
        } else {
            defaultValue = "nil";
        }

        st.setCode("main", "_vrt_array_init(" + st.size.code("main") + ",\"" + Types.getName(type) + "\"," + defaultValue + ")");
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitNewMap(final NewMapST st) {
        st.setCode("main", "_vrt_map_init()");
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitNewStruct(final NewStructST st) {
        final StringBuilder sb = new StringBuilder();

        final TypeST type = Types.getTypeST(st.type.type());
        if(type instanceof StructTypeST) {
            sb.append("_struct");
            sb.append(((StructTypeST) type).id);
            sb.append("_init()");
        } else {
            throw new AssertionError();
        }

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitParen(final ParenST st) {
        st.setCode("main", "(" + st.value.code("main") + ")"); 
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitUnary(final UnaryST st) {
        switch(st.op) {
            case NEG:
                st.setCode("main", "-" + st.value.code("main"));
                break;
            case NOT:
                st.setCode("main", "not " + st.value.code("main"));
                break;
            case BIT_NOT:
                // TODO(sci4me)
                break;
            case RUN:
                this.compileTimeExecute(st);
                break;
            case LENGTH:
                if(st.value.type() == Types.STRING) {
                    st.setCode("main", st.value.code("main") + ":len()");
                } else {
                    st.setCode("main", st.value.code("main") + ".length");
                }
                break;
        }
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitBinary(final BinaryST st) {
        final String left = st.left.code("main");
        final String right = st.right.code("main");

        switch(st.op) {
            case EQ:
                st.setCode("main", left + "==" + right);
                break;
            case NE:
                st.setCode("main", left + "~=" + right);
                break;
            case LT:
                st.setCode("main", left + "<" + right);
                break;
            case GT:
                st.setCode("main", left + ">" + right);
                break;
            case LTE:
                st.setCode("main", left + "<=" + right);
                break;
            case GTE:
                st.setCode("main", left + ">=" + right);
                break;
            case CONCAT:
                st.setCode("main", "tostring(" + left + ")..tostring(" + right + ")");
                break;
            case ADD:
                st.setCode("main", left + "+" + right);
                break;
            case SUB:
                st.setCode("main", left + "-" + right);
                break;
            case MUL:
                st.setCode("main", left + "*" + right);
                break;
            case DIV:
                if(st.type() == Types.INT) {
                    st.setCode("main", "math.floor(" + left + "/" + right + ")");
                } else {
                    st.setCode("main", left + "/" + right);
                }
                break;
            case MOD:
                st.setCode("main", "math.fmod(" + left + "," + right + ")");
                break;
            case POW:
                st.setCode("main", "math.pow(" + left + "," + right + ")");
                break;
            case BIT_LSH:
                // TODO(sci4me)
                break;
            case BIT_RSH:
                // TODO(sci4me)
                break;
            case BIT_ARSH:
                // TODO(sci4me)
                break;
            case BIT_AND:
                // TODO(sci4me)
                break;
            case BIT_OR:
                // TODO(sci4me)
                break;
            case BIT_XOR:
                // TODO(sci4me)
                break;
            case AND:
                st.setCode("main", left + " and " + right);
                break;
            case OR:
                st.setCode("main", left + " or " + right);
                break;
        }
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitAccess(final AccessST st) {
        final StringBuilder sb = new StringBuilder();

        final int valueType = st.value.type();
        if(valueType == Types.UNKNOWN) {
            this.waitOn(st.value, WaitType.TYPE_DEDUCE);
            return;
        }
        
        sb.append(st.value.code("main"));

        if(valueType == Types.STRING) {
            sb.append(":sub((");
            sb.append(st.index.code("main"));
            sb.append(")+1,(");
            sb.append(st.index.code("main"));
            sb.append(")+1)");
        } else {
            final TypeST type = Types.getTypeST(valueType);

            if(type instanceof ArrayTypeST) {
                sb.append(".data[(");
                sb.append(st.index.code("main"));
                sb.append(")+1]");
            } else if(type instanceof MapTypeST) {
                sb.append("[");
                sb.append(st.index.code("main"));
                sb.append("]");
            } else {
                throw new AssertionError();
            }
        }

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitDot(final DotST st) {
        final StringBuilder sb = new StringBuilder();

        sb.append(st.value.code("main"));
        sb.append(".");
        sb.append(st.key);

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitCast(final CastST st) {
        if(st.type.type() == Types.INT && st.value.type() == Types.FLOAT) {
            st.setCode("main", "math.floor(" + st.value.code("main") + ")");
        } else if(st.type.type() == Types.STRING && st.value.type() == Types.INT) {
            st.setCode("main", "string.char(" + st.value.code("main") + ")");
        } else if(st.type.type() == Types.INT && st.value.type() == Types.STRING) {
            st.setCode("main", "string.byte(" + st.value.code("main") + ")");
        } else {
            st.setCode("main", st.value.code("main"));
        }
        
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitCall(final CallST st) {
        final StringBuilder sb = new StringBuilder();

        sb.append("_vrt_call(");
        sb.append(st.function.code("main"));
        sb.append(", ");

        sb.append("{line=\"");
        sb.append(st.line);
        sb.append("\",file=\"");
        sb.append(st.file);
        sb.append("\",name=\"");
        sb.append(st.name);
        sb.append("\"}");

        if(st.arguments.length > 0) {
            sb.append(", ");
            for(int i = 0; i < st.arguments.length; i++) {
                sb.append(st.arguments[i].code("main"));

                if(i < st.arguments.length - 1) {
                    sb.append(",");
                }
            }
        }

        sb.append(")");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitAssign(final AssignST st) {
        final StringBuilder sb = new StringBuilder();

        sb.append(st.left.code("main"));
        sb.append(" = ");

        switch(st.op) {
            case MOD_ASSIGN:
                sb.append("math.fmod(");
                sb.append(st.left.code("main"));
                sb.append(",");
                sb.append(st.right.code("main"));
                sb.append(")");
                break;
            case POW_ASSIGN:
                sb.append("math.pow(");
                sb.append(st.left.code("main"));
                sb.append(",");
                sb.append(st.right.code("main"));
                sb.append(")");
                break;
            case BIT_LSH_ASSIGN:
                // TODO(sci4me)
                break;
            case BIT_RSH_ASSIGN:
                // TODO(sci4me)
                break;
            case BIT_ARSH_ASSIGN:
                // TODO(sci4me)
                break;
            case BIT_AND_ASSIGN:
                // TODO(sci4me)
                break;
            case BIT_OR_ASSIGN:
                // TODO(sci4me)
                break;
            case BIT_XOR_ASSIGN:
                // TODO(sci4me)
                break;
            default:
                if(st.op != AssignOP.ASSIGN) {
                    sb.append(st.left.code("main"));

                    switch(st.op) {
                        case CONCAT_ASSIGN:
                            sb.append("..");
                            break;
                        case ADD_ASSIGN:
                            sb.append("+");
                            break;
                        case SUB_ASSIGN:
                            sb.append("-");
                            break;
                        case MUL_ASSIGN:
                            sb.append("*");
                            break;
                        case DIV_ASSIGN:
                            sb.append("/");
                            break;
                        case AND_ASSIGN:
                            sb.append(" and ");
                            break;
                        case OR_ASSIGN:
                            sb.append(" or ");
                            break;
                    }
                }

                sb.append(st.right.code("main"));
                sb.append("\n");
                break;
        }

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitInc(final IncST st) {
        final StringBuilder sb = new StringBuilder();

        sb.append(st.expr.code("main"));
        sb.append("=");
        sb.append(st.expr.code("main"));
        sb.append("+1\n");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitDec(final DecST st) {
        final StringBuilder sb = new StringBuilder();

        sb.append(st.expr.code("main"));
        sb.append("=");
        sb.append(st.expr.code("main"));
        sb.append("-1\n");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitSwitch(final SwitchST st) {
        final StringBuilder sb = new StringBuilder();

        final int id = ++this.counter;

        sb.append("local _switch_table");
        sb.append(id);
        sb.append(" = {\n");

        int i = 0;
        for(final Map.Entry<ExpressionST, StatementST> entry : st.getCases().entrySet()) {
            sb.append("[");
            sb.append(entry.getKey().code("main"));
            sb.append("]=function()\n");
            sb.append(entry.getValue().code("main"));
            sb.append("end");

            i++;
            if(i < st.getCases().size()) {
                sb.append(",");
            }

            sb.append("\n");
        }

        sb.append("}\n");

        sb.append("local _switch_case");
        sb.append(id);
        sb.append(" = _switch_table");
        sb.append(id);
        sb.append("[");
        sb.append(st.value.code("main"));
        sb.append("]\n");

        sb.append("if _switch_case");
        sb.append(id);
        sb.append(" then\n");
        sb.append("_switch_case");
        sb.append(id);
        sb.append("()\n");

        if(st.getDefaultCase() != null) {
            sb.append("else\n");
            sb.append(st.getDefaultCase().code("main"));
        }

        sb.append("end\n");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitBreak(final BreakST st) {
        st.setCode("main", "return \"break\"\n");
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitContinue(final ContinueST st) {
        st.setCode("main", "return\n");
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitWhile(final WhileST st) {
        final StringBuilder sb = new StringBuilder();

        final int id = ++this.counter;

        sb.append("local _loop_");
        sb.append(st.scope.loopDepth);
        sb.append("_return_value\n");

        sb.append("local _wcond");
        sb.append(id);
        sb.append("\n");

        sb.append("local _wcondu");
        sb.append(id);
        sb.append(" = function()\n");
        sb.append("_wcond");
        sb.append(id);
        sb.append(" = ");
        sb.append(st.condition.code("main"));
        sb.append("\n");
        sb.append("end\n");

        sb.append("local _wc");
        sb.append(id);
        sb.append(" = function()\n");
        sb.append(st.code.code("main"));
        sb.append("end\n");

        sb.append("_wcondu");
        sb.append(id);
        sb.append("()\n");

        sb.append("while _wcond");
        sb.append(id);
        sb.append(" do\n");

        sb.append("local _wcr");
        sb.append(id);
        sb.append(" = _wc");
        sb.append(id);
        sb.append("()\n");

        sb.append("if not _wcond");
        sb.append(id);
        sb.append(" or _wcr");
        sb.append(id);
        sb.append(" == \"break\" then break end\n");

        sb.append("_wcondu");
        sb.append(id);
        sb.append("()\n");

        sb.append("end\n");

        sb.append("if _loop_");
        sb.append(st.scope.loopDepth);
        sb.append("_return_value then\n");
        if(st.function.hasDefer()) {
            sb.append("_run_defers");
            sb.append(st.scope.functionDepth);
            sb.append("()\n");
        }
        sb.append("return _loop_");
        sb.append(st.scope.loopDepth);
        sb.append("_return_value\n");
        sb.append("end\n");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitDo(final DoST st) {
        final StringBuilder sb = new StringBuilder();

        final int id = ++this.counter;

        sb.append("local _loop_");
        sb.append(st.scope.loopDepth);
        sb.append("_return_value\n");

        sb.append("local _wcond");
        sb.append(id);
        sb.append("\n");

        sb.append("local _wcondu");
        sb.append(id);
        sb.append(" = function()\n");
        sb.append("_wcond");
        sb.append(id);
        sb.append(" = ");
        sb.append(st.condition.code("main"));
        sb.append("\n");
        sb.append("end\n");

        sb.append("local _wc");
        sb.append(id);
        sb.append(" = function()\n");
        sb.append(st.code.code("main"));
        sb.append("end\n");

        sb.append("_wcond");
        sb.append(id);
        sb.append("=true\n");

        sb.append("while _wcond");
        sb.append(id);
        sb.append(" do\n");

        sb.append("local _wcr");
        sb.append(id);
        sb.append(" = _wc");
        sb.append(id);
        sb.append("()\n");

        sb.append("if not _wcond");
        sb.append(id);
        sb.append(" or _wcr");
        sb.append(id);
        sb.append(" == \"break\" then break end\n");

        sb.append("_wcondu");
        sb.append(id);
        sb.append("()\n");

        sb.append("end\n");

        sb.append("if _loop_");
        sb.append(st.scope.loopDepth);
        sb.append("_return_value then\n");
        if(st.function.hasDefer()) {
            sb.append("_run_defers");
            sb.append(st.scope.functionDepth);
            sb.append("()\n");
        }
        sb.append("return _loop_");
        sb.append(st.scope.loopDepth);
        sb.append("_return_value\n");
        sb.append("end\n");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitFor(final ForST st) {
        final StringBuilder sb = new StringBuilder();

        final int id = ++this.counter;

        sb.append("local _loop_");
        sb.append(st.scope.loopDepth);
        sb.append("_return_value\n");

        if(st.condition != null) {
            sb.append("local _fcond");
            sb.append(id);
            sb.append("\n");
        }

        sb.append(st.init.code("main"));

        if(st.condition != null) {
            sb.append("local _fcondu");
            sb.append(id);
            sb.append(" = function()\n");
            sb.append("_fcond");
            sb.append(id);
            sb.append(" = ");
            sb.append(st.condition.code("main"));
            sb.append("\n");
            sb.append("end\n");
        }

        sb.append("local _fafterthought");
        sb.append(id);
        sb.append(" = function()\n");
        sb.append(st.afterthought.code("main"));
        sb.append("end\n");

        sb.append("local _fc");
        sb.append(id);
        sb.append(" = function()\n");
        sb.append(st.code.code("main"));
        sb.append("end\n");

        if(st.condition != null) {
            sb.append("_fcondu");
            sb.append(id);
            sb.append("()\n");
        }

        if(st.condition != null) {
            sb.append("while _fcond");
            sb.append(id);
            sb.append(" do\n");
        } else {
            sb.append("while true do\n");
        }

        sb.append("local _fcr");
        sb.append(id);
        sb.append(" = _fc");
        sb.append(id);
        sb.append("()\n");

        if(st.condition != null) {
            sb.append("if not _fcond");
            sb.append(id);
            sb.append(" or _fcr");
            sb.append(id);
            sb.append(" == \"break\" then break end\n");
        } else {
            sb.append("if _fcr");
            sb.append(id);
            sb.append(" == \"break\" then break end\n");
        }

        sb.append("_fafterthought");
        sb.append(id);
        sb.append("()\n");

        if(st.condition != null) {
            sb.append("_fcondu");
            sb.append(id);
            sb.append("()\n");
        }

        sb.append("end\n");

        if(st.scope.loopDepth <= 1) {
            sb.append("if _loop_");
            sb.append(st.scope.loopDepth);
            sb.append("_return_value then\n");
            if(st.function.hasDefer()) {
                sb.append("_run_defers");
                sb.append(st.scope.functionDepth);
                sb.append("()\n");
            }
            sb.append("return _loop_");
            sb.append(st.scope.loopDepth);
            sb.append("_return_value\n");
            sb.append("end\n");
        } else {
            sb.append("if _loop_");
            sb.append(st.scope.loopDepth);
            sb.append("_return_value then\n");
            sb.append("_loop_");
            sb.append(st.scope.loopDepth - 1);
            sb.append("_return_value=");
            sb.append("_loop_");
            sb.append(st.scope.loopDepth);
            sb.append("_return_value\n");
            sb.append("return \"break\"\n");
            sb.append("end\n");
        }

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitIf(final IfST st) {
        final StringBuilder sb = new StringBuilder();

        sb.append("if ");
        sb.append(st.condition.code("main"));
        sb.append(" then\n");

        sb.append(st.success.code("main"));

        if(st.failure != null) {
            sb.append("else\n");
            sb.append(st.failure.code("main"));
        }

        sb.append("end\n");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitReturn(final ReturnST st) {
        final StringBuilder sb = new StringBuilder();

        if(st.value != null) {
            if(st.scope.loopDepth > 0) {
                sb.append("_loop_");
                sb.append(st.scope.loopDepth);
                sb.append("_return_value = ");
                sb.append(st.value.code("main"));
                sb.append("\n");

                sb.append("return \"break\"\n");
            } else {
                if(st.function.hasDefer()) {
                    sb.append("local _rvalf");
                    sb.append(st.scope.functionDepth);
                    sb.append("=");
                    sb.append(st.value.code("main"));
                    sb.append("\n");

                    sb.append("_run_defers");
                    sb.append(st.scope.functionDepth);
                    sb.append("()\n");

                    sb.append("return _rvalf");
                    sb.append(st.scope.functionDepth);
                    sb.append("\n");
                } else {
                    sb.append("return ");
                    sb.append(st.value.code("main"));
                    sb.append("\n");
                }
            }
        } else {
            if(st.scope.loopDepth > 0) {
                sb.append("_loop_");
                sb.append(st.scope.loopDepth);
                sb.append("_return_value = nil\n");
                sb.append("return \"break\"\n");
            } else {
                if(st.function.hasDefer()) {
                    sb.append("_run_defers");
                    sb.append(st.scope.functionDepth);
                    sb.append("()\n");
                }

                sb.append("return\n");
            }
        }

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitDefer(final DeferST st) {
        final StringBuilder sb = new StringBuilder();

        sb.append("_defer");
        sb.append(st.scope.functionDepth);
        sb.append("(function()\n");

        sb.append(st.code.code("main"));

        sb.append("end)\n");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitParameter(final ParameterST st) {
        st.name.accept(this); // TODO(sci4me) this is kind of hacky

        st.setCode("main", st.name.code("main"));
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitFunctionType(final FunctionTypeST st) {
        final StringBuilder sb = new StringBuilder();

        sb.append("function(");
        for(int i = 0; i < st.parameters.length; i++) {
            sb.append(st.parameters[i].code("main"));

            if(i < st.parameters.length - 1) {
                sb.append(",");
            }
        }
        sb.append(")");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitBlock(final BlockST st) {
        final StringBuilder sb = new StringBuilder();

        final List<StatementST> statements = st.getStatements();

        sb.append("do\n");
        for(int i = 0; i < statements.size(); i++) {
            sb.append(statements.get(i).code("main"));
        }

        sb.append("end\n");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitFunction(final FunctionST st) {
        if(st.type.foreign) {
            st.setCode("main", "");
            this.compiler.notify(st, WaitType.CODE_GENERATE);
            return;
        }

        final StringBuilder sb = new StringBuilder();

        sb.append(st.type.code("main"));
        sb.append("\n");

        if(st.hasDefer()) {
            sb.append("local _defer_table_");
            sb.append(st.scope.functionDepth);
            sb.append(" = {}\n");

            sb.append("local function _defer");
            sb.append(st.scope.functionDepth);
            sb.append("(_dfn");
            sb.append(st.scope.functionDepth);
            sb.append(")\n");
            sb.append("table.insert(_defer_table_");
            sb.append(st.scope.functionDepth);
            sb.append(",_dfn");
            sb.append(st.scope.functionDepth);
            sb.append(")\n");
            sb.append("end\n");

            sb.append("local function _run_defers");
            sb.append(st.scope.functionDepth);
            sb.append("()\n");
            sb.append("for i=1,#_defer_table_");
            sb.append(st.scope.functionDepth);
            sb.append(" do\n");
            sb.append("_defer_table_");
            sb.append(st.scope.functionDepth);
            sb.append("[i]()\n");
            sb.append("end\n");
            sb.append("end\n");
        }

        sb.append(st.code.code("main"));

        if(st.hasDefer()) {
            sb.append("_run_defers");
            sb.append(st.scope.functionDepth);
            sb.append("()\n");
        }

        sb.append("end");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitExpressionStatement(final ExpressionStatementST st) {
        st.setCode("main", st.expr.code("main") + "\n");
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void visitConstDecl(final ConstDeclST st) {
        // TODO(sci4me): make this less hacky
        if((st.value instanceof FunctionST && ((FunctionST) st.value).type.foreign) || st.value instanceof TypeST) {
            st.setCode("preamble", "");
            st.setCode("main_separate", "");
            st.setCode("main", "");
            this.compiler.notify(st, WaitType.CODE_GENERATE);
            return;
        }

        st.setCode("preamble", "local " + st.name.code("main") + "\n");
        st.setCode("main_separate", st.name.code("main") + " = " + st.value.code("main") + "\n");
        st.setCode("main", "local " + st.name.code("main") + "\n" + st.name.code("main") + " = " + st.value.code("main") + "\n");
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitAssignDecl(final AssignDeclST st) {
        st.setCode("preamble", "local " + st.name.code("main") + "\n");
        st.setCode("main_separate", st.name.code("main") + " = " + st.value.code("main") + "\n");
        st.setCode("main", "local " + st.name.code("main") + "\n" + st.name.code("main") + " = " + st.value.code("main") + "\n");
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitVariableDecl(final VariableDeclST st) {
        final StringBuilder sb = new StringBuilder();

        sb.append("local ");
        sb.append(st.name.code("main"));
        sb.append(" = ");

        if(st.defaultValue == null) {
            final int type = st.type.type();
            if(type == Types.INT || type == Types.FLOAT) {
                sb.append("0");
            } else if(type == Types.BOOL) {
                sb.append("false");
            } else if(type == Types.STRING) {
                sb.append("\"\"");
            } else {
                final TypeST typeST = Types.getTypeST(type);
                if(typeST instanceof StructTypeST) {
                    sb.append("_struct");
                    sb.append(((StructTypeST) typeST).id);
                    sb.append("_init()");
                } else {
                    sb.append("nil");
                }
            }
        } else {
            sb.append(st.defaultValue.code("main"));
        }

        sb.append("\n");

        st.setCode("main", sb.toString());
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitStructType(final StructTypeST st) {
        final StringBuilder sb = new StringBuilder();

        sb.append("_struct");
        sb.append(st.id);
        sb.append("_init = function()\n");

        sb.append("local r = {\n");

        int i = 0;
        for(final Map.Entry<String, TypeST> field : st.getFields().entrySet()) {
            sb.append(field.getKey());
            sb.append("=");
            
            final int type = field.getValue().type();
            if(type == Types.UNKNOWN) {
                this.waitOn(field.getValue(), WaitType.TYPE_DEDUCE);
                return;
            }

            if(type == Types.INT || type == Types.FLOAT) {
                sb.append("0");
            } else if(type == Types.BOOL) {
                sb.append("false");
            } else if(type == Types.STRING) {
                sb.append("\"\"");
            } else {
                final TypeST typeST = Types.getTypeST(type);
                if(typeST instanceof StructTypeST) {
                    sb.append("_struct");
                    sb.append(((StructTypeST) typeST).id);
                    sb.append("_init()");
                } else {
                    sb.append("nil");
                }
            }

            i++;
            if(i < st.getFields().size()) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("}\n");

        sb.append("setmetatable(r, {\n");
        sb.append("__tostring = function()\n");

        sb.append("return \"{ \" .. ");
        
        i = 0;
        for(final Map.Entry<String, TypeST> field : st.getFields().entrySet()) {
            sb.append("\"");
            sb.append(field.getKey());
            sb.append(" = \" .. tostring(r.");
            sb.append(field.getKey());
            sb.append(")");

            i++;
            if(i < st.getFields().size()) {
                sb.append(" .. \", \" .. ");
            }
        }
        sb.append(".. \" }\"\n");

        sb.append("end\n");
        sb.append("})\n");

        sb.append("return r\n");

        sb.append("end\n");

        st.setCode("initializer", sb.toString());
        st.setCode("initializer_preamble", "local _struct" + st.id + "_init\n");
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override
    public void visitEmpty(final EmptyST st) {
        st.setCode("main", "");
        this.compiler.notify(st, WaitType.CODE_GENERATE);
    }

    @Override 
    public void finish() {

    }
}