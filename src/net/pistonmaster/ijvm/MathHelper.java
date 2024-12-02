package ijvm;

public class MathHelper {
    public static int maskSign(byte value) {
        return value & 0xFF;
    }

    public static int maskSign(short value) {
        return value & 0xFFFF;
    }
}
