package com.voice.util;

import de.maxhenkel.rnnoise4j.Denoiser;
import de.maxhenkel.rnnoise4j.UnknownPlatformException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DenoiserUtils {
    private static Denoiser denoiser;
    public static byte[] shortToBytes(short[] data) {
        byte[] resultData = new byte[2 * data.length];
        int iter = 0;
        for (short sample : data) {
            resultData[iter++] = (byte) (sample & 0xff);     //低位存储，0xff是掩码操作
            resultData[iter++] = (byte) ((sample >> 8) & 0xff); //高位存储
        }
        return resultData;
    }
    public static short[] bytesToShort(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    public static Denoiser getSingleDenoiser() {
        try {
            if (denoiser == null) {
                denoiser = new Denoiser();
            }
            return denoiser;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnknownPlatformException e) {
            e.printStackTrace();
        }
        return null;
    }
}
