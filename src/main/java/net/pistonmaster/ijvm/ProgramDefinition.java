package net.pistonmaster.ijvm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ProgramDefinition(byte[] constantPool, byte[] methodArea) {
    public static void test() {
        System.out.println(new ProgramDefinitionBuilder()
                .addMethod("main", new MethodBodyBuilder(
                        List.of(),
                        List.of(),
                        new ArrayList<>()
                ))
                .link());
    }

    public static class ProgramDefinitionBuilder {
        private final Map<String, Integer> constants = new LinkedHashMap<>();
        private final Map<String, MethodBodyBuilder> methods = new LinkedHashMap<>();

        public ProgramDefinitionBuilder putConstant(String name, int value) {
            if (constants.containsKey(name)) {
                throw new IllegalArgumentException("Constant already exists!");
            }

            constants.put(name, value);
            return this;
        }

        public ProgramDefinitionBuilder addMethod(String name, MethodBodyBuilder body) {
            if (methods.containsKey(name)) {
                throw new IllegalArgumentException("Method already exists!");
            }

            methods.put(name, body);
            return this;
        }

        public ProgramDefinition link() {
            return new ProgramDefinition(new byte[0], new byte[0]); // TODO: Implement linking
        }
    }

    public record MethodBodyBuilder(List<String> parameterNames, List<String> localVariableNames,
                                    List<MethodByteResolvable> bytes) {
        private boolean needsWide(int number) {
            return number > Byte.MAX_VALUE;
        }

        private int getVariableIndex(String name) {
            int index = parameterNames.indexOf(name);

            if (index == -1) {
                index = localVariableNames.indexOf(name);

                if (index == -1) {
                    throw new IllegalArgumentException("Variable not found!");
                }

                index += parameterNames.size();
            }

            return index;
        }

        public MethodBodyBuilder addBIPUSH(int value) {
            bytes.add(new MethodInstruction(Instruction.BIPUSH));
            bytes.add(new ParameterData(IMemory.ParameterType.BYTE, value));

            return this;
        }

        public MethodBodyBuilder addDUP() {
            bytes.add(new MethodInstruction(Instruction.DUP));

            return this;
        }

        public MethodBodyBuilder addGOTO(int offset) {
            bytes.add(new MethodInstruction(Instruction.GOTO));
            bytes.add(new ParameterData(IMemory.ParameterType.OFFSET, offset));

            return this;
        }

        public MethodBodyBuilder addIADD() {
            bytes.add(new MethodInstruction(Instruction.IADD));

            return this;
        }

        public MethodBodyBuilder addIAND() {
            bytes.add(new MethodInstruction(Instruction.IAND));

            return this;
        }

        public MethodBodyBuilder addIFEQ(int offset) {
            bytes.add(new MethodInstruction(Instruction.IFEQ));
            bytes.add(new ParameterData(IMemory.ParameterType.OFFSET, offset));

            return this;
        }

        public MethodBodyBuilder addIFLT(int offset) {
            bytes.add(new MethodInstruction(Instruction.IFLT));
            bytes.add(new ParameterData(IMemory.ParameterType.OFFSET, offset));

            return this;
        }

        public MethodBodyBuilder addIF_ICMPEQ(int offset) {
            bytes.add(new MethodInstruction(Instruction.IF_ICMPEQ));
            bytes.add(new ParameterData(IMemory.ParameterType.OFFSET, offset));

            return this;
        }

        public MethodBodyBuilder addIINC(String variable, String constantName) {
            var varIndex = getVariableIndex(variable);
            var wide = needsWide(varIndex);
            if (wide) {
                bytes.add(new MethodInstruction(Instruction.WIDE));
            }

            bytes.add(new MethodInstruction(Instruction.IINC));
            bytes.add(new ParameterData(wide ? IMemory.ParameterType.VAR_NUM_WIDE : IMemory.ParameterType.VAR_NUM, varIndex));
            bytes.add(new ConstantPoolResolvableVariable(constantName));

            return this;
        }

        public MethodBodyBuilder addILOAD(String variable) {
            var varIndex = getVariableIndex(variable);
            var wide = needsWide(varIndex);
            if (wide) {
                bytes.add(new MethodInstruction(Instruction.WIDE));
            }

            bytes.add(new MethodInstruction(Instruction.ILOAD));
            bytes.add(new ParameterData(wide ? IMemory.ParameterType.VAR_NUM_WIDE : IMemory.ParameterType.VAR_NUM, varIndex));

            return this;
        }

        public MethodBodyBuilder addINVOKEVIRTUAL(String methodName) {
            bytes.add(new MethodInstruction(Instruction.INVOKEVIRTUAL));
            bytes.add(new ConstantPoolResolvableMethod(methodName));

            return this;
        }

        public MethodBodyBuilder addIOR() {
            bytes.add(new MethodInstruction(Instruction.IOR));

            return this;
        }

        public MethodBodyBuilder addIRETURN() {
            bytes.add(new MethodInstruction(Instruction.IRETURN));

            return this;
        }

        public MethodBodyBuilder addISTORE(String variable) {
            var varIndex = getVariableIndex(variable);
            var wide = needsWide(varIndex);
            if (wide) {
                bytes.add(new MethodInstruction(Instruction.WIDE));
            }

            bytes.add(new MethodInstruction(Instruction.ISTORE));
            bytes.add(new ParameterData(wide ? IMemory.ParameterType.VAR_NUM_WIDE : IMemory.ParameterType.VAR_NUM, varIndex));

            return this;
        }

        public MethodBodyBuilder addISUB() {
            bytes.add(new MethodInstruction(Instruction.ISUB));

            return this;
        }

        public MethodBodyBuilder addLDC_W(int constant) {
            bytes.add(new MethodInstruction(Instruction.LDC_W));
            bytes.add(new ParameterData(IMemory.ParameterType.INDEX, constant));

            return this;
        }

        public MethodBodyBuilder addNOP() {
            bytes.add(new MethodInstruction(Instruction.NOP));

            return this;
        }

        public MethodBodyBuilder addPOP() {
            bytes.add(new MethodInstruction(Instruction.POP));

            return this;
        }

        public MethodBodyBuilder addSWAP() {
            bytes.add(new MethodInstruction(Instruction.SWAP));

            return this;
        }
    }

    public interface MethodByteResolvable {
    }

    public record MethodInstruction(Instruction instruction) implements MethodByteResolvable {
    }

    public record ParameterData(IMemory.ParameterType type, int value) implements MethodByteResolvable {
    }

    public record ConstantPoolResolvableMethod(String methodName) implements MethodByteResolvable {
    }

    public record ConstantPoolResolvableVariable(String constantName) implements MethodByteResolvable {
    }
}
