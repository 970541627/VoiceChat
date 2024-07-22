package com.voice.basslib;


import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class StreamAudio extends Structure {
    public Pointer x;
    public int y;
    public NativeLong id;
    public StreamAudio(){
        super();
    }
    public StreamAudio(Pointer x,int y,NativeLong id){
        this.x=x;
        this.y=y;
        this.id=id;
    }

    //添加2个内部类，分别实现指针类型接口、值类型接口
    public static class ByReference extends StreamAudio implements Structure.ByReference {

        public ByReference() {
        }
    }
    public static class ByValue extends StreamAudio implements Structure.ByValue{

        public ByValue() {
        }
    }
    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[]{"x", "y","id"});
    }
}
