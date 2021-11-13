package compiler.seman;

import java.util.*;

import compiler.*;
import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.type.*;

/**
 * Preverjanje tipov.
 * 
 * @author sliva
 */
public class TypeChecker implements Visitor {
        
        
	public void visit(AbsArrType arrType) {
            arrType.type.accept(this);
            SemArrType typ = new SemArrType(arrType.length, SymbDesc.getType(arrType.type));
            SymbDesc.setType(arrType, typ);
	}
	
	public void visit(AbsAtomConst atomConst) {
            SemAtomType typ = new SemAtomType(atomConst.type);
            SymbDesc.setType(atomConst, typ);
	}
	
	public void visit(AbsAtomType atomType) {
            SemAtomType typ = new SemAtomType(atomType.type);
            SymbDesc.setType(atomType, typ);
	}
	
	public void visit(AbsBinExpr binExpr) {
            binExpr.expr1.accept(this);
            binExpr.expr2.accept(this);
            SemType ltyp = new SemAtomType(0);
            SemType ityp = new SemAtomType(1);
            SemType styp = new SemAtomType(2);
            if((binExpr.oper == 0 || binExpr.oper==1) && SymbDesc.getType(binExpr.expr1).sameStructureAs(ltyp) && SymbDesc.getType(binExpr.expr2).sameStructureAs(ltyp)){
                SymbDesc.setType(binExpr, ltyp);
            }else if((binExpr.oper == 8 || binExpr.oper==9 || binExpr.oper == 10 || binExpr.oper==11 || binExpr.oper==12) && SymbDesc.getType(binExpr.expr1).sameStructureAs(ityp) && SymbDesc.getType(binExpr.expr2).sameStructureAs(ityp)){
                SymbDesc.setType(binExpr, ityp);
            }else if((binExpr.oper == 2 || binExpr.oper==3 || binExpr.oper == 4 || binExpr.oper==5 || binExpr.oper==6) && SymbDesc.getType(binExpr.expr1).sameStructureAs(ltyp) && SymbDesc.getType(binExpr.expr2).sameStructureAs(ityp)){
                SymbDesc.setType(binExpr, ltyp);
            }else if(binExpr.oper == 15 && (SymbDesc.getType(binExpr.expr1).sameStructureAs(ltyp) && SymbDesc.getType(binExpr.expr2).sameStructureAs(ltyp))){
                SymbDesc.setType(binExpr, ltyp);
            }else if(binExpr.oper == 15 && (SymbDesc.getType(binExpr.expr1).sameStructureAs(ityp) && SymbDesc.getType(binExpr.expr2).sameStructureAs(ityp))){
                SymbDesc.setType(binExpr, ityp);
            }else if(binExpr.oper == 15 && (SymbDesc.getType(binExpr.expr1).sameStructureAs(styp) && SymbDesc.getType(binExpr.expr2).sameStructureAs(styp))){
                SymbDesc.setType(binExpr, styp);
            }else{
                Report.error(binExpr.position, "Wrong type");
            }
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
                SemType typ = SymbDesc.getType(exprs.expr(exprs.numExprs()-1));
                SymbDesc.setType(exprs, typ);
	}
	
	public void visit(AbsFor forStmt) {
            forStmt.count.accept(this);
            forStmt.lo.accept(this);
            forStmt.hi.accept(this);
            forStmt.step.accept(this);
            forStmt.body.accept(this);
            SemType ctyp = new SemAtomType(1);
            if(SymbDesc.getType(forStmt.lo).sameStructureAs(ctyp) && SymbDesc.getType(forStmt.hi).sameStructureAs(ctyp) && SymbDesc.getType(forStmt.step).sameStructureAs(ctyp)){
                SemType typ = new SemAtomType(3);
                SymbDesc.setType(forStmt, typ);
            }else{
                Report.error(forStmt.position, "Wrong type");
            }
	}
	
	public void visit(AbsFunCall funCall) {
		for (int arg = 0; arg < funCall.numArgs(); arg++) {
                    funCall.arg(arg).accept(this);
		}
                SemType typ = SymbDesc.getType(funCall.arg(funCall.numArgs()-1));
                SymbDesc.setType(funCall, typ);
	}
	
	public void visit(AbsFunDef funDef) {
            Vector<SemType> parTypes = new Vector<>();
            for (int par = 0; par < funDef.numPars(); par++) {
                funDef.par(par).accept(this);
                parTypes.add(SymbDesc.getType(funDef.par(par).type));
            }
            funDef.type.accept(this);
            funDef.expr.accept(this);
            if(SymbDesc.getType(funDef.type).actualType() instanceof SemAtomType){
                SemFunType typ = new SemFunType(parTypes, SymbDesc.getType(funDef.type));
                SymbDesc.setType(funDef, typ);
            }
                
	}
	
	public void visit(AbsIfThen ifThen) {
            ifThen.cond.accept(this);		
            ifThen.thenBody.accept(this);	
            SemType ctyp = new SemAtomType(0);
            if(SymbDesc.getType(ifThen.cond).sameStructureAs(ctyp)){
                SemType typ = new SemAtomType(3);
                SymbDesc.setType(ifThen, typ);
            }else{
                Report.error(ifThen.position, "Wrong type");
            }
	}
	
	public void visit(AbsIfThenElse ifThenElse) {
            ifThenElse.cond.accept(this);		
            ifThenElse.thenBody.accept(this);		
            ifThenElse.elseBody.accept(this);
            SemType ctyp = new SemAtomType(0);
            if(SymbDesc.getType(ifThenElse.cond).sameStructureAs(ctyp)){
                SemType typ = new SemAtomType(3);
                SymbDesc.setType(ifThenElse, typ);
            }else{
                Report.error(ifThenElse.position, "Wrong type");
            }
	}
	
	public void visit(AbsPar par) {
            par.type.accept(this);
            SemTypeName typName = new SemTypeName(par.name);
            SemType typ = SymbDesc.getType(par.type);
            if(typ.actualType() instanceof SemAtomType){
                typName.setType(typ);
                SymbDesc.setType(par, typName);
            }
	}
		
	public void visit(AbsTypeDef typeDef) {
            typeDef.type.accept(this);
            SemTypeName typName = new SemTypeName(typeDef.name);
            SemType typ = SymbDesc.getType(typeDef.type);
            typName.setType(typ);
            SymbDesc.setType(typeDef, typName);
	}
	
	public void visit(AbsTypeName typeName) {
            
            SemTypeName typName = new SemTypeName(typeName.name);
            SemType typ = SymbDesc.getType(SymbDesc.getNameDef(typeName));
            typName.setType(typ);
            SymbDesc.setType(typeName, typName);
	}
	
	public void visit(AbsUnExpr unExpr) {
            unExpr.expr.accept(this);
            SemType ltyp = new SemAtomType(0);
            SemType ityp = new SemAtomType(1);
            if(unExpr.oper ==4 && SymbDesc.getType(unExpr.expr).sameStructureAs(ltyp)){
                SymbDesc.setType(unExpr, ltyp);
            }else if((unExpr.oper ==0 || unExpr.oper==1) && SymbDesc.getType(unExpr.expr).sameStructureAs(ityp)){
                SymbDesc.setType(unExpr, ityp);
            }else{
                Report.error(unExpr.position, "Wrong type");
            }
	}
	
	public void visit(AbsVarDef varDef) {
            varDef.type.accept(this);  
            SemTypeName typName = new SemTypeName(varDef.name);
            SemType typ = SymbDesc.getType(varDef.type);
            typName.setType(typ);
            SymbDesc.setType(varDef, typName);
	}
	
	public void visit(AbsVarName varName) {
            SemTypeName typName = new SemTypeName(varName.name);
            SemType typ = SymbDesc.getType(SymbDesc.getNameDef(varName));
            typName.setType(typ);
            SymbDesc.setType(varName, typName);
	}
	
	public void visit(AbsWhere where) {
            where.defs.accept(this);
            where.expr.accept(this);
            SymbDesc.setType(where, SymbDesc.getType(where.expr));
            
	}
	
	public void visit(AbsWhile whileStmt) {
            whileStmt.cond.accept(this);
            whileStmt.body.accept(this);
            SemType ctyp = new SemAtomType(0);
            if(SymbDesc.getType(whileStmt.cond).sameStructureAs(ctyp)){
                SemType typ = new SemAtomType(3);
                SymbDesc.setType(whileStmt, typ);
            }else{
                Report.error(whileStmt.position, "Wrong type");
            }
	}

}

