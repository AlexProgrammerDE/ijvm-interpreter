package net.pistonmaster.ijvm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IJVMTest {
    @Test
    public void memoryReadTest() {
        var memory = new ProgramMemory(128, false);
        memory.writeByte(0x00000010, (byte) 0x00);
        memory.writeByte(0x00000011, (byte) 0x10);
        memory.writeByte(0x00000012, (byte) 0x00);
        memory.writeByte(0x00000013, (byte) 0x40);

        Assertions.assertEquals(0x00100040, memory.readBigEndianInt(0x00000010));
    }

    @Test
    public void serializationIntTest() {
        var memory = new ProgramMemory(128, false);

        var firstTestValue = Integer.MIN_VALUE;
        memory.writeBigEndianInt(0x00000000, firstTestValue);
        Assertions.assertEquals(firstTestValue, memory.readBigEndianInt(0x00000000));

        var secondTestValue = Integer.MAX_VALUE;
        memory.writeBigEndianInt(0x00000004, secondTestValue);
        Assertions.assertEquals(secondTestValue, memory.readBigEndianInt(0x00000004));
    }

    @Test
    public void serializationShortTest() {
        var memory = new ProgramMemory(128, false);

        var firstTestValue = Short.MIN_VALUE;
        memory.writeBigEndianShort(0x00000000, firstTestValue);
        Assertions.assertEquals(firstTestValue, memory.readBigEndianShort(0x00000000));

        var secondTestValue = Short.MAX_VALUE;
        memory.writeBigEndianShort(0x00000002, secondTestValue);
        Assertions.assertEquals(secondTestValue, memory.readBigEndianShort(0x00000002));
    }

    @Test
    public void biPushTest() {
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

        Assertions.assertEquals(0x05, processor.stack.readBigEndianInt(5));
    }
}
