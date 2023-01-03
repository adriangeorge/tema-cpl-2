package cool.compiler;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import cool.structures.Scope__Base;
import cool.structures.Symbol_Identifier;

// Representation of a node from the AST tree
public abstract class ASTNode {
    ParserRuleContext ctx;
    private int node_depth;

    // Functions used for AST representation in text
    public void setDepth(int node_depth) {
        this.node_depth = node_depth;
    }

    public int getDepth() {
        return node_depth;
    }

    // Print token type
    public void print_AST(Token content) {
        print_AST(content.getText(), 0);
    }

    public void print_AST(Token content, int relative_indent) {
        print_AST(content.getText(), relative_indent);
    }

    // Print ObjId
    public void print_AST(ObjId content) {
        print_AST(content.tk.getText(), 0);
    }

    public void print_AST(ObjId content, int relative_indent) {
        print_AST(content.tk.getText(), relative_indent);
    }

    // Print string type
    public void print_AST(String content) {
        print_AST(content, 0);
    }

    public void print_AST(String content, int relative_indent) {
        String out = "";
        for (int i = 1; i < this.node_depth + relative_indent; i++) {
            out += "  ";
        }
        out += content;
        System.out.println(out);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}

// DEFINITIONS ABSTRACT
abstract class Definition extends ASTNode {
}

class ClassDef extends Definition {

    ObjId type;
    ObjId inherit;
    List<Feature> features;

    public ClassDef(ObjId type, ObjId inherit, List<Feature> features) {
        this.type = type;
        this.features = features;
        this.inherit = inherit;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Formal extends Definition {
    ObjId id;
    Token type;

    Formal(ObjId id, Token type) {
        this.id = id;
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Class Features
class Feature extends Definition {
    ObjId id;
    List<Formal> formal_list;

    Feature(ObjId id, List<Formal> formal_list) {
        this.id = id;
        this.formal_list = formal_list;
    }
}

class FuncDef extends Feature {
    Expression body;
    ObjId type;

    public FuncDef(ObjId id, ObjId type, ArrayList<Formal> formal_list, Expression init) {
        super(id, formal_list);
        this.type = type;
        this.body = init;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class VarDef extends Feature {
    Expression init;
    ObjId type;

    public VarDef(ObjId id, ObjId type, Expression init) {
        super(id, null);
        this.type = type;
        this.init = init;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class LocalVar extends Feature {
    ObjId type;
    Expression init;

    public LocalVar(ObjId id, ObjId type, Expression init) {
        super(id, null);
        this.init = init;
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// EXPRESSIONS ABSTRACT
abstract class Expression extends ASTNode {
}

class OopDispatch extends Expression {

    Expression left;
    ObjId parent_type;
    ObjId method_id;
    ArrayList<Expression> param_list;

    public OopDispatch(Expression left, ObjId parent_type, ObjId method_id, ArrayList<Expression> param_list) {
        this.parent_type = parent_type;
        this.method_id = method_id;
        this.param_list = param_list;
        this.left = left;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

}

class FuncCall extends Expression {

    ObjId id;
    ArrayList<Expression> param_list;

    public FuncCall(ObjId id, ArrayList<Expression> param_list) {
        this.id = id;
        this.param_list = param_list;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class WhileLoop extends Expression {
    Expression cond_expr;
    Expression instr_expr;

    WhileLoop(Expression cond_expr, Expression instr_expr) {
        this.cond_expr = cond_expr;
        this.instr_expr = instr_expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Block extends Expression {
    ArrayList<Expression> expr_list;

    public Block(ArrayList<Expression> expr_list) {
        this.expr_list = expr_list;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Case extends Expression {
    Expression init;
    ArrayList<CaseBranch> cases;

    public Case(Expression init, ArrayList<CaseBranch> cases) {
        this.init = init;
        this.cases = cases;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class CaseBranch extends Expression {
    Expression expr;

    ObjId id;
    ObjId type;

    public CaseBranch(Expression expr, ObjId id, ObjId type) {
        this.id = id;
        this.type = type;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class NewInstance extends Expression {
    ObjId type;

    NewInstance(ObjId type) {
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class VoidCheck extends Expression {
    Expression expr;
    ObjId tk;

    public VoidCheck(Expression expr) {
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Addition extends Expression {
    Expression left;
    Expression right;
    Token left_tk;
    Token right_tk;

    Addition(Expression left, Expression right, Token left_tk, Token right_tk) {
        this.left = left;
        this.right = right;
        this.left_tk = left_tk;
        this.right_tk = right_tk;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Subtraction extends Expression {
    Expression left;
    Expression right;
    Token left_tk;
    Token right_tk;

    Subtraction(Expression left, Expression right, Token left_tk, Token right_tk) {
        this.left = left;
        this.right = right;
        this.left_tk = left_tk;
        this.right_tk = right_tk;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Multiplication extends Expression {
    Expression left;
    Expression right;
    Token left_tk;
    Token right_tk;

    Multiplication(Expression left, Expression right, Token left_tk, Token right_tk) {
        this.left = left;
        this.right = right;
        this.left_tk = left_tk;
        this.right_tk = right_tk;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Division extends Expression {
    Expression left;
    Expression right;
    Token left_tk;
    Token right_tk;

    Division(Expression left, Expression right, Token left_tk, Token right_tk) {
        this.left = left;
        this.right = right;
        this.left_tk = left_tk;
        this.right_tk = right_tk;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ComplExpr extends Expression {
    Expression operand;
    ObjId tk;

    ComplExpr(Expression operand) {
        this.operand = operand;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class LessThan extends Expression {
    Expression left;
    Expression right;
    Token left_tk;
    Token right_tk;

    LessThan(Expression left, Expression right, Token left_tk, Token right_tk) {
        this.left = left;
        this.right = right;
        this.left_tk = left_tk;
        this.right_tk = right_tk;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class LessThanEqual extends Expression {
    Expression left;
    Expression right;
    Token left_tk;
    Token right_tk;

    LessThanEqual(Expression left, Expression right, Token left_tk, Token right_tk) {
        this.left = left;
        this.right = right;
        this.left_tk = left_tk;
        this.right_tk = right_tk;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Equality extends Expression {
    Expression left;
    Expression right;
    Token left_tk;
    Token right_tk;
    Token operator_tk;

    Equality(Expression left, Expression right, Token left_tk, Token right_tk, Token operator_tk) {
        this.left = left;
        this.right = right;
        this.left_tk = left_tk;
        this.right_tk = right_tk;
        this.operator_tk = operator_tk;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class NotExpr extends Expression {
    Expression operand;
    ObjId tk;

    NotExpr(Expression operand) {
        this.operand = operand;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ParenExpr extends Expression {

    Expression expression;

    public ParenExpr(Expression expression) {
        this.expression = expression;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ObjId extends Expression {

    Token tk;
    Symbol_Identifier symbol;
    Scope__Base scope;

    ObjId(Token id) {
        this.tk = id;
    }

    public String getText() {
        if (tk == null) {
            return null;
        }
        return tk.getText();
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ClInteger extends Expression {
    Token val;

    ClInteger(Token val) {
        this.val = val;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ClString extends Expression {
    Token val;

    public ClString(Token val) {
        this.val = val;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class BoolTrue extends Expression {
    Token val;

    public BoolTrue(Token val) {
        this.val = val;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class BoolFalse extends Expression {
    Token val;

    public BoolFalse(Token val) {
        this.val = val;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class SimpleAssign extends Expression {
    ObjId id;
    Expression expr;

    public SimpleAssign(ObjId id, Expression expr) {
        this.id = id;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ClIf extends Expression {

    Expression cond;
    Expression then_expr;
    Expression else_expr;

    public ClIf(Expression cond, Expression then_expr, Expression else_expr) {
        this.cond = cond;
        this.then_expr = then_expr;
        this.else_expr = else_expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Let extends Expression {
    ArrayList<Feature> let_list;
    Expression expr;

    public Let(ArrayList<Feature> let_list, Expression expr) {
        this.let_list = let_list;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
// PROGRAM ABSTRACT

class Program extends ASTNode {
    // A program is made up of one or more classes
    ArrayList<ClassDef> classes;

    Program(ArrayList<ClassDef> classes) {
        this.classes = classes;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}