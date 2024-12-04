package net.pistonmaster.ijvm;

public interface IMemory {
    void writeByte(int address, byte value);

    byte readByte(int address);

    default int readUnsignedByte(int address) {
        return MathHelper.maskSign(readByte(address));
    }

    default void writeBytes(int address, byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            writeByte(address + i, bytes[i]);
        }
    }

    /**
     * Read an integer from memory in big-endian format.
     *
     * @param address The address to read the integer from.
     * @return The integer read from memory.
     */
    default int readBigEndianInt(int address) {
        return (readByte(address) << 24)
                | ((readByte(address + 1) & 0xFF) << 16)
                | ((readByte(address + 2) & 0xFF) << 8)
                | (readByte(address + 3) & 0xFF);
    }

    /**
     * Write an integer to memory in big-endian format.
     *
     * @param address The address to write the integer to.
     * @param value   The integer to write to memory.
     */
    default void writeBigEndianInt(int address, int value) {
        writeByte(address, (byte) (value >> 24));
        writeByte(address + 1, (byte) (value >> 16));
        writeByte(address + 2, (byte) (value >> 8));
        writeByte(address + 3, (byte) value);
    }

    /**
     * Read a short from memory in big-endian format.
     *
     * @param address The address to read the short from.
     * @return The short read from memory.
     */
    default short readBigEndianShort(int address) {
        return (short) (((readByte(address) & 0xFF) << 8) | (readByte(address + 1) & 0xFF));
    }

    /**
     * Write a short to memory in big-endian format.
     *
     * @param address The address to write the short to.
     * @param value   The short to write to memory.
     */
    default void writeBigEndianShort(int address, short value) {
        writeByte(address, (byte) (value >> 8));
        writeByte(address + 1, (byte) value);
    }

    default int readUnsignedBigEndianShort(int address) {
        return MathHelper.maskSign(readBigEndianShort(address));
    }

    default int readVarNum(int address, boolean wide) {
        if (wide) {
            return readUnsignedBigEndianShort(address);
        }

        return readUnsignedByte(address);
    }

    default int readIndex(int address) {
        return readUnsignedBigEndianShort(address);
    }

    default int readDisp(int address) {
        return readUnsignedBigEndianShort(address);
    }

    default byte readConst(int address) {
        return readByte(address);
    }

    default short readOffset(int address) {
        return readBigEndianShort(address);
    }

    default void writeVarNum(int address, int value, boolean wide) {
        if (wide) {
            writeBigEndianShort(address, (short) value);
        } else {
            writeByte(address, (byte) value);
        }
    }

    default void writeIndex(int address, int value) {
        writeBigEndianShort(address, (short) value);
    }

    default void writeDisp(int address, int value) {
        writeBigEndianShort(address, (short) value);
    }

    default void writeConst(int address, byte value) {
        writeByte(address, value);
    }

    default void writeOffset(int address, short value) {
        writeBigEndianShort(address, value);
    }

    default void writeType(int address, ParameterType type, int value) {
        switch (type) {
            case VAR_NUM -> writeVarNum(address, value, false);
            case VAR_NUM_WIDE -> writeVarNum(address, value, true);
            case INDEX -> writeIndex(address, value);
            case DISP -> writeDisp(address, value);
            case CONST -> writeConst(address, (byte) value);
            case BYTE -> writeByte(address, (byte) value);
            case OFFSET -> writeOffset(address, (short) value);
        }
    }

    enum ParameterType {
        VAR_NUM {
            @Override
            void validate(int value) {
                if (value < 0 || value > Byte.MAX_VALUE) {
                    throw new IllegalArgumentException("Value must be between 0 and 255");
                }
            }
        },
        VAR_NUM_WIDE {
            @Override
            void validate(int value) {
                if (value < 0 || value > Short.MAX_VALUE) {
                    throw new IllegalArgumentException("Value must be between 0 and 65535");
                }
            }
        },
        INDEX {
            @Override
            void validate(int value) {
                if (value < 0 || value > Short.MAX_VALUE) {
                    throw new IllegalArgumentException("Value must be between 0 and 65535");
                }
            }
        },
        DISP {
            @Override
            void validate(int value) {
                if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                    throw new IllegalArgumentException("Value must be between -32768 and 32767");
                }
            }
        },
        CONST {
            @Override
            void validate(int value) {
                if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                    throw new IllegalArgumentException("Value must be between -128 and 127");
                }
            }
        },
        BYTE {
            @Override
            void validate(int value) {
                if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                    throw new IllegalArgumentException("Value must be between -128 and 127");
                }
            }
        },
        OFFSET {
            @Override
            void validate(int value) {
                if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                    throw new IllegalArgumentException("Value must be between -32768 and 32767");
                }
            }
        };

        abstract void validate(int value);
    }
}
