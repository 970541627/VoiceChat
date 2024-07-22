package com.voice.basslib;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class RecordStatus extends Structure {
    public boolean status;
    public long id;

    public RecordStatus() {
        super();
    }

    public RecordStatus(boolean status, long id) {
        this.status = status;
        this.id = id;
    }

    //添加2个内部类，分别实现指针类型接口、值类型接口
    public static class ByReference extends RecordStatus implements Structure.ByReference {

        public ByReference() {
        }
    }

    public static class ByValue extends RecordStatus implements Structure.ByValue {

        public ByValue() {
        }
    }

    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[]{"status", "id"});
    }
}

