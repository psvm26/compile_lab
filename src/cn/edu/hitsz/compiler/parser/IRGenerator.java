package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Symbol;
import cn.edu.hitsz.compiler.parser.table.Term;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {

    private List<Instruction> IRList = new ArrayList<>();
    private SymbolTable symbolTable;

    private Stack<Symbol> symbolStack = new Stack<>(); //符号栈

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        Symbol nowSymbol = new Symbol(currentToken);
        if(currentToken.getKindId().equals("IntConst")){
            nowSymbol.setVal(IRImmediate.of(Integer.parseInt(currentToken.getText())));
        } else if (currentToken.getKindId().equals("id")) {
            nowSymbol.setVal(IRVariable.named(currentToken.getText()));
        }
        symbolStack.push(nowSymbol);
//        throw new NotImplementedException();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        Symbol nowSymbol = new Symbol(production.head());
        switch (production.index()) {
            case 6->{   //S -> id = E
                Symbol fromSymbol = symbolStack.pop();
                symbolStack.pop();
                Symbol resultSymbol = symbolStack.pop();
                resultSymbol.setVal(IRVariable.named(resultSymbol.getToken().getText()));
                Instruction movInst = Instruction.createMov((IRVariable) resultSymbol.getVal(), fromSymbol.getVal());
                IRList.add(movInst);
            }
            case 7->{   //S -> return E
                Symbol returnSymbol = symbolStack.pop();
                symbolStack.pop();
                Instruction retInst = Instruction.createRet(returnSymbol.getVal());
                IRList.add(retInst);
            }
            case 8->{   //E -> E + A
                Symbol rSrcSymbol = symbolStack.pop();
                symbolStack.pop();
                Symbol lSrcSymbol = symbolStack.pop();
                nowSymbol.setVal(IRVariable.temp());
                Instruction addInst = Instruction.createAdd((IRVariable) nowSymbol.getVal(), lSrcSymbol.getVal(), rSrcSymbol.getVal());
                IRList.add(addInst);
            }
            case 9->{   //E -> E - A
                Symbol rSrcSymbol = symbolStack.pop();
                symbolStack.pop();
                Symbol lSrcSymbol = symbolStack.pop();
                nowSymbol.setVal(IRVariable.temp());
                Instruction subInst = Instruction.createSub((IRVariable) nowSymbol.getVal(), lSrcSymbol.getVal(), rSrcSymbol.getVal());
                IRList.add(subInst);
            }
            case 10,12,14,15->{  //E -> A,A -> B,B -> id,B -> IntConst
                Symbol topSymbol = symbolStack.pop();
                nowSymbol.setVal(topSymbol.getVal());
            }
            case 11->{  //A -> A * B
                Symbol rSrcSymbol = symbolStack.pop();
                symbolStack.pop();
                Symbol lSrcSymbol = symbolStack.pop();
                nowSymbol.setVal(IRVariable.temp());
                Instruction mulInst = Instruction.createMul((IRVariable) nowSymbol.getVal(), lSrcSymbol.getVal(), rSrcSymbol.getVal());
                IRList.add(mulInst);
            }
            case 13 -> {
                symbolStack.pop();
                Symbol topSymbol = symbolStack.pop();
                symbolStack.pop();
                nowSymbol.setVal(topSymbol.getVal());
            }
            default -> {
                for(Term ignore: production.body()) {
                    symbolStack.pop();
                }
            }
        }
        symbolStack.push(nowSymbol);
//        throw new NotImplementedException();
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
//        throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        this.symbolTable = table;
//        throw new NotImplementedException();
    }

    public List<Instruction> getIR() {
        // TODO
        return IRList;
//        throw new NotImplementedException();
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

