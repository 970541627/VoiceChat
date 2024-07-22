package com.voice.basslib;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class StreamLevel extends Structure{
    public StreamLevel(){
        super();
    }
    public short left;
    public short right;
    //添加2个内部类，分别实现指针类型接口、值类型接口
    public static class ByReference extends StreamLevel implements Structure.ByReference {

        public ByReference() {
        }
    }
    public static class ByValue extends StreamLevel implements Structure.ByValue{

        public ByValue() {
        }
    }
    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[]{"left", "right"});
    }
}
