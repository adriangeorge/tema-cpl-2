package cool.structures;

import java.io.File;

import org.antlr.v4.runtime.*;

import cool.compiler.Compiler;
import cool.parser.CoolParser;

public class SymbolTable {
    public static Scope__Base globals;

    private static boolean semanticErrors;

    public static void defineBasicClasses() {
        globals = new Scope_Default(null);
        semanticErrors = false;

        // TODO Populate global scope.
        globals.add(Symbol_Type.INT);
        globals.add(Symbol_Type.STRING);
        globals.add(Symbol_Type.SELF_TYPE);
        globals.add(Symbol_Type.BOOL);
        globals.add(Symbol_Type.Object);
        globals.add(Symbol_Type.IO);

    }

    /**
     * Displays a semantic error message.
     * 
     * @param ctx  Used to determine the enclosing class context of this error,
     *             which knows the file name in which the class was defined.
     * @param info Used for line and column information.
     * @param str  The error message.
     */
    public static void error(ParserRuleContext ctx, Token info, String str) {
        while (!(ctx.getParent() instanceof CoolParser.ProgramContext))
            ctx = ctx.getParent();

        String message = "\"" + new File(Compiler.fileNames.get(ctx)).getName()
                + "\", line " + info.getLine()
                + ":" + (info.getCharPositionInLine() + 1)
                + ", Semantic error: " + str;

        System.err.println(message);

        semanticErrors = true;
    }

    public static void error(String str) {
        String message = "Semantic error: " + str;

        System.err.println(message);

        semanticErrors = true;
    }

    public static boolean hasSemanticErrors() {
        return semanticErrors;
    }
}
