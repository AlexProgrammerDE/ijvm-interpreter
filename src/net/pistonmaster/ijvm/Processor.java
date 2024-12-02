package net.pistonmaster.ijvm;

import java.util.function.BinaryOperator;

public class Processor {
    public final ProgramMemory constantPool = new ProgramMemory(128, false);
    public final ProgramMemory stack = new ProgramMemory(128, true);
    public final ProgramMemory methodArea = new ProgramMemory(128, false);
    // CPP = Constant Pool Pointer
    public final MemoryPointer constantPoolPointer = new MemoryPointer(constantPool);
    // SP = Stack Pointer
    public final MemoryPointer stackPointer = new MemoryPointer(stack);
    // LV = Local Variable Pointer
    public final MemoryPointer localVariablePointer = new MemoryPointer(stack);
    // PC = Program Counter
    public final MemoryPointer methodAreaPointer = new MemoryPointer(methodArea);

    @SuppressWarnings("StatementWithEmptyBody")
    public void run() {
        while (!tick()) {
            // Do nothing
        }
    }

    public boolean tick() {
        return tick(false);
    }

    public boolean tick(boolean wide) {
        if (methodAreaPointer.currentPointer() >= methodArea.storage.length) {
            System.out.println("Program finished");
            return true;
        }

        byte opcode = methodArea.readByte(methodAreaPointer.currentPointer());
        Instruction instruction = Instruction.fromOpcode(opcode);
        switch (instruction) {
            case BIPUSH -> {
                stackPointer.pushWord(methodArea.readByte(methodAreaPointer.currentPointer() + 1));

                // BIPUSH <byte>
                methodAreaPointer.movePointer(2);
            }
            case DUP -> {
                stackPointer.pushWord(stack.readBigEndianInt(stackPointer.currentPointer()));

                // DUP
                methodAreaPointer.increment();
            }
            case GOTO -> {
                var offset = methodArea.readOffset(methodAreaPointer.currentPointer() + 1);
                methodAreaPointer.movePointer(offset);

                // GOTO <offset-part-1> <offset-part-2>
                methodAreaPointer.movePointer(3);
            }
            case IADD -> operationWithTwoWords(Integer::sum);
            case IAND -> operationWithTwoWords((left, right) -> left & right);
            case IFEQ -> {
                var offset = methodArea.readOffset(methodAreaPointer.currentPointer() + 1);
                var value = stackPointer.popWord();
                if (value == 0) {
                    methodAreaPointer.movePointer(offset);
                } else {
                    // IFEQ <offset-part-1> <offset-part-2>
                    methodAreaPointer.movePointer(3);
                }
            }
            case IFLT -> {
                var offset = methodArea.readOffset(methodAreaPointer.currentPointer() + 1);
                var value = stackPointer.popWord();
                if (value < 0) {
                    methodAreaPointer.movePointer(offset);
                } else {
                    // IFLT <offset-part-1> <offset-part-2>
                    methodAreaPointer.movePointer(3);
                }
            }
            case IF_ICMPEQ -> {
                var offset = methodArea.readOffset(methodAreaPointer.currentPointer() + 1);
                var value1 = stackPointer.popWord();
                var value2 = stackPointer.popWord();
                if (value1 == value2) {
                    methodAreaPointer.movePointer(offset);
                } else {
                    // IF_ICMPEQ <offset-part-1> <offset-part-2>
                    methodAreaPointer.movePointer(3);
                }
            }
            case IINC -> {
                var index = methodArea.readVarNum(methodAreaPointer.currentPointer() + 1, wide);
                var value = methodArea.readConst(methodAreaPointer.currentPointer() + 2);
                var lvIndex = localVariablePointer.currentPointer() + (index * MemoryPointer.WORD_SIZE);
                var currentValue = stack.readBigEndianInt(lvIndex);
                stack.writeBigEndianInt(lvIndex, currentValue + value);

                // IINC <index> <value> OR IINC <index-part-1> <index-part-2> <value>
                methodAreaPointer.movePointer(wide ? 4 : 3);
            }
            case ILOAD -> {
                var index = methodArea.readVarNum(methodAreaPointer.currentPointer() + 1, wide);
                var value = stack.readBigEndianInt(localVariablePointer.currentPointer() + (index * MemoryPointer.WORD_SIZE));

                stackPointer.pushWord(value);

                // ILOAD <index> OR ILOAD <index-part-1> <index-part-2>
                methodAreaPointer.movePointer(wide ? 3 : 2);
            }
            case INVOKEVIRTUAL -> {
                // TODO: Implement INVOKEVIRTUAL
            }
            case IOR -> operationWithTwoWords((left, right) -> left | right);
            case IRETURN -> {
                // TODO: Implement IRETURN
            }
            case ISTORE -> {
                var index = methodArea.readVarNum(methodAreaPointer.currentPointer() + 1, wide);
                var value = stackPointer.popWord();

                stack.writeBigEndianInt(localVariablePointer.currentPointer() + (index * MemoryPointer.WORD_SIZE), value);

                // ISTORE <index> OR ISTORE <index-part-1> <index-part-2>
                methodAreaPointer.movePointer(wide ? 3 : 2);
            }
            case ISUB -> operationWithTwoWords((left, right) -> left - right);
            case LDC_W -> {
                var constantPoolIndex = methodArea.readIndex(methodAreaPointer.currentPointer() + 1);
                var value = constantPool.readBigEndianInt(constantPoolIndex);
                stackPointer.pushWord(value);

                // LDC_W <index-part-1> <index-part-2>
                methodAreaPointer.movePointer(3);
            }
            case NOP -> methodAreaPointer.increment(); // NOP
            case POP -> {
                stackPointer.popWord(); // Ignore the value

                // POP
                methodAreaPointer.increment();
            }
            case SWAP -> {
                var first = stackPointer.popWord();
                var second = stackPointer.popWord();
                stackPointer.pushWord(first);
                stackPointer.pushWord(second);

                // SWAP
                methodAreaPointer.increment();
            }
            case WIDE -> {
                // WIDE
                methodAreaPointer.increment();
                return tick(true);
            }
            default -> throw new IllegalStateException("Unsupported value: " + instruction);
        }

        return false;
    }

    private void operationWithTwoWords(BinaryOperator<Integer> operator) {
        var right = stackPointer.popWord();
        var left = stackPointer.popWord();
        stackPointer.pushWord(operator.apply(left, right));

        // OPERATION NAME
        methodAreaPointer.increment();
    }
}
