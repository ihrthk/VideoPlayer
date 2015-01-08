package com.dlink.mydlinkbase;

public class ByteArrayUtil {
    public static int byteArrayIndexof(byte[] data, byte[] sub, int start) {
        int len = data.length - sub.length;
        boolean found;
        for (int i = start; i < len; i++) {
            found = true;
            if (data[i] == sub[0]) {
                for (int j = 1; j < sub.length; j++) {
                    if (data[i + j] != sub[j]) {
                        found = false;
                        break;
                    }
                }
                if (found)
                    return i;
            }
        }
        return -1;
    }


    public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        if (startIndexInclusive < 0) {
            startIndexInclusive = 0;
        }
        if (endIndexExclusive > array.length) {
            endIndexExclusive = array.length;
        }
        int newSize = endIndexExclusive - startIndexInclusive;
        if (newSize <= 0) {
            return null;
        }

        byte[] subarray = new byte[newSize];
        System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
        return subarray;
    }


    public static boolean isEmpty(byte[] temp) {
        int lenght = 0;
        for (int i = 0; i < temp.length; i++) {
            if (temp[i] == ((byte) 0x00)) {
                lenght++;
            }
        }

        if (lenght == temp.length) {
            return true;
        } else {
            return false;
        }

    }

    public static void clear(byte[] temp) {
        int lenght = temp.length;
        for (int i = 0; i < lenght; i++) {
            temp[i] = (byte) 0x00;
        }
    }
}
