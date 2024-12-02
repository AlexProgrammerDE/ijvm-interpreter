package net.pistonmaster.ijvm;

public class ProgramMemory {
    private final boolean canGrow;
    public byte[] storage;

    public ProgramMemory(int baseSize, boolean canGrow) {
        this.canGrow = canGrow;
        this.storage = new byte[baseSize];
    }

    public void ensureCapacity(int capacity) {
        if (canGrow && storage.length < capacity) {
            byte[] newStorage = new byte[capacity];
            System.arraycopy(storage, 0, newStorage, 0, storage.length);
            storage = newStorage;
        }
    }

    public void writeByte(int address, byte value) {
        ensureCapacity(address + 1);
        storage[address] = value;
    }

    public byte readByte(int address) {
        ensureCapacity(address + 1);
        return storage[address];
    }

    public void writeBytes(int address, byte[] bytes) {
        ensureCapacity(address + bytes.length);
        System.arraycopy(bytes, 0, storage, address, bytes.length);
    }

    /**
     * Read an integer from memory in little-endian format.
     *
     * @param address The address to read the integer from.
     * @return The integer read from memory.
     */
    public int readLittleEndianInt(int address) {
        ensureCapacity(address + 4);
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value |= (storage[address + i] & 0xFF) << (i * 8);
        }
        return value;
    }

    /**
     * Write an integer to memory in little-endian format.
     *
     * @param address The address to write the integer to.
     * @param value The integer to write to memory.
     */
    public void writeLittleEndianInt(int address, int value) {
        ensureCapacity(address + 4);
        for (int i = 0; i < 4; i++) {
            storage[address + i] = (byte) ((value >> (i * 8)) & 0xFF);
        }
    }

    /**
     * Write a short to memory in little-endian format.
     *
     * @param address The address to write the short to.
     * @param value The short to write to memory.
     */
    public void writeLittleEndianShort(int address, short value) {
        ensureCapacity(address + 2);
        for (int i = 0; i < 2; i++) {
            storage[address + i] = (byte) ((value >> (i * 8)) & 0xFF);
        }
    }

    /**
     * Read a short from memory in little-endian format.
     *
     * @param address The address to read the short from.
     * @return The short read from memory.
     */
    public short readLittleEndianShort(int address) {
        ensureCapacity(address + 2);
        short value = 0;
        for (int i = 0; i < 2; i++) {
            value |= (short) ((storage[address + i] & 0xFF) << (i * 8));
        }
        return value;
    }

    public int readVarNum(int address, boolean wide) {
        if (wide) {
            return readIndex(address);
        }

        return MathHelper.maskSign(readByte(address));
    }

    public int readIndex(int address) {
        return MathHelper.maskSign(readLittleEndianShort(address));
    }

    public byte readConst(int address) {
        return readByte(address);
    }

    public short readOffset(int address) {
        return readLittleEndianShort(address);
    }
}
