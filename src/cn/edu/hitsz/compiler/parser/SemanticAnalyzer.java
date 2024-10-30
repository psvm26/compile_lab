package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Symbol;
import cn.edu.hitsz.compiler.parser.table.Term;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.symtab.SymbolTableEntry;

import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {
    private SymbolTable symbolTable;
    private Stack<Symbol> symbolStack = new Stack<>(); //符号栈

    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作
//        throw new NotImplementedException();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        Symbol nowSymbol = new Symbol(production.head());
        switch (production.index()){
            case 4->{   //S -> D id
                Symbol topSymbol1 = symbolStack.pop();  //id
                Symbol topSymbol2 = symbolStack.pop();
                String symbol = topSymbol1.getToken().getText();
                if(symbolTable.has(symbol)){
                    SymbolTableEntry symbolTableEntry = symbolTable.get(symbol);
                    symbolTableEntry.setType(topSymbol2.getType());
                    symbolStack.push(nowSymbol);
                }else {
                    System.out.println(symbol);
                    throw new NotImplementedException();
                }
            }
            case 5->{   //D -> int
                Symbol topSymbol = symbolStack.pop();
                nowSymbol.setType(topSymbol.getType());
                symbolStack.push(nowSymbol);
            }
            default -> {
                for(Term ignore: production.body()){
                    symbolStack.pop();
                }
                symbolStack.push(nowSymbol);
            }
        }
//        throw new NotImplementedException();
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
        Symbol nowSymbol = new Symbol(currentToken);
        if(currentToken.getKindId().equals("int")) {
            nowSymbol.setType(SourceCodeType.Int);
        }
        symbolStack.push(nowSymbol);
//        throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        this.symbolTable = table;
//        throw new NotImplementedException();
    }
}

