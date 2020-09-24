package com.sci.valerie.compiler;

import java.io.*;
import java.util.*;

import com.sci.valerie.util.*;
import com.sci.valerie.lexer.*;
import com.sci.valerie.parser.*;

import com.sci.valerie.parser.tree.*;
import com.sci.valerie.parser.tree.expr.*;
import com.sci.valerie.parser.tree.stmt.*;
import com.sci.valerie.parser.tree.stmt.decl.*;
import com.sci.valerie.parser.util.*;

public final class CompileManager {
    private List<Worker> runQueue;
    private Map<Node, Map<WaitType, List<Worker>>> waiters;

    private Map<String, FileST> files;

    private Set<FileST> compiledFiles;

    private Set<Node> declarations;

    public final String globalHeader;

    public CompileManager() {
        this.runQueue = new ArrayList<>();
        this.waiters = new HashMap<>();

        this.files = new HashMap<>();

        this.compiledFiles = new HashSet<>();

        this.declarations = new HashSet<>();

        try { 
            this.globalHeader = FileUtils.readFile(CompileManager.class.getResourceAsStream("/global_header.lua"));
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addFile(final String path, final FileST ast) {
        this.files.put(path, ast);
    }

    public void compileFile(final FileST file) {
        if(this.compiledFiles.contains(file)) {
            return;
        }
        this.compiledFiles.add(file);

        final List<Node> imports = ASTLinearizer.linearize(file, n -> n instanceof ImportST);
        imports.forEach(i -> {
            final ImportST im = (ImportST) i;
            this.importFile(file, im.name.string);
        });

        final List<Node> declarations = ASTLinearizer.linearize(file, n -> {
            if(n.scope != file.scope) {
                return false;
            }

            return n instanceof ConstDeclST || n instanceof AssignDeclST || n instanceof VariableDeclST;
        });

        final List<Node> runs = ASTLinearizer.linearize(file, n -> {
            if(n.scope != file.scope) {
                return false;
            }

            return n instanceof UnaryST && ((UnaryST) n).op == UnaryOP.RUN;
        });
        
        declarations.forEach(decl -> {
            if(!this.declarations.contains(decl)) {
                this.declarations.add(decl);
            }

            final NameBinder binder = new NameBinder(this, ASTLinearizer.linearizeAsArray(decl));
            final WorkStatus status = binder.work();
            if(!status.finished) {
                throw new AssertionError();
            }
            binder.finish();
        });

        runs.forEach(run -> {
            final NameBinder binder = new NameBinder(this, ASTLinearizer.linearizeAsArray(run));
            final WorkStatus status = binder.work();
            if(!status.finished) {
                throw new AssertionError();
            }
            binder.finish();
        });
    }

    private void importFile(final FileST file, final String name) {
        final FileST importedFile = this.resolveImport(file, name);
        
        this.compileFile(importedFile);

        for(final Map.Entry<String, Node> binding : importedFile.scope.getBindings().entrySet()) {
            if(file.scope.hasBinding(binding.getKey())) {
                if(file.scope.lookup(binding.getKey()) == binding.getValue()) {
                    continue;
                }
            }

            file.scope.bind(binding.getKey(), binding.getValue(), importedFile.scope.findDeclarer(binding.getValue()), importedFile.scope.isConstant(binding.getKey()));
        }
    }

    private FileST resolveImport(final FileST file, final String name) {
        try {
            final File importingFileParent = file.file.getParentFile();
            final File importedFile = new File(importingFileParent, name + ".valerie");
            final String ap = importedFile.getCanonicalPath();

            if(this.files.containsKey(ap)) {
                return this.files.get(ap);
            }

            if(!importedFile.exists()) {
                throw new RuntimeException("Failed to resolve import: " + name);
            }
        
            final Lexer lexer = new Lexer(importedFile);
            final Parser parser = new Parser(importedFile, lexer.tokenize());
            final FileST ast = parser.parse();
            this.files.put(ap, ast);
            return ast;
        } catch(final Throwable t) {
            t.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private void waitOn(final Node node, final WaitType waitType, final Worker waiter) {
        if(!this.waiters.containsKey(node)) {
            this.waiters.put(node, new HashMap<>());
        }

        final Map<WaitType, List<Worker>> waitersByWaitType = this.waiters.get(node);

        if(!waitersByWaitType.containsKey(waitType)) {
            waitersByWaitType.put(waitType, new ArrayList<>());
        }

        final List<Worker> waiters = waitersByWaitType.get(waitType);
        waiters.add(waiter);
    }

    public void notify(final Node node, final WaitType waitType) {
        if(!this.waiters.containsKey(node)) {
            return;
        }

        final Map<WaitType, List<Worker>> waitersByWaitType = this.waiters.get(node);
        if(!waitersByWaitType.containsKey(waitType)) {
            return;
        }

        final List<Worker> waiters = waitersByWaitType.get(waitType);
        for(final Worker waiter : waiters) {
            waiter.notify(node, waitType);
            this.queueWorker(waiter);
        }
        waiters.clear();
    }

    public void queueWorker(final Worker worker) {
        this.runQueue.add(worker);
    }

    public void process() {
        while(!this.runQueue.isEmpty()) {
            final Worker worker = this.runQueue.remove(0);

            final WorkStatus status = worker.work();     
            if(status.finished) {
                worker.finish();
            } else {
                this.waitOn(status.waitingOn, status.waitType, worker);
            }
        }

        for(final Map.Entry<Node, Map<WaitType, List<Worker>>> e1 : this.waiters.entrySet()) {
            for(final Map.Entry<WaitType, List<Worker>> e2 : e1.getValue().entrySet()) {
                if(!e2.getValue().isEmpty()) {
                    System.out.println("WaitType " + e2.getKey() + " for node " + e1.getKey() + " is not empty");
                    System.out.println("Waiters:");
                    e2.getValue().forEach(e -> System.out.println("    " + e));
                    System.out.println();
                }
            }
        }

        this.finish();
    }

    private FunctionST findMain() {
        for(final FileST file : this.files.values()) {
            final Node main = file.scope.lookup("main");
            
            if(main == null) {
                continue;
            }

            if(main instanceof FunctionST) {
                return (FunctionST) main;
            }
        }

        return null;
    }

    private void finish() {
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(new File("out.lua")));

            writer.write(this.globalHeader);
            writer.newLine();

            final FunctionST main = this.findMain();
            if(main == null) {
                this.declarations.forEach(decl -> {
                    try { 
                        final String code = decl.code("preamble");
                        if(code != null) {
                            writer.write(code);
                        }
                    } catch(final IOException e) { 
                    }
                });

                this.declarations.forEach(decl -> {
                    final Node node;
                    if(decl instanceof ConstDeclST) {
                        node = ((ConstDeclST) decl).value;
                    } else {
                        return;
                    }

                    try { 
                        final String code = node.code("initializer_preamble");
                        if(code != null) {
                            writer.write(code);
                        }
                    } catch(final IOException e) { 
                    }
                });

                this.declarations.forEach(decl -> {
                    final Node node;
                    if(decl instanceof ConstDeclST) {
                        node = ((ConstDeclST) decl).value;
                    } else {
                        return;
                    }

                    try { 
                        final String code = node.code("initializer");
                        if(code != null) {
                            writer.write(code);
                        }
                    } catch(final IOException e) { 
                    }
                });

                this.declarations.forEach(decl -> {
                    try { 
                        final String code = decl.code("main_separate");
                        if(code != null) {
                            writer.write(code);
                        }
                    } catch(final IOException e) { 
                    }
                });

                try { 
                    writer.write("return {\n");

                    int i = 0;
                    for(final Node decl : this.declarations) {
                        final String name;
                        if(decl instanceof ConstDeclST) {
                            name = ((ConstDeclST) decl).name.ident;
                        } else if(decl instanceof AssignDeclST) {
                            name = ((AssignDeclST) decl).name.ident;
                        } else if(decl instanceof VariableDeclST) {
                            name = ((VariableDeclST) decl).name.ident;
                        } else {
                            throw new AssertionError();
                        }
                       
                        writer.write("[\"");
                        writer.write(name);
                        writer.write("\"]=");
                        writer.write(name);

                        i++;
                        if(i < this.declarations.size()) {
                            writer.write(",");
                        }
                        
                        writer.write("\n");
                    }

                    writer.write("}\n");
                } catch(final IOException e) { 
                    e.printStackTrace();
                }
            } else {
                final Node mainDecl = main.scope.findDeclarer(main);

                final List<Node> dependencies = DependencyComputer.computeDependencies(main);

                dependencies.forEach(decl -> {
                    try { 
                        final String code = decl.code("preamble");
                        if(code != null) {
                            writer.write(code);
                        }
                    } catch(final IOException e) { 
                    }
                });

                this.declarations.forEach(decl -> {
                    final Node node;
                    if(decl instanceof ConstDeclST) {
                        node = ((ConstDeclST) decl).value;
                    } else {
                        return;
                    }

                    try { 
                        final String code = node.code("initializer_preamble");
                        if(code != null) {
                            writer.write(code);
                        }
                    } catch(final IOException e) { 
                    }
                });

                this.declarations.forEach(decl -> {
                    final Node node;
                    if(decl instanceof ConstDeclST) {
                        node = ((ConstDeclST) decl).value;
                    } else {
                        return;
                    }

                    try { 
                        final String code = node.code("initializer");
                        if(code != null) {
                            writer.write(code);
                        }
                    } catch(final IOException e) { 
                    }
                });

                dependencies.forEach(decl -> {
                    try { 
                        final String code = decl.code("main_separate");
                        if(code != null) {
                            writer.write(code);
                        }
                    } catch(final IOException e) { 
                    }
                });

                writer.write(mainDecl.code("main"));

                writer.write("main()");
            }

            writer.close();
        } catch(final IOException e) {
            e.printStackTrace();
        }
    }
}