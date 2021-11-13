package compiler.frames;

import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.SymbDesc;
import compiler.seman.SymbTable;
import java.util.Vector;

public class FrmEvaluator implements Visitor {
    
        int level = 1;
        FrmFrame frame;
    
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
                Vector<AbsFunDef> vec = new Vector();
		for (int def = 0; def < defs.numDefs(); def++) {
                    if(defs.def(def) instanceof  AbsFunDef)
                        vec.add((AbsFunDef) defs.def(def));
                    else
                        defs.def(def).accept(this);
		}
                for (AbsFunDef fun : vec) {
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
            AbsDef def = SymbDesc.getNameDef(funCall);
            FrmFrame frameCall = FrmDesc.getFrame(def);
            frame.sizeArgs = frameCall.sizePars;
		for (int arg = 0; arg < funCall.numArgs(); arg++) {
                    funCall.arg(arg).accept(this);
		}
	}
	
	public void visit(AbsFunDef funDef) {
            frame = new FrmFrame(funDef, level);
            FrmDesc.setFrame(funDef, frame);
            for (int par = 0; par < funDef.numPars(); par++) {
                funDef.par(par).accept(this);
            }
            funDef.type.accept(this);
            funDef.expr.accept(this);
            
            

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
            FrmParAccess parAcc = new FrmParAccess(par, frame);
            FrmDesc.setAccess(par, parAcc);
            par.type.accept(this);
	}
		
	public void visit(AbsTypeDef typeDef) {

            typeDef.type.accept(this);
	}
	
	public void visit(AbsTypeName typeName) {
            
	}
	
	public void visit(AbsUnExpr unExpr) {
            unExpr.expr.accept(this);
	}
	
	public void visit(AbsVarDef varDef) {
            FrmVarAccess varAcc = new FrmVarAccess(varDef);
            FrmDesc.setAccess(varDef, varAcc);
            varDef.type.accept(this);
	}
	
	public void visit(AbsVarName varName) {
	}
	
	public void visit(AbsWhere where) {
            level++;
            for (int def = 0; def < where.defs.numDefs(); def++) {
                if(where.defs.def(def) instanceof  AbsVarDef){
                    FrmLocAccess locAcc = new FrmLocAccess((AbsVarDef) where.defs.def(def), frame);
                    frame.locVars.add(locAcc);
                    FrmDesc.setAccess(where.defs.def(def), locAcc);
                }else
                    where.defs.def(def).accept(this);
            }
            where.expr.accept(this);
            level--;
            
	}
	
	public void visit(AbsWhile whileStmt) {
            whileStmt.cond.accept(this);
            whileStmt.body.accept(this);
	}
}
