package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum Reg{
    t0,t1,t2,t3,t4,t5,t6,a0
}

enum Inst{
    add,addi,sub,subi,mul,mv,li
}

/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {
    private List<Instruction> instructions = new ArrayList<>();
    private BMap<IRVariable, Reg> varRagMap = new BMap<>();
    private Map<IRVariable,Integer> lastUseValueMap = new HashMap<>();
    private List<String> finalCode = new ArrayList<>();

    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO:
        int rnt = 0;
        for(Instruction inst:originInstructions){
            if(inst.getKind().isBinary()){
                var operands = inst.getOperands();
                int num = 0;
                for(IRValue v:operands){
                    if(v.isImmediate()) num++;
                }
                switch (num) {
                    case 0 -> instructions.add(inst);
                    case 1 -> {
                            for(IRValue src : operands){
                                if(src.isImmediate()){
                                    int index = operands.indexOf(src);
                                    IRValue elseSrc = operands.get((index + 1) % 2);
                                    switch (inst.getKind()) {
                                        case MUL -> {
                                            IRVariable tempVar = IRVariable.temp();
                                            Instruction movInst = Instruction.createMov(tempVar, src);
                                            instructions.add(movInst);
                                            Instruction mulInst = Instruction.createMul(inst.getResult(),elseSrc,tempVar);
                                            instructions.add(mulInst);
                                        }
                                        case SUB -> {
                                            if(inst.getLHS().isImmediate()){
                                                IRVariable tempVar = IRVariable.temp();
                                                Instruction movInst = Instruction.createMov(tempVar, src);
                                                instructions.add(movInst);
                                                Instruction subInst = Instruction.createSub(inst.getResult(),tempVar,elseSrc);
                                                instructions.add(subInst);
                                            } else {
                                                Instruction subInst = Instruction.createSub(inst.getResult(),elseSrc,src);
                                                instructions.add(subInst);
                                            }
                                        }
                                        case ADD -> {
                                            Instruction addInst = Instruction.createAdd(inst.getResult(),elseSrc,src);
                                            instructions.add(addInst);
                                        }
                                    }
                                }
                            }
                    }
                    case 2 -> {
                        int result = 0;
                        int LSrc = ((IRImmediate)operands.get(0)).getValue();
                        int RSrc = ((IRImmediate)operands.get(1)).getValue();
                        switch (inst.getKind()) {
                            case ADD -> result = LSrc + RSrc;
                            case SUB -> result = LSrc - RSrc;
                            case MUL -> result = LSrc * RSrc;
                        }
                        IRImmediate resultSrc = IRImmediate.of(result);
                        Instruction movInst = Instruction.createMov(IRVariable.temp(),resultSrc);
                        instructions.add(movInst);
                    }
                }
            } else {
                instructions.add(inst);
                if (inst.getKind().isReturn()) break;
            }
        }
        int num = 0;
        for(Instruction inst:instructions) {
            var operands = inst.getOperands();
            for(IRValue value:operands) {
                if(value.isIRVariable()) lastUseValueMap.put((IRVariable) value,num);
            }
            num++;
        }
//        for(Instruction inst:instructions) System.out.println(inst);
//        throw new NotImplementedException();
    }

    private void distributeReg(IRVariable value,int index) {
        if(!varRagMap.containsKey(value)) {
            boolean flag = false;
            //有空闲，直接分配空闲
            for (Reg reg : Reg.values()) {
                if(reg == Reg.a0) continue;
                if (!varRagMap.containsValue(reg)) {
                    varRagMap.replace(value, reg);
                    flag = true;
                    break;
                }
            }
            //无空闲，找以后不用
            if (!flag) {
                for(Reg reg : Reg.values()) {
                    IRVariable variable = varRagMap.getByValue(reg);
                    if(lastUseValueMap.get(variable) < index) {
                        varRagMap.replace(value,reg);
                        flag = true;
                        break;
                    }
                }
            }
            if(!flag) throw new RuntimeException();
        }
    }


    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
        int index = 0;
        for(Instruction inst:instructions){
            for(IRValue value:inst.getOperands()) {
                if(value.isIRVariable()) {
                    distributeReg((IRVariable) value,index);
                }
            }
            if(inst.getKind() != InstructionKind.RET) distributeReg(inst.getResult(),index);
            switch (inst.getKind()){
                case MOV -> {
                    Reg rd = varRagMap.getByKey(inst.getResult());
                    if(inst.getFrom().isIRVariable()){
                        Inst mv = Inst.mv;
                        Reg rs = varRagMap.getByKey((IRVariable) inst.getFrom());
                        finalCode.add(mv +" "+rd.toString()+", "+rs.toString());
                    }else{
                        Inst li = Inst.li;
                        int imm = ((IRImmediate)inst.getFrom()).getValue();
                        finalCode.add(li +" "+rd.toString()+", "+ imm);
                    }
                }
                case ADD -> {
                    Reg rd = varRagMap.getByKey(inst.getResult());
                    Reg rs1 = varRagMap.getByKey((IRVariable) inst.getLHS());
                    if(inst.getRHS().isIRVariable()){
                        Inst add = Inst.add;
                        Reg rs2 = varRagMap.getByKey((IRVariable) inst.getRHS());
                        finalCode.add(add +" "+rd.toString()+", "+rs1.toString()+", "+rs2.toString());
                    }else {
                        Inst addi = Inst.addi;
                        int imm = ((IRImmediate)inst.getRHS()).getValue();
                        finalCode.add(addi +" "+rd.toString()+", "+rs1.toString()+", "+ imm);
                    }
                }
                case SUB -> {
                    Reg rd = varRagMap.getByKey(inst.getResult());
                    Reg rs1 = varRagMap.getByKey((IRVariable) inst.getLHS());
                    if(inst.getRHS().isIRVariable()){
                        Inst sub = Inst.sub;
                        Reg rs2 = varRagMap.getByKey((IRVariable) inst.getRHS());
                        finalCode.add(sub +" "+rd.toString()+", "+rs1.toString()+", "+rs2.toString());
                    }else {
                        Inst subi = Inst.subi;
                        int imm = ((IRImmediate)inst.getRHS()).getValue();
                        finalCode.add(subi +" "+rd.toString()+", "+rs1.toString()+", "+ imm);
                    }
                }
                case MUL -> {
                    Inst mul = Inst.mul;
                    Reg rd = varRagMap.getByKey(inst.getResult());
                    Reg rs1 = varRagMap.getByKey((IRVariable) inst.getLHS());
                    Reg rs2 = varRagMap.getByKey((IRVariable) inst.getRHS());
                    finalCode.add(mul +" "+rd.toString()+", "+rs1.toString()+", "+rs2.toString());
                }
                case RET -> {
                    Inst mv = Inst.mv;
                    Reg rd = Reg.a0;
                    Reg rs = varRagMap.getByKey((IRVariable) inst.getReturnValue());
                    finalCode.add(mv +" "+rd+", "+rs.toString());
                }
            }
            index++;
        }
//        for(String fc:finalCode){
//            System.out.println(fc);
//        }
//        throw new NotImplementedException();
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path))) {
            bufferedWriter.write(".text\n");
            for(String fc:finalCode){
                bufferedWriter.write("\t"+fc+"\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        throw new NotImplementedException();
    }
}

