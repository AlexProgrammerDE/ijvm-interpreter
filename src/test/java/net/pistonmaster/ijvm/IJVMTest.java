package net.pistonmaster.ijvm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class IJVMTest {
    @Test
    public void memoryReadTest() {
        var memory = new ProgramMemory(new byte[]{
                0x00,
                0x10,
                0x00,
                0x40,
        });

        Assertions.assertEquals(0x00100040, memory.readBigEndianInt(0x00000000));
    }

    @Test
    public void serializationIntTest() {
        var memory = new ProgramMemory(128);

        var firstTestValue = Integer.MIN_VALUE;
        memory.writeBigEndianInt(0x00000000, firstTestValue);
        Assertions.assertEquals(firstTestValue, memory.readBigEndianInt(0x00000000));

        var secondTestValue = Integer.MAX_VALUE;
        memory.writeBigEndianInt(0x00000004, secondTestValue);
        Assertions.assertEquals(secondTestValue, memory.readBigEndianInt(0x00000004));
    }

    @Test
    public void serializationShortTest() {
        var memory = new ProgramMemory(128);

        var firstTestValue = Short.MIN_VALUE;
        memory.writeBigEndianShort(0x00000000, firstTestValue);
        Assertions.assertEquals(firstTestValue, memory.readBigEndianShort(0x00000000));

        var secondTestValue = Short.MAX_VALUE;
        memory.writeBigEndianShort(0x00000002, secondTestValue);
        Assertions.assertEquals(secondTestValue, memory.readBigEndianShort(0x00000002));
    }

    @Test
    public void biPushTest() {
        var processor = new Processor(new byte[0], new byte[]{
                0x00, 0x01, // Parameters
                0x00, 0x00, // Local variables
                Instruction.BIPUSH.getOpcode(),
                0x05,
        }, 0);
        processor.run();

        Assertions.assertEquals(0x05, processor.stack.readBigEndianInt(0x00000008));
    }

    @Test
    public void simpleAddProgramTest() {
        var program = new ProgramDefinition.ProgramDefinitionBuilder()
                .addMethod("main", new ProgramDefinition.MethodBodyBuilder(List.of(), List.of())
                        .addBIPUSH(5)
                        .addBIPUSH(3)
                        .addIADD()
                        .addIRETURN()
                )
                .link();
        var processor = new Processor(program, "main");
        processor.run();

        Assertions.assertEquals(0x08, processor.stack.readBigEndianInt(0x00000000));
    }

    @Test
    public void simpleSubProgramTest() {
        var program = new ProgramDefinition.ProgramDefinitionBuilder()
                .addMethod("main", new ProgramDefinition.MethodBodyBuilder(List.of(), List.of())
                        .addBIPUSH(5)
                        .addBIPUSH(3)
                        .addISUB()
                        .addIRETURN()
                )
                .link();
        var processor = new Processor(program, "main");
        processor.run();

        Assertions.assertEquals(0x02, processor.stack.readBigEndianInt(0x00000000));
    }

    @Test
    public void simpleLocalVarProgramTest() {
        var program = new ProgramDefinition.ProgramDefinitionBuilder()
                .addMethod("main", new ProgramDefinition.MethodBodyBuilder(List.of(), List.of("x"))
                        .addBIPUSH(5)
                        .addISTORE("x")
                        .addBIPUSH(3)
                        .addILOAD("x")
                        .addIRETURN()
                )
                .link();
        var processor = new Processor(program, "main");
        processor.run();

        Assertions.assertEquals(0x05, processor.stack.readBigEndianInt(0x00000000));
    }

    @Test
    public void complexInvokeProgramTest() {
        var program = new ProgramDefinition.ProgramDefinitionBuilder()
                .addMethod("main", new ProgramDefinition.MethodBodyBuilder(List.of(), List.of("x"))
                        .addDUP() // OBJREF
                        .addBIPUSH(5)
                        .addINVOKEVIRTUAL("test")
                        .addISTORE("x")
                        .addILOAD("x")
                        .addIRETURN()
                )
                .addMethod("test", new ProgramDefinition.MethodBodyBuilder(List.of("x"), List.of("a"))
                        .addBIPUSH(5)
                        .addILOAD("x")
                        .addIADD()
                        .addISTORE("a")
                        .addILOAD("a")
                        .addIRETURN()
                )
                .link();
        var processor = new Processor(program, "main");
        processor.run();

        Assertions.assertEquals(10, processor.stack.readBigEndianInt(0x00000000));
    }
}
