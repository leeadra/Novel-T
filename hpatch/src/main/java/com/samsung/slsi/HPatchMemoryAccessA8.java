package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 7. 10..
 */

public interface HPatchMemoryAccessA8 {
    interface ReadListener {
        void onSPatchMemoryReadA8(int address, int length, byte[] data);
    }

    void addReadA8Listener(ReadListener listener);
    void removeReadA8Listener(ReadListener listener);

    void writeA8(int address, byte[] data);
    void readA8(int address, int length);
}
