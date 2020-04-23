package com.exam.novelt3_1;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class RestDialog_stop extends Dialog implements HeartRateObserver {
    TextView text_rest_crt_hr, text_elapsed_time;
    long startTime;
    int hr, restHR_1min, restHR_2min, restHR_3min;

    HRPublisher publisher;

    public RestDialog_stop(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rest_stop);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        startTime = System.currentTimeMillis();

        text_rest_crt_hr = (TextView)findViewById(R.id.text_rest_crt_hr_stop);
        text_elapsed_time = (TextView)findViewById(R.id.text_elapsed_time_rest_stop);

        RestElapsedTime restElapsedTime = new RestElapsedTime();
        restElapsedTime.execute();

        publisher = MainActivity.publisher_hr;
        publisher.addObserver(this);
    }

    @Override
    public void update(int heart_rate) {
        hr = heart_rate;
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
                text_rest_crt_hr.setText(hr + "");
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

            if(minute == 1 && second == 0) {
                restHR_1min = hr;
            } else if(minute == 2 && second == 0) {
                restHR_2min = hr;
            } else if(minute == 3 && second == 0) {
                restHR_3min = hr;
            }

            publishProgress();

            if(minute == 3) {
                dismiss();
            }
        }
    }

    @Override
    public void onBackPressed() { }
}