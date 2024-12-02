package ijvm;

public enum Instruction {
    BIPUSH((byte) 0x10),
    DUP((byte) 0x59),
    GOTO((byte) 0xA7),
    IADD((byte) 0x60),
    IAND((byte) 0x7E),
    IFEQ((byte) 0x99),
    IFLT((byte) 0x9B),
    IF_ICMPEQ((byte) 0x9F),
    IINC((byte) 0x84),
    ILOAD((byte) 0x15),
    INVOKEVIRTUAL((byte) 0xB6),
    IOR((byte) 0x80),
    IRETURN((byte) 0xAC),
    ISTORE((byte) 0x36),
    ISUB((byte) 0x64),
    LDC_W((byte) 0x13),
    NOP((byte) 0x00),
    POP((byte) 0x57),
    SWAP((byte) 0x5F),
    WIDE((byte) 0xC4);

    private static final Instruction[] VALUES = values();
    private final byte opcode;

    Instruction(byte opcode) {
        this.opcode = opcode;
    }

    public byte getOpcode() {
        return opcode;
    }

    public static Instruction fromOpcode(byte opcode) {
        for (Instruction instruction : VALUES) {
            if (instruction.getOpcode() == opcode) {
                return instruction;
            }
        }

        throw new IllegalArgumentException("Unknown opcode: " + opcode);
    }
}
