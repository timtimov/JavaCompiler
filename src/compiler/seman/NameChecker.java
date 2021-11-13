package compiler.seman;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import compiler.abstr.*;
import compiler.abstr.tree.*;
import java.util.Vector;

/**
 * Preverjanje in razresevanje imen (razen imen komponent).
 * 
 * @author sliva
 */
public class NameChecker implements Visitor {

	public void visit(AbsArrType arrType) {
            arrType.type.accept(this);
	}
	
	public void visit(AbsAtomConst atomConst) {
	}
	
	public void visit(AbsAtomType atomType) {
	}
	
	public void visit(AbsBinExpr binExpr) {
            binExpr.expr1.accept(this);
            binExpr.expr2.accept(this);
	}
		
	public void visit(AbsDefs defs) {
            Vector<AbsVarDef> varVec = new Vector<>();
            Vector<AbsFunDef> funVec = new Vector();
            for (int def = 0; def < defs.numDefs(); def++) {
                if(defs.def(def) instanceof AbsVarDef)
                    varVec.add((AbsVarDef) defs.def(def));
                else if(defs.def(def) instanceof  AbsFunDef)
                    funVec.add((AbsFunDef) defs.def(def));
                else
                    defs.def(def).accept(this);
            }
            for (AbsVarDef var : varVec) {
                var.accept(this);
            }
            for (AbsFunDef fun : funVec) {
                fun.accept(this);
            }
	}
	
	public void visit(AbsExprs exprs) {
		for (int expr = 0; expr < exprs.numExprs(); expr++) {
                    exprs.expr(expr).accept(this);
		}
	}
	
	public void visit(AbsFor forStmt) {
            forStmt.count.accept(this);
            forStmt.lo.accept(this);
            forStmt.hi.accept(this);
            forStmt.step.accept(this);
            forStmt.body.accept(this);
	}
	
	public void visit(AbsFunCall funCall) {
            AbsDef def = SymbTable.fnd(funCall.name);
            SymbDesc.setNameDef(funCall, def);
		for (int arg = 0; arg < funCall.numArgs(); arg++) {
                    funCall.arg(arg).accept(this);
		}
	}
	
	public void visit(AbsFunDef funDef) {
            try {
                SymbTable.ins(funDef.name, funDef);
            } catch (SemIllegalInsertException ex) {
            }
            SymbTable.newScope();
            for (int par = 0; par < funDef.numPars(); par++) {
                funDef.par(par).accept(this);
            }
            funDef.type.accept(this);
            funDef.expr.accept(this);
            SymbTable.oldScope();
                
	}
	
	public void visit(AbsIfThen ifThen) {
            ifThen.cond.accept(this);		
            ifThen.thenBody.accept(this);	
	}
	
	public void visit(AbsIfThenElse ifThenElse) {
            ifThenElse.cond.accept(this);		
            ifThenElse.thenBody.accept(this);		
            ifThenElse.elseBody.accept(this);		
	}
	
	public void visit(AbsPar par) {
            try {
                SymbTable.ins(par.name, par);
            } catch (SemIllegalInsertException ex) {
            }
            par.type.accept(this);
	}
		
	public void visit(AbsTypeDef typeDef) {
            try {
                SymbTable.ins(typeDef.name, typeDef);
            } catch (SemIllegalInsertException ex) {
            }
            typeDef.type.accept(this);
	}
	
	public void visit(AbsTypeName typeName) {
            
            AbsDef def = SymbTable.fnd(typeName.name);
            SymbDesc.setNameDef(typeName, def);
	}
	
	public void visit(AbsUnExpr unExpr) {
            unExpr.expr.accept(this);
	}
	
	public void visit(AbsVarDef varDef) {
            try {
                SymbTable.ins(varDef.name, varDef);
            } catch (SemIllegalInsertException ex) {
            }
            varDef.type.accept(this);
	}
	
	public void visit(AbsVarName varName) {
            AbsDef def = SymbTable.fnd(varName.name);
            SymbDesc.setNameDef(varName, def);
	}
	
	public void visit(AbsWhere where) {
            SymbTable.newScope();
            where.defs.accept(this);
            where.expr.accept(this);
            SymbTable.oldScope();
            
	}
	
	public void visit(AbsWhile where) {
            where.cond.accept(this);
            where.body.accept(this);
	}

}
