package net.pistonmaster.ijvm;

public class ProgramMemory implements IMemory {
    private final boolean canGrow;
    public byte[] storage;

    public ProgramMemory(int baseSize) {
        this.canGrow = true;
        this.storage = new byte[baseSize];
    }

    public ProgramMemory(byte[] storage) {
        this.canGrow = false;
        this.storage = storage;
    }

    public void ensureCapacity(int capacity) {
        if (canGrow && storage.length < capacity) {
            byte[] newStorage = new byte[capacity];
            System.arraycopy(storage, 0, newStorage, 0, storage.length);
            storage = newStorage;
        }
    }

    @Override
    public void writeByte(int address, byte value) {
        ensureCapacity(address + 1);
        storage[address] = value;
    }

    @Override
    public byte readByte(int address) {
        ensureCapacity(address + 1);
        return storage[address];
    }
}
