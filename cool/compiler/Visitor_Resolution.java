package cool.compiler;

import java.util.ArrayList;

import cool.structures.Symbol_Type;
import cool.structures.Scope_Default;
import cool.structures.Symbol_Method;
import cool.structures.Symbol_Identifier;
import cool.structures.SymbolTable;

public class Visitor_Resolution implements ASTVisitor<Symbol_Type> {

    // According to cool manual, for multiple type branches a join must be computed,
    // a.k.a. lowest common ancestor for classes
    // Source for lowest common ancestor:
    // https://www.baeldung.com/cs/tree-lowest-common-ancestor

    public Integer findDistanceToObject(Symbol_Type type) {
        Integer distance_counter = 0;
        // System.out.println("For class: " + type.name + " counting levels until
        // \"Object\"");
        // Get initial parent
        var parent_name = "";
        var parent = (Symbol_Type) SymbolTable.globals.lookup_attribute(type.name);
        // if no inheritance return 1
        if (parent.inheritance == null) {
            return 1;
        }
        do {
            parent_name = parent.inheritance;
            parent = (Symbol_Type) SymbolTable.globals.lookup_attribute(parent_name);
            distance_counter++;
        } while (parent != null);
        // System.out.println("Class " + type.name + " is at a distance of " +
        // distance_counter);
        return distance_counter;
    }

    public Integer findDistanceToParent(Symbol_Type type, Symbol_Type parent) {
        Integer distance_counter = 0;
        var parent_name = "";

        if (type.name.equals(parent.name)) {
            return distance_counter;
        }
        // if no inheritance return -1 (error)
        if (type.inheritance == null) {
            // check if on of the base types
            return -1;
        }
        do {
            parent_name = type.inheritance;
            type = (Symbol_Type) SymbolTable.globals.lookup_attribute(parent_name);
            distance_counter++;
            if (parent_name.equals(type.name)) {
                return distance_counter;
            }
        } while (parent != null);
        // System.out.println("Class " + type.name + " is at a distance of " +
        // distance_counter);
        return -1;
    }

    public Symbol_Type getParentAbove(Symbol_Type type, Integer up_levels) {
        // System.out.println("For class: " + type.name + " moving up " + up_levels + "
        // levels");
        var parent_name = "";
        var parent = (Symbol_Type) SymbolTable.globals.lookup_attribute(type.name);
        do {
            parent_name = parent.inheritance;

            parent = (Symbol_Type) SymbolTable.globals.lookup_attribute(parent_name);
            up_levels--;
            if (up_levels == 0) {
                // System.out.println("Moved from " + type.name + " to " + parent.name);
                return parent;
            }
        } while (parent != null);

        return Symbol_Type.Object;

    }

    public Symbol_Type findLCA(ArrayList<Symbol_Type> types) {
        // For each child find distance to Object "root"
        ArrayList<Integer> distances = new ArrayList<>(types.size());
        for (int i = 0; i < types.size(); i++) {
            distances.add(findDistanceToObject(types.get(i)));
        }

        // Find smallest distance to Object
        Integer smallest_dist = distances.get(0);
        for (Integer integer : distances) {
            if (integer <= smallest_dist)
                smallest_dist = integer;
        }
        // System.out.println("Smallest distance: " + smallest_dist);
        // // Optimisation: If the smallest distance is already 1 (next inheritance is
        // // "Object") stop here
        // if (smallest_dist == 1)
        // return Symbol_Type.Object;

        // Attempt to get all types to the same level in inheritance tree
        for (int i = 0; i < types.size(); i++) {
            Integer level_difference = distances.get(i) - smallest_dist;
            // Node already at smallest distance
            if (level_difference <= 0)
                continue;

            // Get to parent on smallest level
            types.set(i, getParentAbove(types.get(i), level_difference));
            distances.set(i, findDistanceToObject(types.get(i)));
        }

        while (smallest_dist >= 1) {
            // Check if all types are the same
            var same_type_flag = true;
            for (int i = 1; i < types.size(); i++) {
                if (types.get(i - 1) != types.get(i))
                    same_type_flag = false;
            }

            if (same_type_flag) {
                // System.out.println("Found common ancestor at " + types.get(0).name);
                return types.get(0);
            } else {
                smallest_dist--;
            }
            // Move each type up the tree one step at a time
            for (int i = 0; i < types.size(); i++) {
                types.set(i, getParentAbove(types.get(i), 1));
            }

        }

        return Symbol_Type.Object;
    }

    @Override
    public Symbol_Type visit(Program a) {
        for (var c : a.classes) {
            c.accept(this);
        }
        return null;
    }

    @Override
    public Symbol_Type visit(ClassDef a) {
        // Inheritance checks
        if (a.inherit.tk != null) {
            var parent_name = a.inherit.getText();
            var parent_type = (Symbol_Type) SymbolTable.globals.lookup_attribute(parent_name);
            // Check if this class' parent exists
            if (parent_type == null) {
                SymbolTable.error(a.ctx, a.inherit.tk,
                        "Class "
                                + a.type.getText()
                                + " has undefined parent "
                                + a.inherit.getText());
                return null;
            }

            // Go up the inheritance tree to check for cycles
            do {
                if (parent_name.equals(a.type.getText())) {
                    SymbolTable.error(a.ctx, a.type.tk,
                            "Inheritance cycle for class " + a.type.getText());
                    return null;
                }
                parent_name = parent_type.inheritance;
                parent_type = (Symbol_Type) SymbolTable.globals.lookup_attribute(parent_name);
            } while (parent_type != null);

        }

        // Visit class features
        for (var f : a.features) {
            f.accept(this);
        }

        return null;
    }

    @Override
    public Symbol_Type visit(FuncDef a) {
        var parent_class = ((Symbol_Type) a.id.scope.getParentScope());
        var symbol = (Symbol_Method) parent_class.lookup_method(a.id.getText());
        // Abort if this symbol is not in scope
        if (symbol == null) {
            return null;
        }

        // Check if return type exists
        var return_type = (Symbol_Type) SymbolTable.globals.lookup_attribute(a.type.getText());
        if (return_type == null) {
            SymbolTable.error(a.ctx, a.type.tk,
                    "Class "
                            + parent_class.getName()
                            + " has method "
                            + a.id.getText()
                            + " with undefined return type "
                            + a.type.getText());
            return null;
        }

        // Check if method has already been defined in parent class
        Symbol_Method override_method = null;
        ArrayList<Symbol_Identifier> override_formals = null;

        if (parent_class.inheritance != null) {
            // Check if method from parent is redefined
            var inherit_scope = (Symbol_Type) SymbolTable.globals.lookup_attribute(parent_class.inheritance);
            override_method = (Symbol_Method) inherit_scope.lookup_method(a.id.getText());

            if (override_method != null) {
                // Get formal list from overriden method
                override_formals = override_method.getFormals();

                // Check formal array length
                if (override_formals.size() != symbol.getFormals().size()) {
                    SymbolTable.error(a.ctx, a.id.tk,
                            "Class "
                                    + parent_class.getName()
                                    + " overrides method "
                                    + a.id.getText()
                                    + " with different number of formal parameters");
                    return null;
                }

                // Check if return type is different
                if (!override_method.return_type.equals(return_type.getName())) {
                    SymbolTable.error(a.ctx, a.type.tk,
                            "Class "
                                    + parent_class.getName()
                                    + " overrides method "
                                    + symbol.getName()
                                    + " but changes return type from "
                                    + override_method.return_type
                                    + " to "
                                    + return_type.getName());
                    return null;
                }
            }
        }

        // Iterate through all formals and check types
        for (int i = 0; i < a.formal_list.size(); i++) {
            Symbol_Type f_type = a.formal_list.get(i).accept(this);
            if (override_formals != null) {
                Symbol_Identifier o_type = override_formals.get(i);

                if (!f_type.name.equals(o_type.type)) {
                    SymbolTable.error(a.ctx, a.formal_list.get(i).type,
                            "Class "
                                    + parent_class.getName()
                                    + " overrides method "
                                    + symbol.getName()
                                    + " but changes type of formal parameter "
                                    + a.formal_list.get(i).id.getText()
                                    + " from "
                                    + o_type.type
                                    + " to "
                                    + f_type.name);
                    return null;
                }
            }
        }

        if (a.body != null) {
            Symbol_Type init_type = a.body.accept(this);
            if (init_type == null) {
                return null;
            }
            int distance_init_var_type = findDistanceToParent(init_type, return_type);
            if (distance_init_var_type == -1 && return_type != Symbol_Type.Object) {
                SymbolTable.error(a.ctx, a.body.ctx.start,
                        "Type "
                                + init_type.name
                                + " of the body of method "
                                + a.id.getText()
                                + " is incompatible with declared return type "
                                + return_type.name);
                return null;
            }
            return init_type;
        }
        return (Symbol_Type) SymbolTable.globals.lookup_attribute(a.type.getText());
    }

    @Override
    public Symbol_Type visit(VarDef a) {
        var parent_class = (Symbol_Type) a.id.scope;
        var symbol = (Symbol_Identifier) parent_class.lookup_attribute(a.id.getText());
        // Abort if this symbol is not in scope
        if (symbol == null || a.id.symbol == null) {
            return null;
        }

        // Prevent illegal self attribute name
        if (a.id.getText().equals("self")) {
            SymbolTable.error(a.ctx, a.id.tk,
                    "Class "
                            + parent_class.getName()
                            + " has attribute with illegal name self");
            return null;
        }

        // Check if attribute type exists
        var type_check = (Symbol_Type) SymbolTable.globals.lookup_attribute(a.type.getText());
        if (type_check == null) {
            SymbolTable.error(a.ctx, a.type.tk,
                    "Class "
                            + parent_class.getName()
                            + " has attribute "
                            + a.id.getText()
                            + " with undefined type "
                            + a.type.getText());
            return null;
        }

        // Check if attribute has already been defined in parent class
        if (parent_class.inheritance != null) {
            // Check if attribute from parent is redefined
            var inherit_scope = (Symbol_Type) SymbolTable.globals.lookup_attribute(parent_class.inheritance);
            if (inherit_scope.lookup_attribute(a.id.getText()) != null) {
                SymbolTable.error(a.ctx, a.id.tk,
                        "Class "
                                + parent_class.getName()
                                + " redefines inherited attribute "
                                + a.id.getText());
                return null;
            }
        }

        // Set var type
        if (a.init != null) {
            Symbol_Type init_type = a.init.accept(this);
            if (init_type == null) {
                return null;
            }
            int distance_init_var_type = findDistanceToParent(init_type, type_check);
            if (distance_init_var_type == -1) {
                SymbolTable.error(a.ctx, a.init.ctx.start,
                        "Type "
                                + init_type.name
                                + " of initialization expression of attribute "
                                + a.id.getText()
                                + " is incompatible with declared type "
                                + type_check.name);
                return null;
            }
            return init_type;
        }
        return (Symbol_Type) SymbolTable.globals.lookup_attribute(a.type.getText());
    }

    @Override
    public Symbol_Type visit(Formal a) {
        var parent_method = ((Symbol_Method) a.id.scope);
        var symbol = (Symbol_Identifier) parent_method.lookup_attribute(a.id.getText());

        // Abort if this symbol is not in scope
        if (symbol == null) {
            return null;
        }

        // Prevent illegal self formal name
        if (a.id.getText().equals("self")) {
            SymbolTable.error(a.ctx, a.id.tk,
                    "Method "
                            + parent_method.getName()
                            + " of class "
                            + parent_method.getParentScope().getName()
                            + " has formal parameter with illegal name self");
            return null;
        }

        if (a.type.getText().equals("SELF_TYPE")) {
            SymbolTable.error(a.ctx, a.type,
                    "Method "
                            + parent_method.getName()
                            + " of class "
                            + parent_method.getParentScope().getName()
                            + " has formal parameter "
                            + a.id.getText()
                            + " with illegal type SELF_TYPE");
            return null;
        }

        // Check if formal type exists
        var type_check = (Symbol_Type) SymbolTable.globals.lookup_attribute(a.type.getText());
        if (type_check == null) {
            SymbolTable.error(a.ctx, a.type,
                    "Method "
                            + parent_method.getName()
                            + " of class "
                            + parent_method.getParentScope().getName()
                            + " has formal parameter "
                            + a.id.getText()
                            + " with undefined type "
                            + a.type.getText());
            return null;
        }

        // Set formal type
        return type_check;
    }

    @Override
    public Symbol_Type visit(NewInstance a) {
        var new_type = (Symbol_Type) SymbolTable.globals.lookup_attribute(a.type.getText());
        if (new_type == null) {
            SymbolTable.error(a.ctx, a.type.tk,
                    "new is used with undefined type "
                            + a.type.getText());
            return null;
        }
        return new_type;
    }

    @Override
    public Symbol_Type visit(FuncCall a) {

        ArrayList<Symbol_Type> param_types = new ArrayList<>();
        for (var p : a.param_list) {
            param_types.add(p.accept(this));
        }

        return null;
    }

    @Override
    public Symbol_Type visit(OopDispatch a) {

        // Get class where method is called and method information
        var left_type = a.left.accept(this);
        var method_info = left_type.lookup_method(a.method_id.tk.getText());
        
        ArrayList<Symbol_Type> param_types = new ArrayList<>();
        for (var p : a.param_list) {
            param_types.add(p.accept(this));
        }
        return (Symbol_Type) left_type.lookup_attribute(method_info.return_type);
    }

    @Override
    public Symbol_Type visit(ObjId a) {
        // check for self reference
        if (a.tk.getText().equals("self")) {
            var class_scope = a.scope;
            while (class_scope.getParentScope() != SymbolTable.globals) {
                class_scope = class_scope.getParentScope();
            }
            return (Symbol_Type) class_scope;
        }

        // look for variable in scope
        var lookup = a.scope.lookup_attribute(a.tk.getText());

        // return type of variable
        return (Symbol_Type) SymbolTable.globals.lookup_attribute(((Symbol_Identifier) lookup).type);
    }

    @Override
    public Symbol_Type visit(ParenExpr a) {
        return a.expression.accept(this);
    }

    @Override
    public Symbol_Type visit(NotExpr a) {
        var e_type = a.operand.accept(this);
        if (e_type != Symbol_Type.BOOL && e_type != null) {
            SymbolTable.error(a.ctx, a.tk.tk,
                    "Operand of not has type " + e_type.name + " instead of Bool");
            return null;
        }
        return e_type;
    }

    @Override
    public Symbol_Type visit(Equality a) {
        var l_type = a.left.accept(this);
        var r_type = a.right.accept(this);

        if (l_type == Symbol_Type.BOOL || l_type == Symbol_Type.INT || l_type == Symbol_Type.STRING ||
                r_type == Symbol_Type.BOOL || r_type == Symbol_Type.INT || r_type == Symbol_Type.STRING)
            if (l_type != r_type && r_type != null && l_type != null) {
                SymbolTable.error(a.ctx, a.operator_tk, "Cannot compare " + l_type + " with " + r_type);
                return null;
            }

        return Symbol_Type.BOOL;
    }

    @Override
    public Symbol_Type visit(LessThanEqual a) {
        var l_type = a.left.accept(this);
        var r_type = a.right.accept(this);

        if (l_type != Symbol_Type.INT && l_type != null) {
            SymbolTable.error(a.ctx, a.left_tk, "Operand of <= has type " + l_type + " instead of Int");
            return null;
        } else if (r_type != Symbol_Type.INT && r_type != null) {
            SymbolTable.error(a.ctx, a.right_tk, "Operand of <= has type " + r_type + " instead of Int");
            return null;
        }

        return Symbol_Type.BOOL;
    }

    @Override
    public Symbol_Type visit(LessThan a) {
        var l_type = a.left.accept(this);
        var r_type = a.right.accept(this);

        if (l_type != Symbol_Type.INT && l_type != null) {
            SymbolTable.error(a.ctx, a.left_tk, "Operand of < has type " + l_type + " instead of Int");
            return null;
        } else if (r_type != Symbol_Type.INT && r_type != null) {
            SymbolTable.error(a.ctx, a.right_tk, "Operand of < has type " + r_type + " instead of Int");
            return null;
        }

        return Symbol_Type.BOOL;
    }

    @Override
    public Symbol_Type visit(ComplExpr a) {
        var e_type = a.operand.accept(this);

        if (e_type != Symbol_Type.INT && e_type != null) {
            SymbolTable.error(a.ctx, a.tk.tk,
                    "Operand of ~ has type " + e_type.name + " instead of Int");
            return null;
        }
        return e_type;
    }

    @Override
    public Symbol_Type visit(Division a) {
        var l_type = a.left.accept(this);
        var r_type = a.right.accept(this);

        if (l_type != Symbol_Type.INT && l_type != null) {
            SymbolTable.error(a.ctx, a.left_tk, "Operand of / has type " + l_type + " instead of Int");
            return null;
        } else if (r_type != Symbol_Type.INT && r_type != null) {
            SymbolTable.error(a.ctx, a.right_tk, "Operand of / has type " + r_type + " instead of Int");
            return null;
        }

        return l_type;
    }

    @Override
    public Symbol_Type visit(Multiplication a) {
        var l_type = a.left.accept(this);
        var r_type = a.right.accept(this);

        if (l_type != Symbol_Type.INT && l_type != null) {
            SymbolTable.error(a.ctx, a.left_tk, "Operand of * has type " + l_type + " instead of Int");
            return null;
        } else if (r_type != Symbol_Type.INT && r_type != null) {
            SymbolTable.error(a.ctx, a.right_tk, "Operand of * has type " + r_type + " instead of Int");
            return null;
        }

        return l_type;
    }

    @Override
    public Symbol_Type visit(Subtraction a) {
        var l_type = a.left.accept(this);
        var r_type = a.right.accept(this);

        if (l_type != Symbol_Type.INT && l_type != null) {
            SymbolTable.error(a.ctx, a.left_tk, "Operand of - has type " + l_type + " instead of Int");
            return null;
        } else if (r_type != Symbol_Type.INT && r_type != null) {
            SymbolTable.error(a.ctx, a.right_tk, "Operand of - has type " + r_type + " instead of Int");
            return null;
        }

        return l_type;
    }

    @Override
    public Symbol_Type visit(Addition a) {
        var l_type = a.left.accept(this);
        var r_type = a.right.accept(this);

        if (l_type != Symbol_Type.INT && l_type != null) {
            SymbolTable.error(a.ctx, a.left_tk, "Operand of + has type " + l_type + " instead of Int");
            return null;
        } else if (r_type != Symbol_Type.INT && r_type != null) {
            SymbolTable.error(a.ctx, a.right_tk, "Operand of + has type " + r_type + " instead of Int");
            return null;
        }

        return l_type;
    }

    @Override
    public Symbol_Type visit(Block a) {
        Symbol_Type last_type = null;
        for (var e : a.expr_list) {
            last_type = e.accept(this);
        }
        return last_type;
    }

    @Override
    public Symbol_Type visit(WhileLoop a) {

        var cond_type = a.cond_expr.accept(this);
        if (cond_type != Symbol_Type.BOOL) {
            SymbolTable.error(a.ctx, a.cond_expr.ctx.start,
                    "While condition has type " + cond_type + " instead of Bool");
            return Symbol_Type.Object;

        }
        var expr_type = a.instr_expr.accept(this);
        return Symbol_Type.Object;
    }

    @Override
    public Symbol_Type visit(ClInteger a) {

        return Symbol_Type.INT;
    }

    @Override
    public Symbol_Type visit(BoolTrue a) {
        return Symbol_Type.BOOL;
    }

    @Override
    public Symbol_Type visit(ClString a) {

        return Symbol_Type.STRING;
    }

    @Override
    public Symbol_Type visit(BoolFalse a) {
        return Symbol_Type.BOOL;
    }

    @Override
    public Symbol_Type visit(SimpleAssign a) {
        var assignee = a.id.scope.lookup_attribute(a.id.getText());
        var assigned = a.expr.accept(this);

        if (a.id.tk.getText().equals("self")) {
            SymbolTable.error(a.ctx, a.id.tk, "Cannot assign to self");
            return null;

        }
        if (assigned == null) {
            return null;
        }
        if (assigned.name.equals(((Symbol_Identifier) assignee).type)) {
            return assigned;
        }

        // Check if type of assigned expr is correct
        // Go up the inheritance tree
        var parent_name = assigned.name;
        Symbol_Type parent_type = assigned;
        do {
            if (parent_name.equals(((Symbol_Identifier) assignee).type)) {
                return assigned;
            }
            parent_name = parent_type.inheritance;
            parent_type = (Symbol_Type) SymbolTable.globals.lookup_attribute(parent_name);
        } while (parent_type != null);

        // If no valid inheritance has been found code reaches here and emits error
        SymbolTable.error(a.ctx, a.expr.ctx.start,
                "Type " + assigned.name + " of assigned expression is incompatible with declared type "
                        + ((Symbol_Identifier) assignee).type + " of identifier " + a.id.getText());
        return null;
    }

    @Override
    public Symbol_Type visit(ClIf a) {

        var cond_type = a.cond.accept(this);
        if (cond_type != Symbol_Type.BOOL) {
            SymbolTable.error(a.ctx, a.cond.ctx.start,
                    "If condition has type " + cond_type + " instead of Bool");
            return Symbol_Type.Object;

        }

        var then_type = a.then_expr.accept(this);
        var else_type = a.else_expr.accept(this);

        // Check if type of then and else branches are compatible
        if (else_type == then_type) {
            return then_type;
        }

        var typeArray = new ArrayList<Symbol_Type>(2);
        typeArray.add(then_type);
        typeArray.add(else_type);
        return findLCA(typeArray);

    }

    @Override
    public Symbol_Type visit(Let a) {

        for (var l : a.let_list) {
            l.accept(this);
        }

        var expr_type = a.expr.accept(this);
        return expr_type;
    }

    @Override
    public Symbol_Type visit(Case a) {

        a.init.accept(this);

        ArrayList<Symbol_Type> branch_types = new ArrayList<>();
        for (var c : a.cases) {
            var branch_type = c.accept(this);
            if (branch_type != null) {
                branch_types.add(branch_type);
            }
        }
        return findLCA(branch_types);
    }

    @Override
    public Symbol_Type visit(CaseBranch a) {
        // Prevent illegal self name
        if (a.id.getText().equals("self")) {
            SymbolTable.error(a.ctx, a.id.tk,
                    "Case variable has illegal name self");
            return null;
        }

        if (a.type.getText().equals("SELF_TYPE")) {
            SymbolTable.error(a.ctx, a.type.tk,
                    "Case variable "
                            + a.id.getText()
                            + " has illegal type SELF_TYPE");
            return null;
        }

        if (SymbolTable.globals.lookup_attribute(a.type.getText()) == null) {
            SymbolTable.error(a.ctx, a.type.tk,
                    "Case variable "
                            + a.id.getText()
                            + " has undefined type "
                            + a.type.getText());
            return null;
        }

        var case_branch_type = a.expr.accept(this);

        return case_branch_type;
    }

    @Override
    public Symbol_Type visit(LocalVar a) {
        var let_scope = ((Scope_Default) a.id.scope);
        var symbol = (Symbol_Identifier) let_scope.lookup_attribute(a.id.getText());

        // Abort if this symbol is not in scope
        if (symbol == null) {
            return null;
        }

        // Prevent illegal self name
        if (a.id.getText().equals("self")) {
            SymbolTable.error(a.ctx, a.id.tk,
                    "Let variable has illegal name self");
            return null;
        }

        // Check if attribute type exists
        var type_check = (Symbol_Type) SymbolTable.globals.lookup_attribute(a.type.getText());
        if (type_check == null) {
            SymbolTable.error(a.ctx, a.type.tk,
                    "Let variable "
                            + a.id.getText()
                            + " has undefined type "
                            + a.type.getText());
            return null;
        }

        // Check if local variable type and init type match
        if (a.init != null) {
            Symbol_Type init_type = a.init.accept(this);
            // Perform LCA search to determine if types are compatible
            int distance_init_var_type = findDistanceToParent(init_type, type_check);
            if (distance_init_var_type == -1) {
                SymbolTable.error(a.ctx, a.init.ctx.start,
                        "Type "
                                + init_type.name
                                + " of initialization expression of identifier "
                                + a.id.getText()
                                + " is incompatible with declared type "
                                + type_check.name);
                return null;
            }
        }
        return type_check;
    }

    @Override
    public Symbol_Type visit(VoidCheck voidCheck) {
        // TODO Auto-generated method stub
        return null;
    }
}
