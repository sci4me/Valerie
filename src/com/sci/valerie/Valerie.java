package com.sci.valerie;

import java.io.*;
import java.util.*;

import com.sci.valerie.lexer.*;
import com.sci.valerie.parser.*;
import com.sci.valerie.parser.tree.*;
import com.sci.valerie.compiler.*;

public final class Valerie {
    public static void main(final String[] args) throws Throwable {
        if(args.length != 1) {
            System.out.println("Usage: valerie <file>");
            return;
        }

        final File file = new File(args[0]);

        if(!file.exists() || file.isDirectory()) {
            System.out.println("Can not read source file '" + args[0] + "'");
            return;    
        }

        final Lexer lexer = new Lexer(file);
        final Parser parser = new Parser(file, lexer.tokenize());

        final CompileManager compiler = new CompileManager();
        
        final FileST ast = parser.parse();
        compiler.addFile(ast.file.getCanonicalPath(), ast);
        compiler.compileFile(ast);

        compiler.process();
    }

    private Valerie() {

    }
}