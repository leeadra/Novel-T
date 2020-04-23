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

public class SurveyDialog_Finished extends Dialog {
    TextView text_survey_notice, text_survey_speed, text_survey_incline, text_survey_zone, text_survey_zone_bad, text_survey_notice_end;
    Button btn_speed_slow, btn_speed_good, btn_speed_fast, btn_incline_low, btn_incline_good, btn_incline_high,
            btn_zone_easy, btn_zone_good, btn_zone_bad, btn_zone_bad_period, btn_zone_bad_variance, btn_zone_bad_simple;

    String survey_incline_low, survey_incline_high, survey_duration_short,
            survey_zone_period, survey_zone_dynamic, survey_zone_simple;
    int survey_speed_slow = 0, survey_speed_fast = 0;

    LinearLayout layout_speed, layout_incline, layout_zone, layout_zone_bad;
    String protocol_name;

    public int getSurvey_speed() {
        if(survey_speed_slow != 0) return survey_speed_slow;
        else if(survey_speed_fast != 0) return survey_speed_fast;
        else return 0;
    }

    public String getSurvey_incline() {
        if(survey_incline_low != null) return survey_incline_low;
        else if(survey_incline_high != null) return survey_incline_high;
        else return null;
    }

    public String getSurvey_zone_duration() {
        if(survey_duration_short != null) return survey_duration_short;
        else return null;
    }

    public String getSurvey_zone_bad() {
        if(survey_zone_period != null) return survey_zone_period;
        else if(survey_zone_dynamic != null) return survey_zone_dynamic;
        else if(survey_zone_simple != null) return survey_zone_simple;
        else return null;
    }

    public SurveyDialog_Finished(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_survey_custom_protocol);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        protocol_name = CustomProtocolResultActivity.protocol_name;

        text_survey_notice = (TextView)findViewById(R.id.text_survey_notice);
        text_survey_notice.setText(Html.fromHtml(protocol_name + "코스를 완주하셨습니다.<br/>프로토콜에 대한 간단한 설문이 제공되며 해당 답변에 따라<br/>프로토콜이 조절됩니다.<br/><br/>(화면을 터치하세요.)"));

        text_survey_speed = (TextView)findViewById(R.id.text_survey_speed);
        text_survey_incline = (TextView)findViewById(R.id.text_survey_incline);
        text_survey_zone = (TextView)findViewById(R.id.text_survey_zone);
        text_survey_zone_bad = (TextView)findViewById(R.id.text_survey_zone_bad);
        btn_speed_slow = (Button)findViewById(R.id.btn_survey_speed_slow);
        btn_speed_good = (Button)findViewById(R.id.btn_survey_speed_good);
        btn_speed_fast = (Button)findViewById(R.id.btn_survey_speed_fast);
        btn_incline_low = (Button)findViewById(R.id.btn_survey_incline_low);
        btn_incline_good = (Button)findViewById(R.id.btn_survey_incline_good);
        btn_incline_high = (Button)findViewById(R.id.btn_survey_incline_high);
        btn_zone_easy = (Button)findViewById(R.id.btn_survey_zone_easy);
        btn_zone_good = (Button)findViewById(R.id.btn_survey_zone_good);
        btn_zone_bad = (Button)findViewById(R.id.btn_survey_zone_bad);
        btn_zone_bad_period = (Button)findViewById(R.id.btn_survey_zone_bad_period);
        btn_zone_bad_variance = (Button)findViewById(R.id.btn_survey_zone_variance);
        btn_zone_bad_simple = (Button)findViewById(R.id.btn_survey_zone_simple);
        text_survey_notice_end = (TextView)findViewById(R.id.text_survey_notice_end);
        layout_speed = (LinearLayout)findViewById(R.id.layout_survey_speed);
        layout_incline = (LinearLayout)findViewById(R.id.layout_survey_incline);
        layout_zone = (LinearLayout)findViewById(R.id.layout_survey_zone);
        layout_zone_bad = (LinearLayout)findViewById(R.id.layout_survey_zone_bad);

        text_survey_notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_survey_notice.setVisibility(View.GONE);
                text_survey_speed.setVisibility(View.VISIBLE);
                layout_speed.setVisibility(View.VISIBLE);
            }
        });

        btn_speed_slow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                survey_speed_slow = +5;
                text_survey_speed.setVisibility(View.GONE);
                layout_speed.setVisibility(View.GONE);
                text_survey_incline.setVisibility(View.VISIBLE);
                layout_incline.setVisibility(View.VISIBLE);
            }
        });

        btn_speed_good.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_survey_speed.setVisibility(View.GONE);
                layout_speed.setVisibility(View.GONE);
                text_survey_incline.setVisibility(View.VISIBLE);
                layout_incline.setVisibility(View.VISIBLE);
            }
        });

        btn_speed_fast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                survey_speed_fast = -5;
                text_survey_speed.setVisibility(View.GONE);
                layout_speed.setVisibility(View.GONE);
                text_survey_incline.setVisibility(View.VISIBLE);
                layout_incline.setVisibility(View.VISIBLE);
            }
        });

        btn_incline_low.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                survey_incline_low = "경사_+2";
                text_survey_incline.setVisibility(View.GONE);
                layout_incline.setVisibility(View.GONE);
                text_survey_zone.setVisibility(View.VISIBLE);
                layout_zone.setVisibility(View.VISIBLE);
            }
        });

        btn_incline_good.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_survey_incline.setVisibility(View.GONE);
                layout_incline.setVisibility(View.GONE);
                text_survey_zone.setVisibility(View.VISIBLE);
                layout_zone.setVisibility(View.VISIBLE);
            }
        });

        btn_incline_high.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                survey_incline_high = "경사_-2";
                text_survey_incline.setVisibility(View.GONE);
                layout_incline.setVisibility(View.GONE);
                text_survey_zone.setVisibility(View.VISIBLE);
                layout_zone.setVisibility(View.VISIBLE);
            }
        });

        btn_zone_easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                survey_duration_short = "운동시간_+10%";
                text_survey_zone.setVisibility(View.GONE);
                layout_zone.setVisibility(View.GONE);
                text_survey_notice_end.setVisibility(View.VISIBLE);
            }
        });

        btn_zone_good.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_survey_zone.setVisibility(View.GONE);
                layout_zone.setVisibility(View.GONE);
                text_survey_notice_end.setVisibility(View.VISIBLE);
            }
        });

        btn_zone_bad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_survey_zone.setVisibility(View.GONE);
                layout_zone.setVisibility(View.GONE);
                text_survey_zone_bad.setVisibility(View.VISIBLE);
                layout_zone_bad.setVisibility(View.VISIBLE);
            }
        });

        btn_zone_bad_period.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                survey_zone_period = "운동시간_-10%";
                text_survey_zone_bad.setVisibility(View.GONE);
                layout_zone_bad.setVisibility(View.GONE);
                text_survey_notice_end.setVisibility(View.VISIBLE);
            }
        });

        btn_zone_bad_variance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                survey_zone_dynamic = "버전_-1";
                text_survey_zone_bad.setVisibility(View.GONE);
                layout_zone_bad.setVisibility(View.GONE);
                text_survey_notice_end.setVisibility(View.VISIBLE);
            }
        });

        btn_zone_bad_simple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                survey_zone_simple = "버전_+1";
                text_survey_zone_bad.setVisibility(View.GONE);
                layout_zone_bad.setVisibility(View.GONE);
                text_survey_notice_end.setVisibility(View.VISIBLE);
            }
        });

        text_survey_notice_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_survey_notice_end.setVisibility(View.GONE);
                dismiss();
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onBackPressed() { }
}
