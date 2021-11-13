package compiler.lexan;

import compiler.*;
import java.io.*;

/**
 * Leksikalni analizator.
 * 
 * @author sliva
 */
public class LexAn {
	
	/** Ali se izpisujejo vmesni rezultati. */
	private boolean dump;
        private FileReader fr;
        private BufferedReader bf;
        static int buffer;
        static int line;
        static int column;
        private int temp;
        private String text;
	/**
	 * Ustvari nov leksikalni analizator.
	 * 
	 * @param sourceFileName
	 *            Ime izvorne datoteke.
	 * @param dump
	 *            Ali se izpisujejo vmesni rezultati.
	 */
	public LexAn(String sourceFileName, boolean dump) {		
		
            this.dump = dump;
            this.temp = -2;
            this.text="";
            line = 1;
            column = 1;
            try{
                File file = new File(sourceFileName);
                this.fr = new FileReader(file);
                this.bf = new BufferedReader(this.fr);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
	
	/**
	 * Vrne naslednji simbol iz izvorne datoteke. Preden vrne simbol, ga izpise
	 * na datoteko z vmesnimi rezultati.
	 * 
	 * @return Naslednji simbol iz izvorne datoteke.
	 */
	public Symbol lexAn() {
            
            Symbol sym = null;
            while(true){
                try{
                    if (this.temp == -2)
                        buffer = bf.read();
                    else{
                        buffer = temp;
                        temp = -2;
                    }
                    
                    if(buffer == 39){
                        boolean check = true;
                        int oriColl = column;
                        do{
                            buffer = bf.read();
                            column++;
                            if(buffer < 32 || buffer > 126){
                                if(buffer == 13 || buffer == -1)
                                    Report.error(line, column, "Unclosed string literal");
                                else
                                    Report.error(line, column, "Invalid character ---> " + (char)buffer );
                            }
                            if(buffer == 39){
                                temp = bf.read();
                                column++;
                                if(temp == 39){
                                    temp = -2;
                                    text += (char)buffer;
                                }else{
                                    column--;
                                    check = false;
                                }
                            }else{
                                text += (char)buffer;
                            }
                            
                        }while(check == true);
                        
                        sym = new Symbol(Token.STR_CONST, text, line, oriColl, line, column);
                        text = "";
                        column++;
                        dump(sym);
                        return sym;
                    }
                    
                    if((buffer >= 65 && buffer <= 90) || (buffer >= 97 && buffer <= 122) || buffer == 95){
                        int oriColl = column;
                        while((buffer >= 65 && buffer <= 90) || (buffer >= 97 && buffer <= 122) || buffer == 95 || (buffer >= 48 && buffer <= 57)){
                            text += (char)buffer;
                            buffer = bf.read();
                            column++;
                        }
                        temp = buffer;
                        
                        switch(text){
                            case "arr": sym = new Symbol(Token.KW_ARR, text, line, oriColl, line, column-1);
                                        break;
                            case "else": sym = new Symbol(Token.KW_ELSE, text, line, oriColl, line, column-1);
                                        break; 
                            case "for": sym = new Symbol(Token.KW_FOR, text, line, oriColl, line, column-1);
                                        break;   
                            case "fun": sym = new Symbol(Token.KW_FUN, text, line, oriColl, line, column-1);
                                        break;
                            case "if": sym = new Symbol(Token.KW_IF, text, line, oriColl, line, column-1);
                                        break;
                            case "then": sym = new Symbol(Token.KW_THEN, text, line, oriColl, line, column-1);
                                        break;
                            case "typ": sym = new Symbol(Token.KW_TYP, text, line, oriColl, line, column-1);
                                        break; 
                            case "var": sym = new Symbol(Token.KW_VAR, text, line, oriColl, line, column-1);
                                        break;
                            case "where": sym = new Symbol(Token.KW_WHERE, text, line, oriColl, line, column-1);
                                        break;
                            case "while": sym = new Symbol(Token.KW_WHILE, text, line, oriColl, line, column-1);
                                        break;
                            case "true": sym = new Symbol(Token.LOG_CONST, text, line, oriColl, line, column-1);
                                        break;
                            case "false": sym = new Symbol(Token.LOG_CONST, text, line, oriColl, line, column-1);
                                        break;
                            case "logical": sym = new Symbol(Token.LOGICAL, text, line, oriColl, line, column-1);
                                        break;
                            case "integer": sym = new Symbol(Token.INTEGER, text, line, oriColl, line, column-1);
                                        break;
                            case "string": sym = new Symbol(Token.STRING, text, line, oriColl, line, column-1);
                                        break;            
                            default: sym = new Symbol(Token.IDENTIFIER, text, line, oriColl, line, column-1);
                        }
                        
                        text = "";
                        dump(sym);
                        return sym;
                    }
                    
                    if(buffer >= 48 && buffer <= 57){
                        int oriColl = column;
                        while(buffer >= 48 && buffer <= 57){
                            text += (char)buffer;
                            buffer = bf.read();
                            column++; 
                        }
                        temp = buffer;
                        sym = new Symbol(Token.INT_CONST, text, line, oriColl, line, column-1);
                        text = "";
                        dump(sym);
                        return sym;
                    }
                    
                    switch(buffer){
                        
                        case -1: 
                            bf.close();
                            sym = new Symbol(Token.EOF, "EOF", line, column, line, column);
                            break;
                        case 9: column += 3; 
                            break;
                        case 32: break;                
                        case 13: break;
                        case 10:
                            line++;
                            column = 0;
                            break;
                        case 43: sym = new Symbol(Token.ADD, "+", line, column, line, column);
                                break;
                        case 45: sym = new Symbol(Token.SUB, "-", line, column, line, column);
                                break;
                        case 42: sym = new Symbol(Token.MUL, "*", line, column, line, column);
                                break;       
                        case 47: sym = new Symbol(Token.DIV, "/", line, column, line, column);
                                break;
                        case 37: sym = new Symbol(Token.MOD, "%", line, column, line, column);
                                break;
                        case 38: sym = new Symbol(Token.AND, "&", line, column, line, column);
                                break;
                        case 124: sym = new Symbol(Token.IOR, "|", line, column, line, column);
                                break;
                        case 40: sym = new Symbol(Token.LPARENT, "(", line, column, line, column);
                                break;
                        case 41: sym = new Symbol(Token.RPARENT, ")", line, column, line, column);
                                break;
                        case 91: sym = new Symbol(Token.LBRACKET, "[", line, column, line, column);
                                break;
                        case 93: sym = new Symbol(Token.RBRACKET, "]", line, column, line, column);
                                break;
                        case 123: sym = new Symbol(Token.LBRACE, "{", line, column, line, column);
                                break;
                        case 125: sym = new Symbol(Token.RBRACE, "}", line, column, line, column);
                                break;
                        case 58: sym = new Symbol(Token.COLON, ":", line, column, line, column);
                                break;        
                        case 59: sym = new Symbol(Token.SEMIC, ";", line, column, line, column);
                                break;   
                        case 46: sym = new Symbol(Token.DOT, ".", line, column, line, column);
                                break;     
                        case 44: sym = new Symbol(Token.COMMA, ",", line, column, line, column);
                                break;
                        case 33: temp = bf.read();
                                 if(temp == 61){
                                    sym = new Symbol(Token.NEQ, "!=", line, column, line, column+1);
                                    column++;
                                    temp = -2;
                                    break;   
                                 }
                                 sym = new Symbol(Token.NOT, "!", line, column, line, column);
                                 break;
                        case 61: temp = bf.read();
                                 if(temp == 61){
                                    sym = new Symbol(Token.EQU, "==", line, column, line, column+1);
                                    column++;
                                    temp = -2;
                                    break;   
                                 }
                                 sym = new Symbol(Token.ASSIGN, "=", line, column, line, column);
                                 break;
                        case 60: temp = bf.read();
                                 if(temp == 61){
                                    sym = new Symbol(Token.LEQ, "<=", line, column, line, column+1);
                                    column++;
                                    temp = -2;
                                    break;   
                                 }
                                 sym = new Symbol(Token.LTH, "<", line, column, line, column);
                                 break;       
                        case 62: temp = bf.read();
                                 if(temp == 61){
                                    sym = new Symbol(Token.GEQ, ">=", line, column, line, column+1);
                                    column++;
                                    temp = -2;
                                    break;   
                                 }
                                 sym = new Symbol(Token.GTH, ">", line, column, line, column);
                                 break;
                        case 35: text = bf.readLine();
                                 text = "";
                                line++;
                                column = 0;
                                break;
                        default: Report.error(line, column, "Invalid character ---> " + (char)buffer );
                                
                    }
                    column++;
                    if(sym != null)
                        break;
                    
                    
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
            dump(sym);
            return sym;
            
	}

	/**
	 * Izpise simbol v datoteko z vmesnimi rezultati.
	 * 
	 * @param symb
	 *            Simbol, ki naj bo izpisan.
	 */
	private void dump(Symbol symb) {
		if (! dump) return;
		if (Report.dumpFile() == null) return;
		if (symb.token == Token.EOF)
			Report.dumpFile().println(symb.toString());
		else
			Report.dumpFile().println("[" + symb.position.toString() + "] " + symb.toString());
	}

}
