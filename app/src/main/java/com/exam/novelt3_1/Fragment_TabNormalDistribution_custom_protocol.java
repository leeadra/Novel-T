package com.exam.novelt3_1;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Fragment_TabNormalDistribution_custom_protocol extends Fragment {
    public Fragment_TabNormalDistribution_custom_protocol() { }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_normal_distribution_custom_protocol, container, false);
    }
}
