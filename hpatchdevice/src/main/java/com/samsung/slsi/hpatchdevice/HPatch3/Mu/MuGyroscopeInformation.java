package com.samsung.slsi.hpatchdevice.HPatch3.Mu;

import com.samsung.slsi.GyroscopeInformation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ch36.park on 2017. 6. 15..
 */

public class MuGyroscopeInformation implements GyroscopeInformation {

    private int sequenceNumber;
    private List<Integer> xValues = new ArrayList<>();
    private List<Integer> yValues = new ArrayList<>();
    private List<Integer> zValues = new ArrayList<>();


    public MuGyroscopeInformation(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }


    @Override
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public List<Integer> getXValues() {
        return xValues;
    }

    @Override
    public List<Integer> getYValues() {
        return yValues;
    }

    @Override
    public List<Integer> getZValues() {
        return zValues;
    }
}
