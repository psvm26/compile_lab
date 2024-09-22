package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private String fileString;
    private List<Token> tokenList = new ArrayList<Token>();

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        this.fileString = FileUtils.readFile(path);
//        throw new NotImplementedException();
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
        char[] charArray = this.fileString.toCharArray();
        int nowState = 0;
        int nextState = 0;
        StringBuilder nowString = new StringBuilder();
        int i = 0;
        while (i < charArray.length){
            char c = charArray[i];
            switch (nowState){
                case 0 -> {
                    if(c == ' ' || c == '\t' || c == '\n'){
                        nextState = 0;
                        i ++;
                    } else if (Character.isLetter(c)) {
                        nowString.append(c);
                        nextState = 14;
                        i ++;
                    } else if (Character.isDigit(c)) {
                        nowString.append(c);
                        nextState = 16;
                        i ++;
                    } else if (c == '*') {
                        nextState = 18;
                        i ++;
                    } else if (c == '=') {
                        nextState = 21;
                        i ++;
                    } else if (c == '"') {
                        nextState = 24;
                        i ++;
                    } else if (c == '(') {
                        nextState = 26;
                    } else if (c == ')') {
                        nextState = 27;
                    } else if (c == ';') {
                        nextState = 28;
                    } else if (c == '+') {
                        nextState = 29;
                    } else if (c == '-') {
                        nextState = 30;
                    } else if (c == '/') {
                        nextState = 31;
                    } else if (c == ',') {
                        nextState = 32;
                    }
                }
                case 14 -> {
                    if (Character.isDigit(c) || Character.isLetter(c) || c == '_') {
                        nowString.append(c);
                        nextState = 14;
                        i ++;
                    }else {
                        nextState = 15;
                    }
                }
                case 15 -> {
                    //idCode终态
                    String id = nowString.toString();
                    if(TokenKind.isAllowed(id)) {
                        tokenList.add(Token.simple(id));
                    }else {
                        tokenList.add(Token.normal("id", id));
                        symbolTable.add(id);
                    }
                    nextState = 0;
                    nowString = new StringBuilder();
                }
                case 16 -> {
                    if (Character.isDigit(c)) {
                        nowString.append(c);
                        nextState = 16;
                        i ++;
                    }else {
                        nextState = 17;
                    }
                }
                case 17 -> {
                    //UNSIGNED_INT终态
                    tokenList.add(Token.normal("IntConst", nowString.toString()));
                    nextState = 0;
                    nowString = new StringBuilder();
                }
                case 18 -> {
                    if (c == '*') {
                        nextState = 19;
                    }else {
                        nextState = 20;
                    }
                }
                case 19 -> {
                    //EXP终态
                    tokenList.add(Token.simple("**"));
                    nextState = 0;
                    i ++;
                }
                case 20 -> {
                    //MULTI终态
                    tokenList.add(Token.simple("*"));
                    nextState = 0;
                }
                case 21 -> {
                    if (c == '=') {
                        nextState = 22;
                    }else {
                        nextState = 23;
                    }
                }
                case 22 -> {
                    //EQ终态
                    tokenList.add(Token.simple("=="));
                    nextState = 0;
                    i ++;
                }
                case 23 -> {
                    //ASSIGN终态
                    tokenList.add(Token.simple("="));
                    nextState = 0;
                }
                case 24 -> {
                    if (Character.isDigit(c) || Character.isLetter(c)) {
                        nowString.append(c);
                        nextState = 24;
                        i ++;
                    }else if(c == '"') {
                        nextState = 25;
                    }
                }
                case 25 -> {
                    //STR_CONST终态
                    nextState = 0;
                    nowString = new StringBuilder();
                    i ++;
                }
                case 26 -> {
                    //PARENTHE_BEGIN终态
                    tokenList.add(Token.simple("("));
                    nextState = 0;
                    i ++;
                }
                case 27 -> {
                    //BRACKET_END终态
                    tokenList.add(Token.simple(")"));
                    nextState = 0;
                    i ++;
                }
                case 28 -> {
                    //SEMIC终态
                    tokenList.add(Token.simple("Semicolon"));
                    nextState = 0;
                    i ++;
                }
                case 29 -> {
                    //ADD终态
                    tokenList.add(Token.simple("+"));
                    nextState = 0;
                    i ++;
                }
                case 30 -> {
                    //MINUS终态
                    tokenList.add(Token.simple("-"));
                    nextState = 0;
                    i ++;
                }
                case 31 -> {
                    //RDIV终态
                    tokenList.add(Token.simple("/"));
                    nextState = 0;
                    i ++;
                }
                case 32 -> {
                    //COMMA终态
                    tokenList.add(Token.simple(","));
                    nextState = 0;
                    i ++;
                }
            }
            nowState = nextState;
        }
        tokenList.add(Token.simple(TokenKind.eof()));
//        throw new NotImplementedException();
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        return tokenList;
//        throw new NotImplementedException();
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
