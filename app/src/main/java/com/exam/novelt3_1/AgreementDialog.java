package com.exam.novelt3_1;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class AgreementDialog extends Dialog {
    RadioGroup radioGroup;
    public RadioButton radio_agree, radio_disagree;

    boolean isChecked = false;

    public AgreementDialog(@NonNull Context context, boolean checked) {
        super(context);
        this.isChecked = checked;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_agreement);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        radioGroup = (RadioGroup)findViewById(R.id.radioGroup_agreement);
        radio_agree = (RadioButton)findViewById(R.id.radio_agree);
        radio_disagree = (RadioButton)findViewById(R.id.radio_disagree);

        if(isChecked) {
            radio_agree.setChecked(true);
            radio_agree.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#ffff00")));
            radio_disagree.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#807f7f")));
        } else {
            radio_disagree.setChecked(true);
            radio_disagree.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#ffff00")));
            radio_agree.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#807f7f")));
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radio_agree) {
                    radio_agree.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#ffff00")));
                    radio_disagree.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#807f7f")));
                } else if(checkedId == R.id.radio_disagree) {
                    radio_disagree.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#ffff00")));
                    radio_agree.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#807f7f")));
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                }, 500);
            }
        });
    }

    @Override
    public void onBackPressed() { }
}
