package compiler.synan;

import compiler.Position;
import compiler.Report;
import compiler.abstr.tree.*;
import compiler.lexan.*;
import java.util.Vector;

/**
 * Sintaksni analizator.
 * 
 * @author sliva
 */
public class SynAn {

	/** Leksikalni analizator. */
	private LexAn lexAn;

	/** Ali se izpisujejo vmesni rezultati. */
	private boolean dump;
        static Symbol sym;
        static boolean check = true;

	/**
	 * Ustvari nov sintaksni analizator.
	 * 
	 * @param lexAn
	 *            Leksikalni analizator.
	 * @param dump
	 *            Ali se izpisujejo vmesni rezultati.
	 */
	public SynAn(LexAn lexAn, boolean dump) {
		this.lexAn = lexAn;
		this.dump = dump;
		// TODO
	}

	/**
	 * Opravi sintaksno analizo.
	 */
	public AbsTree parse() {
            
            AbsDefs def = new AbsDefs(null, new Vector());
            sym = lexAn.lexAn();
            if(sym.token != Token.EOF){
                dump("source → definitions");
                def = parseDefinitions();
            }
            
            if(sym.token != Token.EOF)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: EOF", sym.lexeme));
            
            return def;
	}

        public AbsDefs parseDefinitions(){
                
            dump("definitions → definition definitions′");
            Vector<AbsDef> vec = new Vector();
            vec.add(parseDefinition());
            parseDefinitionsAlt(vec);
            
            return new AbsDefs(new Position(vec.get(0).position, vec.get(vec.size()-1).position), vec);
        }
        
        public AbsDef parseDefinition(){
            
            switch(sym.token){
                case Token.KW_TYP : 
                    dump("definition → type_definition");
                    dump("type_definition → typ identifier : type");
                    AbsTypeDef def_typ = parseTyp_Def();
                    sym = lexAn.lexAn();
                    return def_typ;
                case Token.KW_FUN:
                    dump("definition → function_definition");
                    dump("function_definition → fun identifier ( parameters ) : type = expression");
                    AbsFunDef def_fun = parseFun_Def();
                    return def_fun;
                case Token.KW_VAR:
                    dump("definition → variable_definition");
                    dump("variable_definition → var identifier : type");
                    AbsVarDef def_var = parseVar_Def();
                    sym = lexAn.lexAn();
                    return def_var;
                default: Report.error(sym.position, String.format("Unexpected symbol %s. Expected: typ, fun or var", sym.lexeme));
            }
            return null;
        }
        
        public void parseDefinitionsAlt(Vector vec){
            
            if(sym.token == Token.SEMIC){
                dump("definitions′ → ; definition definitions′");
                sym = lexAn.lexAn();
                vec.add(parseDefinition());
                parseDefinitionsAlt(vec);
            }else
                dump("definitions′ → ε");
        }
            
        
        public AbsTypeDef parseTyp_Def(){
            
            Symbol s_pos = sym;
            sym = lexAn.lexAn();
            if(sym.token != Token.IDENTIFIER)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: IDENTIFIER", sym.lexeme));
            Symbol s = sym;
            sym = lexAn.lexAn();
            if(sym.token != Token.COLON)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ':'", sym.lexeme));
            AbsType type = parseType();
            return new AbsTypeDef(new Position(s_pos.position, type.position), s.lexeme, type);
        }
        
        public AbsVarDef parseVar_Def(){
            
            Symbol s_pos = sym;
            sym = lexAn.lexAn();
            if(sym.token != Token.IDENTIFIER)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: IDENTIFIER", sym.lexeme));
            Symbol s = sym;
            sym = lexAn.lexAn();
            if(sym.token != Token.COLON)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ':'", sym.lexeme));
            AbsType type = parseType();
            return new AbsVarDef(new Position(s_pos.position, type.position), s.lexeme, type);
            
        }
        
        public AbsFunDef parseFun_Def(){
            
            Symbol s_pos = sym;
            sym = lexAn.lexAn();
            if(sym.token != Token.IDENTIFIER)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: IDENTIFIER", sym.lexeme));
            Symbol s_name = sym;
            sym = lexAn.lexAn();
            if(sym.token != Token.LPARENT)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: '(", sym.lexeme));
            Vector vec_par = parseParameters();
            if(sym.token != Token.RPARENT)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ')", sym.lexeme));
            sym = lexAn.lexAn();
            if(sym.token != Token.COLON)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ':'", sym.lexeme));
            AbsType type = parseType();
            sym = lexAn.lexAn();
            if(sym.token != Token.ASSIGN)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: '='", sym.lexeme));
            AbsExpr expr = parseExpression();
            return new AbsFunDef(new Position(s_pos.position, expr.position), s_name.lexeme, vec_par, type, expr);
        }
        
        public AbsType parseType(){
            
            sym = lexAn.lexAn();
            Symbol s = sym;
            switch(sym.token){
                case Token.IDENTIFIER:
                    dump("type → identifier");
                    return new AbsTypeName(sym.position, sym.lexeme);
                case Token.LOGICAL:
                    dump("type → logical");
                    return new AbsAtomType(sym.position, 0); 
                case Token.INTEGER:
                    dump("type → integer");
                    return new AbsAtomType(sym.position, 1);
                case Token.STRING:
                    dump("type → string");
                    return new AbsAtomType(sym.position, 2);
                case Token.KW_ARR:
                    dump("type → arr [ int_const ] type");
                    sym = lexAn.lexAn();
                    if (sym.token != Token.LBRACKET) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: '['", sym.lexeme));
                    }
                    sym = lexAn.lexAn();
                    if (sym.token != Token.INT_CONST) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: Integer constant", sym.lexeme));
                    }
                    Symbol s_int = sym;
                    sym = lexAn.lexAn();
                    if (sym.token != Token.RBRACKET) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ']'", sym.lexeme));
                    }
                    AbsType type = parseType();
                    return new AbsArrType(new Position(s.position, type.position), Integer.parseInt(s_int.lexeme), type);
                default: Report.error(sym.position, String.format("Unexpected symbol %s. Expected: IDENTIFIER, integer, string, logical or arr [ int_const ] type", sym.lexeme));
            }
            return null;
        }
        
        public Vector parseParameters(){
            
            dump("parameters → parameter parameters′");
            dump("parameter → identifier : type");
            Vector<AbsPar> vec = new Vector();
            vec.add(parseParameter());
            parseParametersAlt(vec);
            
            return vec;
            
        }
        
        public void parseParametersAlt(Vector vec){
            
            sym = lexAn.lexAn();
            if (sym.token == Token.COMMA) {
                dump("parameters′ → , parameter parameters′");
                dump("parameter → identifier : type");
                vec.add(parseParameter());
                parseParametersAlt(vec);
            }else
                dump("parameters′ → ε");
        }
        
        public AbsPar parseParameter(){
            
            sym = lexAn.lexAn();
            if(sym.token != Token.IDENTIFIER)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: IDENTIFIER", sym.lexeme));
            Symbol s = sym;
            sym = lexAn.lexAn();
            if(sym.token != Token.COLON)
                Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ':'", sym.lexeme));
            AbsType type = parseType();
            return new AbsPar(new Position(s.position, type.position), s.lexeme, type);
            
        }
        
        public AbsExpr parseExpression() {

            dump("expression → logical_ior_expression expression′");
            AbsExpr expr = parselogical_ior_expression();
            AbsExpr expr_where = parseExpressionAlt(expr);
            if(expr_where == null)
                return expr;
            else
                return expr_where;

        }
        
        public AbsExpr parseExpressionAlt(AbsExpr expr) {

            if (sym.token == Token.LBRACE) {
                dump("expression′ → { WHERE definitions }");
                sym = lexAn.lexAn();
                if (sym.token != Token.KW_WHERE) {
                    Report.error(sym.position, String.format("Unexpected symbol %s. Expected: where", sym.lexeme));
                }
                sym = lexAn.lexAn();
                AbsDefs defs = parseDefinitions();
                Symbol s_st = sym;
                if (sym.token != Token.RBRACE) {
                    Report.error(sym.position, String.format("Unexpected symbol %s. Expected: '}'", sym.lexeme));
                }
                sym = lexAn.lexAn();
                return new AbsWhere(new Position(expr.position, s_st.position), expr, defs);
            } else {
                dump("expression′ → ε");
                return null;
            }
        }
        
        public AbsExpr parselogical_ior_expression(){
            
            dump("logical_ior_expression → logical_and_expression logical_ior_expression′");
            AbsExpr expr = parselogical_and_expression();
            expr = parselogical_ior_expressionAlt(expr);
            
            return expr;
        }
        
        public AbsExpr parselogical_ior_expressionAlt(AbsExpr expr){
        
            if(sym.token == Token.IOR){
                dump("logical_ior_expression′ → | logical_and_expression logical_ior_expression′");
                AbsExpr expr_2 = parselogical_and_expression();
                expr = parselogical_ior_expressionAlt(new AbsBinExpr(new Position(expr.position, expr_2.position), 0, expr, expr_2));
            }else{
                dump("logical_ior_expression′ → ε");
            }
            return expr;
        }
        
        public AbsExpr parselogical_and_expression(){
            
            dump("logical_and_expression → compare_expression logical_and_expression′");
            AbsExpr expr = parseCompare_expression();
            expr = parselogical_and_expressionAlt(expr);
            
            return expr;
        }
        
        public AbsExpr parselogical_and_expressionAlt(AbsExpr expr){
            
            if(sym.token == Token.AND){
                dump("logical_and_expression′ → & compare_expression logical_and_expression′");
                AbsExpr expr_2 = parseCompare_expression();
                expr = parselogical_and_expressionAlt(new AbsBinExpr(new Position(expr.position, expr_2.position), 1, expr, expr_2));
            }else
                dump("logical_and_expression′ → ε");
            
            return expr;
        }
        
        public AbsExpr parseCompare_expression(){
            
            dump("compare_expression → additive_expression compare_expression′");
            AbsExpr expr = parseAdditive_expression();
            expr = parseCompare_expressionAlt(expr);
            
            return expr;
        }
        
        public AbsExpr parseCompare_expressionAlt(AbsExpr expr){
            
            switch(sym.token){
                case Token.EQU:
                    dump("compare_expression′ → == additive_expression");
                    AbsExpr expr_equ = parseAdditive_expression();
                    return new AbsBinExpr(new Position(expr.position, expr_equ.position), 2, expr, expr_equ);
                case Token.NEQ:
                    dump("compare_expression′ → != additive_expression");
                    AbsExpr expr_neq = parseAdditive_expression();
                    return new AbsBinExpr(new Position(expr.position, expr_neq.position), 3, expr, expr_neq);
                case Token.LEQ:
                    dump("compare_expression′ → <= additive_expression");
                    AbsExpr expr_leq = parseAdditive_expression();
                    return new AbsBinExpr(new Position(expr.position, expr_leq.position), 4, expr, expr_leq);
                case Token.GEQ:
                    dump("compare_expression′ → >= additive_expression");
                    AbsExpr expr_geq = parseAdditive_expression();
                    return new AbsBinExpr(new Position(expr.position, expr_geq.position), 5, expr, expr_geq);
                case Token.LTH:
                    dump("compare_expression′ → < additive_expression");
                    AbsExpr expr_lth = parseAdditive_expression();
                    return new AbsBinExpr(new Position(expr.position, expr_lth.position), 6, expr, expr_lth);
                case Token.GTH:
                    dump("compare_expression′ → > additive_expression");
                    AbsExpr expr_gth = parseAdditive_expression();
                    return new AbsBinExpr(new Position(expr.position, expr_gth.position), 7, expr, expr_gth);
                default:
                    dump("compare_expression′ → ε");
            }
            return expr;
        }
        
        public AbsExpr parseAdditive_expression(){
            
            dump("additive_expression → multiplicative_expression additive_expression′");
            AbsExpr expr = parseMultiplicative_expression();
            expr = parseAdditive_expressionAlt(expr);
            
            return expr;
        }
        
        public AbsExpr parseAdditive_expressionAlt(AbsExpr expr){
            
            switch(sym.token){
                case Token.ADD:
                    dump("additive_expression′ → + multiplicative_expression additive_expression′");
                    AbsExpr expr_2 = parseMultiplicative_expression();
                    expr = parseAdditive_expressionAlt(new AbsBinExpr(new Position(expr.position, expr_2.position), 8, expr, expr_2));
                    break;
                case Token.SUB:
                    dump("additive_expression′ → - multiplicative_expression additive_expression′");
                    AbsExpr expr_3 = parseMultiplicative_expression();
                    expr = parseAdditive_expressionAlt(new AbsBinExpr(new Position(expr.position, expr_3.position), 9, expr, expr_3));
                    break;
                default:
                    dump("additive_expression′ → ε");
                    
            }
            return expr;
            
        }
        
        public AbsExpr parseMultiplicative_expression(){
        
            dump("multiplicative_expression → prefix_expression multiplicative_expression′");
            AbsExpr expr = parsePrefix_expression();
            expr = parseMultiplicative_expressionAlt(expr);
            
            return expr;
        }
        
        public AbsExpr parseMultiplicative_expressionAlt(AbsExpr expr){
            
            switch(sym.token){
                case Token.MUL:
                    dump("multiplicative_expression′ → * prefix_expression multiplicative_expression′");
                    AbsExpr expr_2 = parsePrefix_expression();
                    expr = parseMultiplicative_expressionAlt(new AbsBinExpr(new Position(expr.position, expr_2.position), 10, expr, expr_2));
                   break;
                case Token.DIV:
                    dump("multiplicative_expression′ → / prefix_expression multiplicative_expression′");
                    AbsExpr expr_3 = parsePrefix_expression();
                    expr = parseMultiplicative_expressionAlt(new AbsBinExpr(new Position(expr.position, expr_3.position), 11, expr, expr_3));
                    break;
                case Token.MOD:
                    dump("multiplicative_expression′ → % prefix_expression multiplicative_expression′");
                    AbsExpr expr_4 = parsePrefix_expression();
                    expr = parseMultiplicative_expressionAlt(new AbsBinExpr(new Position(expr.position, expr_4.position), 12, expr, expr_4));
                    break;
                default:
                    dump("multiplicative_expression′ → ε");
            }
            return expr;
        }
        
        public AbsExpr parsePrefix_expression(){
            
            if(check)
                sym = lexAn.lexAn();
            check = true;
            switch(sym.token){
                case Token.ADD:
                    dump("prefix_expression → + prefix_expression");
                    Symbol s_add = sym;
                    AbsExpr expr_add = parsePrefix_expression();
                    return new AbsUnExpr(new Position(s_add.position, expr_add.position), 0, expr_add);
                case Token.SUB:
                    dump("prefix_expression → - prefix_expression");
                    Symbol s_sub = sym;
                    AbsExpr expr_sub = parsePrefix_expression();
                    return new AbsUnExpr(new Position(s_sub.position, expr_sub.position), 1, expr_sub);
                case Token.NOT:
                    dump("prefix_expression → ! prefix_expression");
                    Symbol s_not = sym;
                    AbsExpr expr_not = parsePrefix_expression();
                    return new AbsUnExpr(new Position(s_not.position, expr_not.position), 4, expr_not);
                default:
                    dump("prefix_expression → postfix_expression");
                    AbsExpr expr = parsePostfix_expression();
                    return expr;
            }
            
        }
        
        public AbsExpr parsePostfix_expression(){
            
            dump("postfix_expression → atom_expression postfix_expression′");
            AbsExpr expr = parseAtom_expression();
            expr = parsePostfix_expressionAlt(expr);
            
            return expr;
        }
        
        public AbsExpr parsePostfix_expressionAlt(AbsExpr expr){
            
            if(sym.token == Token.LBRACKET){
                dump("postfix_expression′ → [ expression ] postfix_expression′");
                AbsExpr expr_2 = parseExpression();
                Symbol s_stop = sym;
                if (sym.token != Token.RBRACKET) {
                    Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ']'", sym.lexeme));
                }
                sym = lexAn.lexAn();
                expr = parsePostfix_expressionAlt(new AbsBinExpr(new Position(expr.position, s_stop.position), 14, expr, expr_2));
            }else{
                dump("postfix_expression′ → ε");
            }
            return expr;
        }
        
        public AbsExpr parseAtom_expression(){
            
            Symbol s = sym;
            switch(sym.token){
                case Token.LOG_CONST:
                    dump("atom_expression → log_const");
                    sym = lexAn.lexAn();
                    return new AbsAtomConst(s.position, 0, s.lexeme);
                case Token.INT_CONST:
                    dump("atom_expression → int_const");
                    sym = lexAn.lexAn();
                    return new AbsAtomConst(s.position, 1, s.lexeme);
                case Token.STR_CONST:
                    dump("atom_expression → str_const");
                    sym = lexAn.lexAn();
                    return new AbsAtomConst(s.position, 2, s.lexeme);
                case Token.IDENTIFIER:
                    dump("atom_expression → identifier atom_expression′′");
                    AbsExpr expr_iden = parseAtom_expression2(s);
                    return expr_iden;
                case Token.LBRACE:
                    dump("atom_expression → { atom_expression′");
                    AbsExpr expr_smt = parseAtom_expression1(s);
                    sym = lexAn.lexAn();
                    return expr_smt;
                case Token.LPARENT:
                    dump("atom_expression → ( expressions )");
                    Vector vec = parseExpressions();
                    Symbol s_pos = sym;
                    if (sym.token != Token.RPARENT) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ')'", sym.lexeme));
                    }
                    sym = lexAn.lexAn();
                    return new AbsExprs(new Position(s.position, s_pos.position), vec);
                default: Report.error(sym.position, String.format("Unexpected symbol %s. Expected: LOG_CONST, INT_CONST, STR_CONST, IDENTIFIER, '{' or '('", sym.lexeme));
            }
            return null;
        }
        
        public AbsExpr parseAtom_expression2(Symbol s){
            
            sym = lexAn.lexAn();
            if(sym.token == Token.LPARENT){
                dump("atom_expression′′ → ( expressions )");
                Vector vec = parseExpressions();
                Symbol s_pos = sym;
                if (sym.token != Token.RPARENT) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ')'", sym.lexeme));
                    }
                sym = lexAn.lexAn();
                return new AbsFunCall(new Position(s.position, s_pos.position), s.lexeme, vec);
            }else{
                dump("atom_expression′′ → ε");  
                return new AbsVarName(s.position, s.lexeme);
            }
        }
        
        public Vector parseExpressions(){
            
            dump("expressions → expression expressions′");
            Vector<AbsExpr> vec = new Vector();
            vec.add(parseExpression());
            parseExpressionsAlt(vec);
            
            return vec;
        }
        
        public void parseExpressionsAlt(Vector vec){
            
            if(sym.token == Token.COMMA){
                dump("expressions′ → , expression expressions′");
                vec.add(parseExpression());
                parseExpressionsAlt(vec);
            }else{
                dump("expressions′ → ε");
            }
        }
        
        public AbsExpr parseAtom_expression1(Symbol s){
            
            sym = lexAn.lexAn();
            switch(sym.token){
                case Token.KW_IF:
                    dump("atom_expression′ → if expression then expression atom_expression′′′");
                    AbsExpr expr_if1 = parseExpression();
                    if (sym.token != Token.KW_THEN) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: then", sym.lexeme));
                    }
                    AbsExpr expr_if2 = parseExpression();
                    Symbol s_stop = sym;
                    AbsExpr expr_else = parseAtom_expression3(s, expr_if1, expr_if2);
                    if (expr_else == null)
                        return new AbsIfThen(new Position(s.position, s_stop.position), expr_if1, expr_if2);
                    else
                        return expr_else;
                case Token.KW_WHILE:
                    dump("atom_expression′ → while expression : expression }");
                    AbsExpr expr_wh1 = parseExpression();
                    if (sym.token != Token.COLON) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ':'", sym.lexeme));
                    }
                    AbsExpr expr_wh2 = parseExpression();
                    if (sym.token != Token.RBRACE) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: '}'", sym.lexeme));
                    }
                    return new AbsWhile(new Position(s.position, sym.position), expr_wh1, expr_wh2);
                case Token.KW_FOR:
                        dump("atom_expression′ → for identifier = expression , expression , expression : expression }");
                        sym = lexAn.lexAn();
                        Symbol s_count = sym;
                        if (sym.token != Token.IDENTIFIER) {
                            Report.error(sym.position, String.format("Unexpected symbol %s. Expected: IDENTIFIER", sym.lexeme));
                        }
                        sym = lexAn.lexAn();
                        if (sym.token != Token.ASSIGN) {
                            Report.error(sym.position, String.format("Unexpected symbol %s. Expected: '='", sym.lexeme));
                        }
                        AbsExpr expr_for1 = parseExpression();
                        if (sym.token != Token.COMMA) {
                            Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ','", sym.lexeme));
                        }
                        AbsExpr expr_for2 = parseExpression();
                        if (sym.token != Token.COMMA) {
                            Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ','", sym.lexeme));
                        }
                        AbsExpr expr_for3 = parseExpression();
                        if (sym.token != Token.COLON) {
                            Report.error(sym.position, String.format("Unexpected symbol %s. Expected: ':'", sym.lexeme));
                        }
                        AbsExpr expr_for4 = parseExpression();
                        if (sym.token != Token.RBRACE) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: '}'", sym.lexeme));
                        }
                        return new AbsFor(new Position(s.position, sym.position), new AbsVarName(s_count.position, s_count.lexeme), expr_for1, expr_for2, expr_for3, expr_for4);
                default:
                    dump("atom_expression′ → expression = expression }");
                    check = false;
                    AbsExpr expr_1 = parseExpression();
                    if (sym.token != Token.ASSIGN) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: '='", sym.lexeme));
                    }
                    AbsExpr expr_2 = parseExpression();
                    if (sym.token != Token.RBRACE) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: '}'", sym.lexeme));
                    }
                    return new AbsBinExpr(new Position(s.position, sym.position), 15, expr_1, expr_2);
            }
        }
        
        public AbsExpr parseAtom_expression3(Symbol s, AbsExpr ex1 , AbsExpr ex2){
            
            switch(sym.token){
                case Token.RBRACE:
                    dump("atom_expression′′′ → }");
                    return null;
                case Token.KW_ELSE:
                    dump("atom_expression′′′ → else expression }");
                    AbsExpr expr = parseExpression();
                    if (sym.token != Token.RBRACE) {
                        Report.error(sym.position, String.format("Unexpected symbol %s. Expected: '}'", sym.lexeme));
                    }
                    return new AbsIfThenElse(new Position(s.position, sym.position), ex1, ex2, expr);
                default: Report.error(sym.position, String.format("Unexpected symbol %s. Expected: '}' or else", sym.lexeme));
            }
            return null;
        }
        
	/**
	 * Izpise produkcijo v datoteko z vmesnimi rezultati.
	 * 
	 * @param production
	 *            Produkcija, ki naj bo izpisana.
	 */
	private void dump(String production) {
		if (!dump)
			return;
		if (Report.dumpFile() == null)
			return;
		Report.dumpFile().println(production);
	}

}
