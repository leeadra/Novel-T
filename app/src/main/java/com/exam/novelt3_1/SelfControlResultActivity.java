package com.exam.novelt3_1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SelfControlResultActivity extends Activity implements ConnectionObserver {
    TextView text_time, text_id, text_patch;
    TextView text_elapsedTime, text_max_incline, text_max_hr, text_distance, text_max_speed, text_calorie;
    ImageView indicator_connection, indicator_battery;
    Button btn_out;
    Custom_HRZoneGraph HRChart;
    TextView text_notice_band;

    Timer timer_crtTime;
    TimerTask task_crtTime;

    String name, height, weight, gender, hp, local, place;
    int age;

    String patchName;
    Publisher publisher;
    boolean isConnected;
    int batteryRatio;

    String elapsed_time;
    int max_hr, max_incline;
    float distance, max_speed;
    double calorie;

    ArrayList<Integer> HRs = new ArrayList<>();
    long[] times = new long[] {};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_control_result);

        text_time = (TextView)findViewById(R.id.text_time_self_control_result);
        task_crtTime = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer_crtTime = new Timer();
        timer_crtTime.schedule(task_crtTime, 0, 1000);

        text_id = (TextView)findViewById(R.id.text_userid_self_control_result);
        text_patch = (TextView)findViewById(R.id.text_patch_self_control_result);
        indicator_connection = (ImageView)findViewById(R.id.indicator_connection_self_control_result);
        indicator_battery = (ImageView)findViewById(R.id.indicator_battery_self_control_result);
        btn_out = (Button)findViewById(R.id.btn_out_self_control_result);

        text_elapsedTime = (TextView)findViewById(R.id.text_elapsed_time_self_control_result);
        text_max_hr = (TextView)findViewById(R.id.text_max_hr_self_control_result);
        text_distance = (TextView)findViewById(R.id.text_distance_self_control_result);
        text_max_speed = (TextView)findViewById(R.id.text_max_speed_self_control_result);
        text_calorie = (TextView)findViewById(R.id.text_calorie_self_control_result);
        text_max_incline = (TextView)findViewById(R.id.text_max_incline_self_control_result);

        HRChart = (Custom_HRZoneGraph)findViewById(R.id.HRGraph_self_control_result);
        text_notice_band = (TextView)findViewById(R.id.text_notice_band_self_control_result);

        Intent intent = getIntent();
        name = intent.getExtras().getString("NAME");
        height = intent.getExtras().getString("HEIGHT");
        weight = intent.getExtras().getString("WEIGHT");
        age = intent.getExtras().getInt("AGE");
        gender = intent.getExtras().getString("GENDER");
        hp = intent.getExtras().getString("HP");
        local = intent.getExtras().getString("LOCAL");
        place = intent.getExtras().getString("PLACE");

        elapsed_time = intent.getExtras().getString("ELAPSED_TIME");
        max_hr = intent.getExtras().getInt("MAX_HR");
        distance = intent.getExtras().getFloat("DISTANCE");
        max_speed = intent.getExtras().getFloat("MAX_SPEED");
        calorie = intent.getExtras().getDouble("CALORIE");
        max_incline = intent.getExtras().getInt("MAX_INCLINE");

        Bundle b = intent.getExtras();
        HRs = b.getIntegerArrayList("HRs");
        times = b.getLongArray("times");

        text_id.setText(name);

        text_elapsedTime.setText(elapsed_time);
        text_max_hr.setText(max_hr + " bpm");
        text_distance.setText(String.format("%.2f", distance) + " km");
        text_max_speed.setText(String.format("%.1f", max_speed) + " km/h");
        text_calorie.setText((int)calorie + " kcal");
        text_max_incline.setText(String.valueOf(max_incline));

        publisher = MainActivity.publisher;
        publisher.addObserver(this);

        btn_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        DPIHelper helper = new DPIHelper(getApplicationContext(), getWindowManager().getDefaultDisplay());
        HRChart.setTextSize((int)helper.getXDPI() / 16);
        HRChart.addAll(HRs, times);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void calc_time() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd. HH:mm:ss");
                Date date = new Date(now);
                String time = sdf.format(date);
                text_time.setText(time);
            }
        });
    }

    @Override
    public void update(String patch_name, boolean isConnect, int battery) {
        patchName = patch_name;
        isConnected = isConnect;
        batteryRatio = battery;

        if(patchName != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_patch.setVisibility(View.VISIBLE);
                    text_patch.setText(patchName);
                    indicator_connection.setVisibility(View.VISIBLE);
                    onUpdateBatteryIndicator();
                    text_notice_band.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_patch.setVisibility(View.INVISIBLE);
                    text_patch.setText("");
                    indicator_connection.setVisibility(View.INVISIBLE);
                    indicator_battery.setVisibility(View.INVISIBLE);
                    text_notice_band.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void onUpdateBatteryIndicator() {
        if(batteryRatio > 75) {
            indicator_battery.setImageResource(R.drawable.battery_indicator_full);
        } else if(batteryRatio > 50) {
            indicator_battery.setImageResource(R.drawable.battery_indicator_75p);
        } else if(batteryRatio > 25) {
            indicator_battery.setImageResource(R.drawable.battery_indicator_50p);
        } else if(batteryRatio > 10) {
            indicator_battery.setImageResource(R.drawable.battery_indicator_25p);
        } else {
            indicator_battery.setImageResource(R.drawable.battery_indicator_low);
        }
        indicator_battery.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        SelfControlActivity.isStarted = false;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() { }
}