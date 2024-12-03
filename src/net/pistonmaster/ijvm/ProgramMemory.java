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

    public int readUnsignedByte(int address) {
        return MathHelper.maskSign(readByte(address));
    }

    public void writeBytes(int address, byte[] bytes) {
        ensureCapacity(address + bytes.length);
        System.arraycopy(bytes, 0, storage, address, bytes.length);
    }

    /**
     * Read an integer from memory in big-endian format.
     *
     * @param address The address to read the integer from.
     * @return The integer read from memory.
     */
    public int readBigEndianInt(int address) {
        ensureCapacity(address + 4);
        return (storage[address] << 24)
                | ((storage[address + 1] & 0xFF) << 16)
                | ((storage[address + 2] & 0xFF) << 8)
                | (storage[address + 3] & 0xFF);
    }

    /**
     * Write an integer to memory in big-endian format.
     *
     * @param address The address to write the integer to.
     * @param value The integer to write to memory.
     */
    public void writeBigEndianInt(int address, int value) {
        ensureCapacity(address + 4);
        storage[address] = (byte) (value >> 24);
        storage[address + 1] = (byte) (value >> 16);
        storage[address + 2] = (byte) (value >> 8);
        storage[address + 3] = (byte) value;
    }

    /**
     * Read a short from memory in big-endian format.
     *
     * @param address The address to read the short from.
     * @return The short read from memory.
     */
    public short readBigEndianShort(int address) {
        ensureCapacity(address + 2);
        return (short) ((storage[address] << 8) | (storage[address + 1] & 0xFF));
    }

    public int readUnsignedBigEndianShort(int address) {
        return MathHelper.maskSign(readBigEndianShort(address));
    }

    public int readVarNum(int address, boolean wide) {
        if (wide) {
            return readUnsignedBigEndianShort(address);
        }

        return readUnsignedByte(address);
    }

    public int readIndex(int address) {
        return readUnsignedBigEndianShort(address);
    }

    public int readDisp(int address) {
        return readUnsignedBigEndianShort(address);
    }

    public byte readConst(int address) {
        return readByte(address);
    }

    public short readOffset(int address) {
        return readBigEndianShort(address);
    }
}
