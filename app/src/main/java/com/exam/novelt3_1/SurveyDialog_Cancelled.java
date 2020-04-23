package com.exam.novelt3_1;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SurveyDialog_Cancelled extends Dialog {
    TextView text_survey;
    Button btn_incline, btn_speed, btn_duration;
    LinearLayout layout;

    Boolean isChanged = false, isShown = false;

    String survey_incline, survey_zone_period, survey_zone_dynamic, survey_zone_simple;
    String protocol_name;
    int survey_speed = 0;

    public String getSurvey_incline() {
        return survey_incline;
    }

    public int getSurvey_speed() {
        return survey_speed;
    }

    public String getSurvey_zone_period() {
        return survey_zone_period;
    }

    public String getSurvey_zone_dynamic() {
        return survey_zone_dynamic;
    }

    public String getSurvey_zone_simple() {
        return survey_zone_simple;
    }

    public SurveyDialog_Cancelled(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_survey_custom_protocol_cancelled);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        protocol_name = CustomProtocolResultActivity.protocol_name;

        text_survey = (TextView)findViewById(R.id.text_survey_custom_protocol);
        text_survey.setText(Html.fromHtml(protocol_name + "코스를 포기하셨습니다.<br/>어떤 부분이 많이 힘들었나요?"));

        btn_incline = (Button)findViewById(R.id.btn_survey_incline);
        btn_speed = (Button)findViewById(R.id.btn_survey_speed);
        btn_duration = (Button)findViewById(R.id.btn_survey_duration);
        layout = (LinearLayout)findViewById(R.id.layout_survey_custom_protocol_cancelled);

        btn_incline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isChanged) {
                    survey_zone_period = "운동시간_-10%";
                } else {
                    survey_incline = "경사_-2";
                }
                onShow();
            }
        });

        btn_speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isChanged) {
                    survey_zone_dynamic = "버전_-1";
                } else {
                    survey_speed = -5;
                }
                onShow();
            }
        });

        btn_duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isChanged) {
                    isChanged = true;
                    text_survey.setText("zone 지속 시간 중 어떤 부분이 많이 힘들었나요?");
                    btn_incline.setText(Html.fromHtml("전체<br/>운동시간"));
                    btn_speed.setText(Html.fromHtml("zone의<br/>다양함"));
                    btn_duration.setText(Html.fromHtml("zone의<br/>단순함"));
                } else {
                    survey_zone_simple = "버전_+1";
                    onShow();
                }
            }
        });

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isShown) {
                    dismiss();
                }
            }
        });
    }

    public void onShow() {
        text_survey.setText(Html.fromHtml("다음 프로토콜부터 해당 설문이 적용됩니다.<br/>다음 운동때는 꼭 완주해봐요!<br/>(화면을 터치하세요)"));
        btn_incline.setVisibility(View.GONE);
        btn_speed.setVisibility(View.GONE);
        btn_duration.setVisibility(View.GONE);
        isShown = true;
    }

    @Override
    public void onBackPressed() { }
}
