package com.voice.basslib;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public interface CLib extends Library {

    boolean record_data();

    float set_record_volume_limit(float new_limit);

    int record_device_info();

    void put_stream_data(Pointer buffer, int length,NativeLong id);

    void setLength(int newLen); //length 长度 ， set 设置

    void put_data(StreamAudio.ByValue buffer);

    void out_data(StreamAudio.ByValue buffer);

    StreamAudio.ByValue poll_input_data();

    StreamAudio.ByValue poll_output_data();

    void init_stream_play(int device);

    boolean init_device(int device);

    void stop_record();

    NativeLong getStreamLevel();

    void setVolume(float volume);

    void free_buffer();

    void startThread();

    float get_avg_level();

    void set_mute(boolean newMute);
}