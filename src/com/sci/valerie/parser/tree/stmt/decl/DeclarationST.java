package com.sci.valerie.parser.tree.stmt.decl;

import com.sci.valerie.type.*;
import com.sci.valerie.parser.tree.stmt.*;

public abstract class DeclarationST extends StatementST {
    public DeclarationST(final Scope scope) {
        super(scope);
    }    
}