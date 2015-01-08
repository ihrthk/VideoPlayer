package zhangls.dlink;

import org.apache.http.protocol.HTTP;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Random;

public class Utils {

    public static void writeToFile(String file, byte[] data, int length)
            throws IOException {
        FileOutputStream fos = new FileOutputStream(file, true);
        fos.write(data, 0, length);
        fos.flush();
        fos.close();
    }

    public static int readDataToAudioBuffer(InputStream in, byte[] buffer)
            throws IOException {
        return readDataToAudioBuffer(in, buffer, 0, buffer.length);
    }

    public static int readDataToVideoBuffer(InputStream in, byte[] buffer)
            throws IOException {
        return readDataToVideoBuffer(in, buffer, 0, buffer.length);
    }

    // Change signed byte to unsigned integer.
    public static int b2Int(byte b) {
        // return Integer.parseInt(Integer.toBinaryString(b & 0xFF), 2);
        return (b + 0x100) % 0x100;
    }

    public static int skipAudio(InputStream in, int length) throws IOException {
        // in.skip(length);
        byte[] data = new byte[length];
        readDataToAudioBuffer(in, data);
        data = null;
        return length;
    }

    public static int skipVideo(InputStream in, int length) throws IOException {
        // in.skip(length);
        byte[] data = new byte[length];
        readDataToVideoBuffer(in, data);
        data = null;
        return length;
    }

    public static int readDataToAudioBuffer(InputStream in, byte[] buffer,
                                            int offset, int length) throws IOException {
        int readLength = 0;

        if (null == in || null == buffer) {
            throw new IOException("stream or buffer is null!!!");
        }

        while (readLength < length) {
            int recvLength = in.read(buffer, offset + readLength, length - readLength);
            if (recvLength < 0) { // IO Error
                return -1;
            }
            readLength += recvLength;
        }
        return readLength;
    }

    public static int readDataToVideoBuffer(InputStream in, byte[] buffer,
                                            int offset, int length) throws IOException {
        int readLength = 0;

        if (null == in || null == buffer) {
            throw new IOException("stream or buffer is null!!!");
        }

        while (readLength < length) {
            int recvLength = in.read(buffer, offset + readLength, length - readLength);
            if (recvLength < 0) { // IO Error
                return -1;
            }
            readLength += recvLength;
        }
        return readLength;
    }


    public static short byteArray2Short(byte[] data, int pos) {
        return (short) ((b2Int(data[pos + 1]) << 8) + b2Int(data[pos]));
    }

    public static int byteArray2Int(byte[] data) {
        return byteArray2Int(data, 0);
    }

    public static int byteArray2Int(byte[] data, int pos) {
        return (int) ((b2Int(data[pos + 3]) << 24)
                + (b2Int(data[pos + 2]) << 16) + (b2Int(data[pos + 1]) << 8) + b2Int(data[pos]));
    }

    public static byte[] int2ByteArray(int value) {
        byte[] data = new byte[4];
        data[0] = (byte) value;
        data[1] = (byte) (value >> 8);
        data[2] = (byte) (value >> 16);
        data[3] = (byte) (value >> 24);
        return data;
    }

    public static String byteArray2String(byte[] data, String spliter) {
        if (data == null)
            return null;
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x" + spliter, b));
        }
        return sb.toString();
    }

    public static String byteArray2String(byte[] data) {
        return byteArray2String(data, " ");
    }

    public static byte[] sboxCrypt(String plain, String key) {
        byte[] sbox = new byte[257];
        byte[] sbox2 = new byte[257];
        byte[] keyArray = null;

        if (plain == null || plain.length() == 0) {
            return null;
        }

        byte[] cipher = plain.getBytes();

        final String unSecuredKey = "www.nuuo.com";
        if (key != null && key.length() > 0) {
            keyArray = key.getBytes();
        } else {
            keyArray = unSecuredKey.getBytes();
        }

        for (int i = 0; i < 256; i++) {
            sbox[i] = (byte) i;
        }

        int j = 0;
        for (int i = 0; i < 256; i++) {
            if (j == keyArray.length) {
                j = 0;
            }
            sbox2[i] = keyArray[j];
            j++;
        }

        j = 0; //Initialize j
        //scramble sbox1 with sbox2
        for (int i = 0; i < 256; i++) {
            j = (j + b2Int(sbox[i]) + b2Int(sbox2[i])) % 256;
            byte temp = sbox[i];
            sbox[i] = sbox[j];
            sbox[j] = temp;
        }

        j = 0;
        int i = 0;
        for (int x = 0; x < plain.length(); x++) {
            //increment i
            i = (i + 1) % 256;
            //increment j
            j = (j + b2Int(sbox[i])) % 256;

            //Scramble SBox #1 further so encryption routine will
            //will repeat itself at great interval
            byte temp = sbox[i];
            sbox[i] = sbox[j];
            sbox[j] = temp;

            //Get ready to create pseudo random  byte for encryption key
            int t = (b2Int(sbox[i]) + b2Int(sbox[j])) % 256;

            //get the random byte
            byte k = sbox[t];

            //xor with the data and done
            cipher[x] = (byte) (cipher[x] ^ k);
        }

        return cipher;
    }

    public static String getDigest(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(message.getBytes(HTTP.UTF_8));
            return byteArray2String(digest, "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String getRandomHexString(int length) {
        Random rd = new Random(System.currentTimeMillis());
        byte[] data = new byte[length];
        rd.nextBytes(data);
        return byteArray2String(data, "");
    }

}
