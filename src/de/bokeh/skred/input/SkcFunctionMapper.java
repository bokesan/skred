package de.bokeh.skred.input;

public class SkcFunctionMapper {

    private static class CombInfo {
        final int code;
        final String oldName;
        final String newName;
        CombInfo(int code, String oldName, String newName) {
            this.code = code;
            this.oldName = oldName;
            this.newName = newName;
        }
        CombInfo(int code, String name) {
            this.code = code;
            this.oldName = name;
            this.newName = name;
        }
    }

    private static final CombInfo[] COMBS = {
        new CombInfo(0, "S"),
        new CombInfo(1, "B"),
        new CombInfo(2, "C"),
        new CombInfo(3, "S'"),
        new CombInfo(4, "B'"),
        new CombInfo(5, "C'"),
        new CombInfo(6, "B*"),
        new CombInfo(7, "I"),
        new CombInfo(8, "K"),
        new CombInfo(9, "U"),
        new CombInfo(10, "Y"),
        new CombInfo(11, "W"),
        new CombInfo(12, "K'"),
        new CombInfo(20, "cons", "Pack2{1}"),
        new CombInfo(30, "+", "addInt"),
        new CombInfo(31, "*", "mulInt"),
        new CombInfo(32, "-", "subInt"),
        new CombInfo(33, "/", "divInt"),
        new CombInfo(34, "%", "modInt"),
        new CombInfo(35, "succ"),
        new CombInfo(36, "pred"),
        new CombInfo(37, "R-", "revSubInt"),
        new CombInfo(38, "R/", "revDivInt"),
        new CombInfo(39, "R%", "revModInt"),
        new CombInfo(40, "<", "ltInt"),
        new CombInfo(41, "<=", "leInt"),
        new CombInfo(42, ">", "gtInt"),
        new CombInfo(43, ">=", "geInt"),
        new CombInfo(44, "=", "eqInt"),
        new CombInfo(45, "<>", "neqInt"),
        new CombInfo(46, "zero"),
        new CombInfo(48, "compare0"),
        new CombInfo(50, "if", "CaseF2"),
        new CombInfo(51, "not"),
        new CombInfo(70, "boolean_p"),
        new CombInfo(71, "pair_p"),
        new CombInfo(73, "char_p"),
        new CombInfo(74, "number_p"),
        new CombInfo(80, "input"),
        new CombInfo(81, "output"),
        new CombInfo(100, "_ERROR"),
        new CombInfo(102, "_LISTCASE"),
        new CombInfo(105, "_READ"),
        new CombInfo(106, "_STD_PORT"),
    };
    
}
