package compiler.imcode;

import java.util.*;

import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.frames.*;
import compiler.seman.SymbDesc;
import compiler.seman.type.SemType;

public class ImcCodeGen implements Visitor {

	public LinkedList<ImcChunk> chunks;
	public LinkedList<ImcExpr> list = new LinkedList();
        public LinkedList<ImcExpr> stm = new LinkedList();
                
	public ImcCodeGen() {
		chunks = new LinkedList<ImcChunk>();
	}
	
	// TODO

        public void visit(AbsArrType arrType) {
            arrType.type.accept(this);
            
	}
	
	public void visit(AbsAtomConst atomConst) {
            ImcCONST cons = null;
            switch (atomConst.type) {
                case 1:
                    cons = new ImcCONST(Integer.parseInt(atomConst.value));
                    break;
                case 0:
                    if(atomConst.value.equals("true"))
                        cons = new ImcCONST(1);
                    else if (atomConst.value.equals("false"))
                        cons = new ImcCONST(0);
                    break;
                default:
                    cons = new ImcCONST(atomConst.value);
                    break;
            }
            list.add(cons);
	}
	
	public void visit(AbsAtomType atomType) {
	}
	
	public void visit(AbsBinExpr binExpr) {
            binExpr.expr1.accept(this);
            binExpr.expr2.accept(this);
            ImcBINOP binop = new ImcBINOP(binExpr.oper, list.removeLast(), list.removeLast());
            list.add(binop);
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
            AbsDef def = SymbDesc.getNameDef(funCall);
            FrmFrame frameCall = FrmDesc.getFrame(def);
            ImcCALL call = new ImcCALL(frameCall.label, new ImcTEMP(frameCall.FP));
            
            LinkedList<ImcExpr> callArgs = new LinkedList();
            for (int arg = 0; arg < funCall.numArgs(); arg++) {
                funCall.arg(arg).accept(this);
            }
            callArgs.addAll(list);
            call.args.addAll(callArgs);
            stm.add(call);
            list = new LinkedList();
	}
	
	public void visit(AbsFunDef funDef) {
            FrmFrame frame = FrmDesc.getFrame(funDef);
            
            for (int par = 0; par < funDef.numPars(); par++) {
                funDef.par(par).accept(this);
            }
            funDef.type.accept(this);
            funDef.expr.accept(this);
            ImcMOVE move;
            if(stm.isEmpty())
                move = new ImcMOVE(new ImcTEMP(frame.RV), list.peekLast());
            else
                move = new ImcMOVE(new ImcTEMP(frame.RV), stm.peekFirst());
            this.chunks.add(new ImcCodeChunk(frame, move));
            stm = new LinkedList();
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
            //FrmParAccess parAcc = new FrmParAccess(par, frame);
           // FrmDesc.setAccess(par, parAcc);
            //par.type.accept(this);
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
            FrmVarAccess access = (FrmVarAccess) FrmDesc.getAccess(varDef);
            SemType type = SymbDesc.getType(varDef.type).actualType();
            ImcDataChunk dataChunk = new ImcDataChunk(access.label, type.size());
            this.chunks.add(dataChunk);
            varDef.type.accept(this);
	}
	
	public void visit(AbsVarName varName) {
	}
	
	public void visit(AbsWhere where) {
            /*level++;
            for (int def = 0; def < where.defs.numDefs(); def++) {
                if(where.defs.def(def) instanceof  AbsVarDef){
                    FrmLocAccess locAcc = new FrmLocAccess((AbsVarDef) where.defs.def(def), frame);
                    frame.locVars.add(locAcc);
                    FrmDesc.setAccess(where.defs.def(def), locAcc);
                }else
                    where.defs.def(def).accept(this);
            }
            where.expr.accept(this);
            level--;*/
            
	}
	
	public void visit(AbsWhile whileStmt) {
            whileStmt.cond.accept(this);
            whileStmt.body.accept(this);
	}
	
}
