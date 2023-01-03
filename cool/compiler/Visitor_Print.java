package cool.compiler;

public class Visitor_Print implements ASTVisitor<Void> {
    @Override
    public Void visit(ClassDef a) {
        a.print_AST("class");
        a.print_AST(a.type, 1);

        if (a.inherit.tk != null)
            a.print_AST(a.inherit, 1);

        for (var f : a.features) {
            f.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Program a) {
        a.print_AST("program");

        // Visit all class defs
        for (var c : a.classes) {
            c.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FuncDef a) {
        a.print_AST("method");
        a.print_AST(a.id, 1);

        for (var f : a.formal_list) {
            f.accept(this);
        }

        a.print_AST(a.type, 1);

        if (a.body != null) {
            a.body.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(VarDef a) {
        a.print_AST("attribute");
        a.print_AST(a.id, 1);
        a.print_AST(a.type, 1);

        if (a.init != null) {
            a.init.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Formal a) {
        a.print_AST("formal");
        a.print_AST(a.id, 1);
        a.print_AST(a.type, 1);

        return null;
    }

    @Override
    public Void visit(ClInteger a) {
        a.print_AST(a.val);
        return null;
    }

    @Override
    public Void visit(NewInstance a) {
        a.print_AST("new");
        a.print_AST(a.type, 1);

        return null;
    }

    @Override
    public Void visit(OopDispatch a) {
        a.print_AST(".");
        a.left.accept(this);

        if (a.parent_type != null) {
            a.print_AST(a.parent_type, 1);
        }
        a.print_AST(a.method_id, 1);
        for (var p : a.param_list) {
            p.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(VoidCheck a) {
        a.print_AST("isvoid");
        a.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(FuncCall a) {
        a.print_AST("implicit dispatch");
        a.print_AST(a.id, 1);

        for (var p : a.param_list) {
            p.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ObjId a) {
        a.print_AST(a.tk);
        return null;
    }

    @Override
    public Void visit(ParenExpr a) {
        a.expression.accept(this);
        return null;
    }

    @Override
    public Void visit(NotExpr a) {
        a.print_AST("not");
        a.operand.accept(this);

        return null;
    }

    @Override
    public Void visit(Equality a) {
        a.print_AST("=");
        a.left.accept(this);
        a.right.accept(this);

        return null;
    }

    @Override
    public Void visit(LessThanEqual a) {
        a.print_AST("<=");
        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Void visit(LessThan a) {
        a.print_AST("<");
        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Void visit(ComplExpr a) {
        a.print_AST("~");
        a.operand.accept(this);
        return null;
    }

    @Override
    public Void visit(Division a) {
        a.print_AST("/");
        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Multiplication a) {
        a.print_AST("*");
        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Subtraction a) {
        a.print_AST("-");
        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Addition a) {
        a.print_AST("+");
        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Case a) {
        a.print_AST("case");
        a.init.accept(this);

        for (var c : a.cases) {

            c.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(CaseBranch a) {
        a.print_AST("case branch");
        a.print_AST(a.id, 1);
        a.print_AST(a.type, 1);
        a.expr.accept(this);

        return null;
    }

    @Override
    public Void visit(Block a) {
        a.print_AST("block");

        for (var e : a.expr_list) {
            e.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(WhileLoop a) {
        a.print_AST("while");

        a.cond_expr.accept(this);
        a.instr_expr.accept(this);
        return null;
    }

    @Override
    public Void visit(BoolTrue a) {
        a.print_AST(a.val);
        return null;
    }

    @Override
    public Void visit(ClString a) {
        a.print_AST(a.val);
        return null;
    }

    @Override
    public Void visit(BoolFalse a) {
        a.print_AST(a.val);
        return null;
    }

    @Override
    public Void visit(SimpleAssign a) {
        a.print_AST("<-");
        a.print_AST(a.id, 1);
        a.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(ClIf a) {
        a.print_AST("if");

        a.cond.accept(this);
        a.then_expr.accept(this);
        a.else_expr.accept(this);
        return null;
    }

    @Override
    public Void visit(Let a) {
        a.print_AST("let");

        for (var l : a.let_list) {
            l.accept(this);
        }

        a.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(LocalVar a) {
        a.print_AST("local");
        a.print_AST(a.id, 1);
        a.print_AST(a.type, 1);

        if (a.init != null) {
            a.init.accept(this);
        }
        return null;
    }
}
