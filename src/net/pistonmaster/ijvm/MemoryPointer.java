package ijvm;

public class MemoryPointer {
    // How many words per element in the stack and constant pool
    public static final int WORD_SIZE = 4;
    private final ProgramMemory memory;
    private int pointer;

    public MemoryPointer(ProgramMemory memory) {
        this.memory = memory;
        this.pointer = 0;
    }

    public int currentPointer() {
        return pointer;
    }

    public void increment() {
        pointer++;
    }

    public void setPointer(int pointer) {
        this.pointer = pointer;
    }

    public void movePointer(int offset) {
        pointer += offset;
    }

    public int popWord() {
        int value = memory.readLittleEndianInt(pointer);
        pointer -= WORD_SIZE;
        return value;
    }

    public void pushWord(int value) {
        pointer += WORD_SIZE;
        memory.writeLittleEndianInt(pointer, value);
    }
}
