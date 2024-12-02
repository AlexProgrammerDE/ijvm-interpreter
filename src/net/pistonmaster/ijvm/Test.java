package ijvm;

public class Test {
    public static void main(String[] args) {
        var processor = new Processor();
        processor.methodArea.writeBytes(0, new byte[]{
                0x00,
                Instruction.BIPUSH.getOpcode(),
                0x05,
        });
        processor.constantPoolPointer.setPointer(0);
        processor.stackPointer.setPointer(1);
        processor.localVariablePointer.setPointer(0);
        processor.methodAreaPointer.setPointer(0);
        processor.run();
        System.out.println(toHex(processor.stack.readLittleEndianInt(5)));

        // ProgramMemory memory = new ProgramMemory(128, false);
        // memory.write(0x00000010, (byte) 0x00);
        // memory.write(0x00000011, (byte) 0x10);
        // memory.write(0x00000012, (byte) 0x00);
        // memory.write(0x00000013, (byte) 0x40);
        // System.out.println(memory.readLittleEndian(0x00000010));
    }

    private static String toHex(int value) {
        return String.format("0x%02X", value);
    }
}
