package com.exam.novelt3_1;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class RestDialog extends Dialog implements HeartRateObserver {
    TextView text_rest_crt_hr, text_elapsed_time;
    CheckBox checkbox_smoking, checkbox_smoking_past, checkbox_smoking_non;
    EditText edit_sitting_time;

    long startTime;
    int hr = 80, avg_sitting_time = 8, crt_hr;
    boolean isSmoking = false;
    boolean isSmoking_past = false;

    String hp, local, place;

    HRPublisher publisher;

    public RestDialog(@NonNull Context context, String hp, String local, String place) {
        super(context);
        this.hp = hp;
        this.local = local;
        this.place = place;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rest_start);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        startTime = System.currentTimeMillis();

        text_rest_crt_hr = (TextView)findViewById(R.id.text_rest_crt_hr);
        text_elapsed_time = (TextView)findViewById(R.id.text_elapsed_time_rest);
        checkbox_smoking = (CheckBox)findViewById(R.id.checkbox_smoking);
        checkbox_smoking_past = (CheckBox)findViewById(R.id.checkbox_smoking_past);
        checkbox_smoking_non = (CheckBox)findViewById(R.id.checkbox_nonsmoking);
        edit_sitting_time = (EditText)findViewById(R.id.edit_avg_sitting_time);
        edit_sitting_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_sitting_time.setText("");
            }
        });

        RestElapsedTime restElapsedTime = new RestElapsedTime();
        restElapsedTime.execute();

        publisher = MainActivity.publisher_hr;
        publisher.addObserver(this);

        onLoadLastSetting();

        checkbox_smoking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkbox_smoking_past.setChecked(false);
                checkbox_smoking_non.setChecked(false);
                checkbox_smoking.setChecked(true);
            }
        });

        checkbox_smoking_past.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkbox_smoking.setChecked(false);
                checkbox_smoking_non.setChecked(false);
                checkbox_smoking_past.setChecked(true);
            }
        });

        checkbox_smoking_non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkbox_smoking.setChecked(false);
                checkbox_smoking_past.setChecked(false);
                checkbox_smoking_non.setChecked(true);
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        isSmoking = checkbox_smoking.isChecked();
        isSmoking_past = checkbox_smoking_past.isChecked();
        if(!edit_sitting_time.getText().toString().equals("")) {
            avg_sitting_time = Integer.valueOf(edit_sitting_time.getText().toString());
        }
    }

    @Override
    public void update(int heart_rate) {
        if(heart_rate >= 50 && heart_rate < 120) {
            hr = heart_rate;
        }
        crt_hr = heart_rate;
    }

    class RestElapsedTime extends AsyncTask<Void, Void, Void> {
        int second, minute;
        String str_minute, str_second;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                onCalculate_elapsedTime();
            }
        };
        Timer timer = new Timer();

        @Override
        protected Void doInBackground(Void... voids) {
            timer.schedule(task, 0, 1000);
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            text_elapsed_time.setText(str_minute + ":" + str_second);
            if(hr != 0) {
                text_rest_crt_hr.setText(crt_hr + "");
            }
        }

        public void onCalculate_elapsedTime() {
            long now = System.currentTimeMillis();
            int time = (int) ((now - startTime) / 1000);
            second = time % 60;
            minute = time / 60 % 60;
            if(minute < 10) {
                str_minute = "0" + minute;
            } else str_minute = minute + "";
            if(second < 10) {
                str_second = "0" + second;
            } else str_second = second + "";

            publishProgress();

            if(minute == 3) {
                dismiss();
            }
            /*if(second == 30) {
                dismiss();
            }*/
        }
    }

    public void onLoadLastSetting() {
        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("LOCAL", local);
        values.put("PLACE", place);
        HttpPostDataClass_onLoadSettings post = new HttpPostDataClass_onLoadSettings("http://125.130.221.35:8001/PROC/AjaxForGetZone_Last_Data.asp", values);
        post.execute();
    }

    class HttpPostDataClass_onLoadSettings extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;
        int param_smoking, param_sitting_time;
        boolean isLoaded = false;

        public HttpPostDataClass_onLoadSettings(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values);
            if (result != null && !result.equals("")) {
                String[] tmp = result.split("<\\|>");
                if(tmp.length > 0) {
                    param_smoking = Integer.parseInt(tmp[0]);
                    param_sitting_time = Integer.parseInt(tmp[1]);
                    isLoaded = true;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(isLoaded) {
                if(param_smoking == 0) checkbox_smoking_non.setChecked(true);
                else if(param_smoking == 2) checkbox_smoking_past.setChecked(true);
                else checkbox_smoking.setChecked(true);

                edit_sitting_time.setText(String.valueOf(param_sitting_time));
            }
        }
    }

    @Override
    public void onBackPressed() { }
}
