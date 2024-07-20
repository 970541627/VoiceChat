package com.voice.snative;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class Test {
    static double calculatePeakAndRms(short[] samples) {
        double sumOfSampleSq = 0.0;    // sum of square of normalized samples.
        double peakSample = 0.0;     // peak sample.

        for (short sample : samples) {
            double normSample = (double) sample / 32767;  // normalized the sample with maximum value.
            sumOfSampleSq += (normSample * normSample);
            if (Math.abs(sample) > peakSample) {
                peakSample = Math.abs(sample);
            }
        }

        double rms = 10 * Math.log10(sumOfSampleSq / samples.length);
        double peak = (float) (20 * Math.log10(peakSample / 32767));
        return (1.0 - (Math.abs(peak) / 93.00));
    }

    static public short bytesToShort(byte[] buffer) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(buffer[0]);
        bb.put(buffer[1]);
        return bb.getShort(0);
    }

    static double getDecibels(byte[] srcBuffer, int numBytes) {
        byte[] tempBuffer = new byte[2];
        int nSamples = numBytes / 2;
        short[] samples = new short[nSamples];  // 16-bit signed value

        for (int i = 0; i < nSamples; i++) {
            tempBuffer[0] = srcBuffer[2 * i];
            tempBuffer[1] = srcBuffer[2 * i + 1];
            samples[i] = bytesToShort(tempBuffer);
        }
        return calculatePeakAndRms(samples);
    }

    public static CLib instance = BASSNative.INSTANCE;


    public static void main(String[] args) throws Exception {
        FriendlyByteBuf friendlyByteBuf=new FriendlyByteBuf(Unpooled.buffer());
        for (int i = 0; i <5 ; i++) {
            byte[] bytes=String.valueOf(i).getBytes();
            friendlyByteBuf.writeInt(bytes.length);
            friendlyByteBuf.writeByteArray(bytes);
        }
        for (int i = 0; i <5 ; i++) {
            String s = new String(friendlyByteBuf.readByteArray(friendlyByteBuf.readInt()), Charset.defaultCharset());
            System.out.println(s);
        }
    }


}
