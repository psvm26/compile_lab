package cn.edu.hitsz.compiler.parser.table;

import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;

public class Symbol{
    Token token;
    NonTerminal nonTerminal;
    SourceCodeType type;
    IRValue val;

    private Symbol(Token token, NonTerminal nonTerminal){
        this.token = token;
        this.nonTerminal = nonTerminal;
        this.type = null;
        this.val = null;
    }

    public Symbol(Token token){
        this(token, null);
    }

    public Symbol(NonTerminal nonTerminal){
        this(null, nonTerminal);
    }

    public boolean isToken(){
        return this.token != null;
    }

    public boolean isNonterminal(){
        return this.nonTerminal != null;
    }

    public NonTerminal getNonTerminal() {
        return this.nonTerminal;
    }

    public Token getToken() {
        return token;
    }

    public void setType(SourceCodeType type) {
        this.type = type;
    }

    public SourceCodeType getType() {
        return this.type;
    }

    public void setVal(IRValue val) {
        this.val = val;
    }

    public IRValue getVal() {
        return val;
    }
}
