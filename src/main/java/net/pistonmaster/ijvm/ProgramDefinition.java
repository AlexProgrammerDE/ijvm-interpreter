package net.pistonmaster.ijvm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a program definition.
 *
 * @param constantPool The constant pool of the program.
 * @param methodArea The method area of the program.
 * @param constants Pointer positions of constants by name.
 * @param methods Pointer positions of methods by name.
 */
public record ProgramDefinition(
        byte[] constantPool,
        byte[] methodArea,
        Map<String, Integer> constants,
        Map<String, Integer> methods
) {
    public static class ProgramDefinitionBuilder {
        private final Map<String, Integer> constants = new LinkedHashMap<>();
        private final Map<String, MethodBodyBuilder> methods = new LinkedHashMap<>();

        public ProgramDefinitionBuilder putConstant(String name, int value) {
            if (constants.containsKey(name)) {
                throw new IllegalArgumentException("Constant already exists!");
            }

            if (methods.containsKey(name)) {
                throw new IllegalArgumentException("Constant name is already a method!");
            }

            constants.put(name, value);
            return this;
        }

        public ProgramDefinitionBuilder addMethod(String name, MethodBodyBuilder body) {
            if (methods.containsKey(name)) {
                throw new IllegalArgumentException("Method already exists!");
            }

            if (constants.containsKey(name)) {
                throw new IllegalArgumentException("Method name is already a constant!");
            }

            methods.put(name, body);
            return this;
        }

        public ProgramDefinition link() {
            var constantPool = new ProgramMemory(0);
            Map<String, Integer> constantAddresses = new LinkedHashMap<>();

            for (var constant : constants.entrySet()) {
                constantAddresses.put(constant.getKey(), constantPool.storage.length);
                constantPool.writeBigEndianInt(constantPool.storage.length, constant.getValue());
            }

            var methodArea = new ProgramMemory(0);
            Map<String, Integer> methodAddresses = new LinkedHashMap<>();
            Map<Integer, String> methodLinkPositions = new LinkedHashMap<>();

            for (var method : methods.entrySet()) {
                var methodIndex = methodArea.storage.length;
                methodAddresses.put(method.getKey(), methodIndex);

                constantAddresses.put(method.getKey(), constantPool.storage.length);
                constantPool.writeBigEndianInt(constantPool.storage.length, methodIndex);

                // Parameters + 1 for OBJREF
                methodArea.writeUnsignedBigEndianShort(methodArea.storage.length, method.getValue().parameterNames.size() + 1);

                // Local variables
                methodArea.writeUnsignedBigEndianShort(methodArea.storage.length, method.getValue().localVariableNames.size());

                for (var byteResolvable : method.getValue().bytes) {
                    switch (byteResolvable) {
                        case MethodInstruction instruction ->
                                methodArea.writeByte(methodArea.storage.length, instruction.instruction.getOpcode());
                        case ParameterData parameterData ->
                                methodArea.writeType(methodArea.storage.length, parameterData.type, parameterData.value);
                        case ConstantPoolResolvableMethod constantPoolResolvableMethod -> {
                            methodLinkPositions.put(methodArea.storage.length, constantPoolResolvableMethod.methodName);

                            // Write some dummy data so the method can be linked later
                            methodArea.writeUnsignedBigEndianShort(methodArea.storage.length, 0);
                        }
                        case ConstantPoolResolvableVariable constantPoolResolvableVariable ->
                                methodArea.writeUnsignedBigEndianShort(methodArea.storage.length, constantAddresses.get(constantPoolResolvableVariable.constantName));
                    }
                }
            }

            for (var entry : methodLinkPositions.entrySet()) {
                methodArea.writeUnsignedBigEndianShort(entry.getKey(), constantAddresses.get(entry.getValue()));
            }

            return new ProgramDefinition(
                    constantPool.copyStorage(),
                    methodArea.copyStorage(),
                    constantAddresses,
                    methodAddresses
            );
        }
    }

    public record MethodBodyBuilder(List<String> parameterNames, List<String> localVariableNames,
                                    List<MethodByteResolvable> bytes) {
        public MethodBodyBuilder(List<String> parameterNames, List<String> localVariableNames) {
            this(parameterNames, localVariableNames, new ArrayList<>());
        }

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

            // Index + 1 for OBJREF
            return index + 1;
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

    public sealed interface MethodByteResolvable permits MethodInstruction, ParameterData, ConstantPoolResolvableMethod, ConstantPoolResolvableVariable {
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
