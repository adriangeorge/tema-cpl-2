package cool.compiler;

import cool.structures.*;

public class Visitor_Definition implements ASTVisitor<Void> {
    Scope__Base currentScope = null;
    boolean forward_declaration_disabled = false;

    // Program
    @Override
    public Void visit(Program a) {
        // Global scope setup in compiler class class all we have to do here is
        // to set the current scope to the global scope
        currentScope = SymbolTable.globals;
        // Visit all class defs
        for (var c : a.classes) {
            c.accept(this);
        }
        return null;
    }

    // Classes Definition
    @Override
    public Void visit(ClassDef a) {
        String inheritance = null;
        if (a.inherit.tk != null) {
            inheritance = a.inherit.getText();
        }
        var symbol = new Symbol_Type(a.type.getText(), inheritance, currentScope);

        if (symbol.getName().equals("SELF_TYPE")) {
            SymbolTable.error(a.ctx, a.type.tk,
                    "Class has illegal name SELF_TYPE");
            return null;
        }

        // Class already defined
        if (!currentScope.add(symbol)) {
            SymbolTable.error(a.ctx, a.type.tk,
                    "Class "
                            + a.type.getText()
                            + " is redefined");
            return null;
        }

        // Check if parent is illegal
        if (a.inherit.tk != null) {
            switch (a.inherit.getText()) {
                case "Int":
                case "String":
                case "Bool":
                case "SELF_TYPE": {
                    SymbolTable.error(a.ctx, a.inherit.tk,
                            "Class "
                                    + a.type.getText()
                                    + " has illegal parent "
                                    + a.inherit.getText());
                    return null;
                }
            }
        }

        // Enter class scope
        currentScope = symbol;

        for (var f : a.features) {
            f.accept(this);
        }
        currentScope = symbol.getParentScope();
        return null;
    }

    // Class features
    @Override
    public Void visit(VarDef a) {
        // Set current scope as class scope
        var cs = (Symbol_Type) currentScope;
        var symbol = new Symbol_Identifier(a.id.getText(), a.type.getText());
        a.id.scope = cs;
        // Check if attribute is already defined in class
        if (!cs.add_attribute(symbol)) {
            SymbolTable.error(a.ctx, a.id.tk,
                    "Class "
                            + cs.getName()
                            + " redefines attribute "
                            + a.id.getText());
            return null;
        }

        a.id.symbol = symbol;
        if (a.init != null) {
            a.init.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FuncDef a) {
        // Set current scope as class scope
        var cs = (Symbol_Type) currentScope;
        var symbol = new Symbol_Method(a.id.getText(), a.type.getText(), cs);
        a.id.scope = symbol;
        // Check if method is already defined in class
        if (!cs.add_method(symbol)) {
            SymbolTable.error(a.ctx, a.id.tk,
                    "Class "
                            + cs.getName()
                            + " redefines method "
                            + a.id.getText());
            return null;
        }

        currentScope = symbol;
        for (var f : a.formal_list) {
            f.accept(this);
        }
        if (a.body != null) {
            a.body.accept(this);
        }
        currentScope = symbol.getParentScope();
        return null;
    }

    // Function features
    @Override
    public Void visit(Formal a) {
        var cs = (Symbol_Method) currentScope;
        var parent_scope = (Symbol_Type) cs.getParentScope();
        var symbol = new Symbol_Identifier(a.id.getText(), a.type.getText());
        a.id.scope = cs;
        // Check if attribute is already defined in class
        if (!currentScope.add(symbol)) {
            SymbolTable.error(a.ctx, a.id.tk,
                    "Method "
                            + cs.name
                            + " of class "
                            + parent_scope.getName()
                            + " redefines formal parameter "
                            + a.id.getText());
            return null;
        }
        a.id.symbol = symbol;
        return null;
    }

    // Let
    @Override
    public Void visit(Let a) {
        var let_scope = new Scope_Default(currentScope);

        currentScope = let_scope;
        forward_declaration_disabled = true;
        for (var l : a.let_list) {
            l.accept(this);
        }

        a.expr.accept(this);
        currentScope = let_scope.getParentScope();
        forward_declaration_disabled = false;
        return null;
    }

    @Override
    public Void visit(LocalVar a) {
        var symbol = new Symbol_Identifier(a.id.getText(), a.type.getText());
        a.id.scope = currentScope;
        if (a.init != null) {
            a.init.accept(this);
        }

        ((Scope_Default) currentScope).add_replace(symbol);
        return null;
    }

    // Class operations
    @Override
    public Void visit(NewInstance a) {

        return null;
    }

    @Override
    public Void visit(OopDispatch a) {

        a.left.accept(this);

        if (a.parent_type != null) {

        }

        for (var p : a.param_list) {
            p.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(VoidCheck a) {

        a.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(FuncCall a) {

        for (var p : a.param_list) {
            p.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ObjId a) {
        // System.out.println(a.getText());
        var symbol = currentScope.lookup_attribute(a.getText());
        a.scope = currentScope;

        if (a.tk.getText().equals("self")) {
            return null;
        }

        if (symbol == null && forward_declaration_disabled) {
            SymbolTable.error(a.ctx, a.tk,
                    "Undefined identifier "
                            + a.tk.getText());
            return null;
        }
        a.symbol = (Symbol_Identifier) symbol;

        return null;
    }

    @Override
    public Void visit(WhileLoop a) {

        a.cond_expr.accept(this);
        a.instr_expr.accept(this);
        return null;
    }

    // Block
    @Override
    public Void visit(Block a) {

        for (var e : a.expr_list) {
            e.accept(this);
        }
        return null;
    }

    // Misc
    @Override
    public Void visit(SimpleAssign a) {
        a.id.scope = currentScope;
        a.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(ClIf a) {

        a.cond.accept(this);
        a.then_expr.accept(this);
        a.else_expr.accept(this);
        return null;
    }

    // Constants
    @Override
    public Void visit(ClInteger a) {

        return null;
    }

    @Override
    public Void visit(ClString a) {
        return null;
    }

    @Override
    public Void visit(BoolTrue a) {
        return null;
    }

    @Override
    public Void visit(BoolFalse a) {

        return null;
    }

    // Case
    @Override
    public Void visit(Case a) {
        var case_scope = new Scope_Default(currentScope);

        currentScope = case_scope;
        a.init.accept(this);

        for (var c : a.cases) {

            c.accept(this);
        }
        currentScope = currentScope.getParentScope();
        return null;
    }

    @Override
    public Void visit(CaseBranch a) {

        a.expr.accept(this);

        return null;
    }

    // Arithmetic
    @Override
    public Void visit(ParenExpr a) {
        a.expression.accept(this);
        return null;
    }

    @Override
    public Void visit(NotExpr a) {

        a.operand.accept(this);

        return null;
    }

    @Override
    public Void visit(Equality a) {

        a.left.accept(this);
        a.right.accept(this);

        return null;
    }

    @Override
    public Void visit(LessThanEqual a) {

        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Void visit(LessThan a) {

        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Void visit(ComplExpr a) {

        a.operand.accept(this);
        return null;
    }

    @Override
    public Void visit(Division a) {

        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Multiplication a) {

        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Subtraction a) {

        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Addition a) {

        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

}
