package cool.compiler;

import java.util.ArrayList;
import cool.parser.CoolParser;
import cool.parser.CoolParserBaseVisitor;

/**
 * CoolParserVisitor
 */
public class Visitor_Parser extends CoolParserBaseVisitor<ASTNode> {
    int parentheses_offset = 0;

    // VISIT PROGRAM
    @Override
    public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
        ArrayList<ClassDef> class_list = new ArrayList<ClassDef>();
        // var rep = ast_representation("program", ctx.depth());
        for (var c : ctx.cl_class()) {
            class_list.add((ClassDef) visitClassDef((CoolParser.ClassDefContext) c));
        }

        // Set depth and return object
        var o = new Program(class_list);

        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    // VISIT DEFINITIONS
    @Override
    public ASTNode visitClassDef(CoolParser.ClassDefContext ctx) {
        // Visit all class features
        ArrayList<Feature> feature_list = new ArrayList<Feature>();
        for (var f : ctx.feat_list) {
            feature_list.add((Feature) visit(f));
        }

        // Set depth and return object
        var o = new ClassDef(new ObjId(ctx.type), new ObjId(ctx.inherit), feature_list);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitFormal(CoolParser.FormalContext ctx) {
        // Set depth and return object
        var o = new Formal(new ObjId(ctx.id), ctx.type);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitFuncDef(CoolParser.FuncDefContext ctx) {
        // Visit all method formals
        ArrayList<Formal> formal_list = new ArrayList<Formal>();
        for (var f : ctx.formal_list) {
            formal_list.add((Formal) visit(f));
        }

        // Check if function has body and visit it
        Expression func_body = null;
        if (ctx.f_body != null) {
            func_body = (Expression) visit(ctx.f_body);
        }

        // Set depth and return object
        var o = new FuncDef(new ObjId(ctx.id), new ObjId(ctx.type), formal_list, func_body);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitVarDef(CoolParser.VarDefContext ctx) {
        // Check if initialisation is being done
        Expression expr = null;
        if (ctx.init != null) {
            expr = (Expression) visit(ctx.init);
        }

        // Set depth and return object
        var o = new VarDef(new ObjId(ctx.id), new ObjId(ctx.type), expr);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    // VISIT EXPRESSIONS
    // Literals
    @Override
    public ASTNode visitClInteger(CoolParser.ClIntegerContext ctx) {
        // Set depth and return object
        var o = new ClInteger(ctx.INTEGER().getSymbol());
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitClString(CoolParser.ClStringContext ctx) {
        // Set depth and return object
        var o = new ClString(ctx.STRING().getSymbol());
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitBoolFalse(CoolParser.BoolFalseContext ctx) {
        // var rep = ast_representation(ctx.FALSE().getText(), ctx.depth());
        // Set depth and return object
        var o = new BoolFalse(ctx.FALSE().getSymbol());
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitBoolTrue(CoolParser.BoolTrueContext ctx) {
        // Set depth and return object
        var o = new BoolTrue(ctx.TRUE().getSymbol());
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitObj_id(CoolParser.Obj_idContext ctx) {
        // Set depth and return object
        var o = new ObjId(ctx.OBJ_ID().getSymbol());
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    // Operations
    @Override
    public ASTNode visitMultiplication(CoolParser.MultiplicationContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);

        // Set depth and return object
        var o = new Multiplication(left, right, ctx.left.start, ctx.right.start);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitDivision(CoolParser.DivisionContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);

        // Set depth and return object
        var o = new Division(left, right, ctx.left.start, ctx.right.start);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitSubtraction(CoolParser.SubtractionContext ctx) {

        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);

        // Set depth and return object
        var o = new Subtraction(left, right, ctx.left.start, ctx.right.start);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitAddition(CoolParser.AdditionContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);

        // Set depth and return object
        var o = new Addition(left, right, ctx.left.start, ctx.right.start);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitParenExpr(CoolParser.ParenExprContext ctx) {
        // Parentheses break up the usage of ctx.depth() a little,
        // but I can compensate by using an offset each time an expression
        // in parantheses is encountered
        parentheses_offset++;
        Expression expression = (Expression) visit(ctx.expr());
        parentheses_offset--;
        var o = new ParenExpr(expression);
        return o;
    }

    @Override
    public ASTNode visitComplExpr(CoolParser.ComplExprContext ctx) {
        Expression operand = (Expression) visit(ctx.expr());

        // Set depth and return object
        var o = new ComplExpr(operand);
        o.tk = new ObjId(ctx.expr().start);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    // Comparison
    @Override
    public ASTNode visitLessThanEqual(CoolParser.LessThanEqualContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);

        // Set depth and return object
        var o = new LessThanEqual(left, right, ctx.left.start, ctx.right.start);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitLessThan(CoolParser.LessThanContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);

        // Set depth and return object
        var o = new LessThan(left, right, ctx.left.start, ctx.right.start);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitNotExpr(CoolParser.NotExprContext ctx) {
        Expression operand = (Expression) visit(ctx.expr());

        // Set depth and return object
        var o = new NotExpr(operand);
        o.ctx = ctx;
        o.tk = new ObjId(ctx.expr().start);
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitEquality(CoolParser.EqualityContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);

        // Set depth and return object
        var o = new Equality(left, right, ctx.left.start, ctx.right.start, ctx.EQUAL().getSymbol());
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    // While
    @Override
    public ASTNode visitWhileLoop(CoolParser.WhileLoopContext ctx) {
        Expression cond_expr = (Expression) visit(ctx.cond_expr);
        Expression instr_expr = (Expression) visit(ctx.instr_expr);

        // Set depth and return object
        var o = new WhileLoop(cond_expr, instr_expr);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    // Class
    @Override
    public ASTNode visitNewInstance(CoolParser.NewInstanceContext ctx) {
        // Set depth and return object
        var o = new NewInstance(new ObjId(ctx.type));
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitOopDispatch(CoolParser.OopDispatchContext ctx) {
        Expression left = (Expression) visit(ctx.left);

        ArrayList<Expression> param_list = new ArrayList<Expression>();
        for (var p : ctx.params) {
            param_list.add((Expression) visit(p));
        }

        // Set depth and return object
        var o = new OopDispatch(left, new ObjId(ctx.p_type), new ObjId(ctx.method_id), param_list);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitVoidCheck(CoolParser.VoidCheckContext ctx) {
        Expression expr = (Expression) visit(ctx.expr());

        // Set depth and return object
        var o = new VoidCheck(expr);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    // Blocks
    @Override
    public ASTNode visitLocalVar(CoolParser.LocalVarContext ctx) {
        // Check if initialisation is being done
        Expression expr = null;
        if (ctx.expr() != null) {
            expr = (Expression) visit(ctx.expr());
        }

        // Set depth and return object
        var o = new LocalVar(new ObjId(ctx.id), new ObjId(ctx.type), expr);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitLet(CoolParser.LetContext ctx) {
        ArrayList<Feature> let_list = new ArrayList<Feature>();
        for (var l : ctx.other_vars) {
            let_list.add((Feature) visit(l));
        }

        Expression expr = (Expression) visit(ctx.expr());

        // Set depth and return object
        var o = new Let(let_list, expr);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitBlock(CoolParser.BlockContext ctx) {
        ArrayList<Expression> expr_list = new ArrayList<Expression>();
        for (var e : ctx.expr_list) {
            expr_list.add((Expression) visit(e));
        }
        // Set depth and return object
        var o = new Block(expr_list);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    // Case
    @Override
    public ASTNode visitCaseBranch(CoolParser.CaseBranchContext ctx) {
        Expression expr = (Expression) visit(ctx.expr());
        // Set depth and return object
        var o = new CaseBranch(expr, new ObjId(ctx.id), new ObjId(ctx.type));
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitCase(CoolParser.CaseContext ctx) {
        Expression init = (Expression) visit(ctx.init);
        ArrayList<CaseBranch> cases = new ArrayList<>();
        for (var l : ctx.cases) {
            cases.add((CaseBranch) visit(l));
        }
        // Set depth and return object
        var o = new Case(init, cases);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitClIf(CoolParser.ClIfContext ctx) {

        Expression cond = (Expression) visit(ctx.cond);
        Expression then_expr = (Expression) visit(ctx.then_expr);
        Expression else_expr = (Expression) visit(ctx.else_expr);

        // Set depth and return object
        var o = new ClIf(cond, then_expr, else_expr);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }
    // Others

    @Override
    public ASTNode visitSimpleAssign(CoolParser.SimpleAssignContext ctx) {
        Expression expr = (Expression) visit(ctx.expr());

        // Set depth and return object
        var o = new SimpleAssign(new ObjId(ctx.id), expr);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }

    @Override
    public ASTNode visitFuncCall(CoolParser.FuncCallContext ctx) {
        // Visit all method params
        ArrayList<Expression> param_list = new ArrayList<Expression>();

        for (var p : ctx.params) {
            param_list.add((Expression) visit(p));
        }
        // Set depth and return object
        var o = new FuncCall(new ObjId(ctx.id), param_list);
        o.ctx = ctx;
        o.setDepth(ctx.depth() - parentheses_offset);
        return o;
    }
}