package com.exam.novelt3_1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class HRZoneProtocolActivity extends Activity implements ConnectionObserver {
    TextView text_time, text_id, text_patch, text_comments;
    Button btn_history, btn_setting, btn_out;
    ImageView indicator_connection, indicator_battery;
    LinearLayout layout_custom_protocol, layout_event_protocol, layout_challenge_protocol;

    String patchName, name, height, weight, gender, max_bp, min_bp, bmi, hp, local, place;
    int age;
    boolean isConnected;
    int batteryRatio;

    TimerTask task;
    Timer timer;

    Publisher publisher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr_zone_protocol);

        text_time = (TextView)findViewById(R.id.text_time_hr_zone_protocol);
        task = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 1000);

        text_id = (TextView)findViewById(R.id.text_userid_hr_zone_protocol);
        text_patch = (TextView)findViewById(R.id.text_patch_hr_zone_protocol);
        text_comments = (TextView)findViewById(R.id.text_comments_hr_zone_protocol);
        indicator_connection = (ImageView)findViewById(R.id.indicator_connection_hr_zone_protocol);
        indicator_battery = (ImageView)findViewById(R.id.indicator_battery_hr_zone_protocol);
        btn_history = (Button)findViewById(R.id.btn_history_hr_zone_protocol);
        //btn_setting = (Button)findViewById(R.id.btn_setting_hr_zone_protocol);
        btn_out = (Button)findViewById(R.id.btn_out_hr_zone_protocol);
        layout_custom_protocol = (LinearLayout)findViewById(R.id.layout_custom_protocol);
        layout_event_protocol = (LinearLayout)findViewById(R.id.layout_event_protocol);
        layout_challenge_protocol = (LinearLayout)findViewById(R.id.layout_challenge_protocol);

        Intent intent = getIntent();
        name = intent.getExtras().getString("NAME");
        height = intent.getExtras().getString("HEIGHT");
        weight = intent.getExtras().getString("WEIGHT");
        age = intent.getExtras().getInt("AGE");
        gender = intent.getExtras().getString("GENDER");
        max_bp = intent.getExtras().getString("MAX_BP");
        min_bp = intent.getExtras().getString("MIN_BP");
        bmi = intent.getExtras().getString("BMI");
        hp = intent.getExtras().getString("HP");
        local = intent.getExtras().getString("LOCAL");
        place = intent.getExtras().getString("PLACE");

        text_id.setText(name);

        btn_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        layout_custom_protocol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CustomProtocolActivity.class);
                intent.putExtra("HP", hp);
                intent.putExtra("NAME", name);
                intent.putExtra("HEIGHT", height);
                intent.putExtra("WEIGHT", weight);
                intent.putExtra("AGE", age);
                intent.putExtra("GENDER", gender);
                intent.putExtra("MAX_BP", max_bp);
                intent.putExtra("MIN_BP", min_bp);
                intent.putExtra("BMI", bmi);
                intent.putExtra("LOCAL", local);
                intent.putExtra("PLACE", place);
                startActivity(intent);
            }
        });

        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HistoryCalendarActivity.class);
                intent.putExtra("HP", hp);
                intent.putExtra("NAME", name);
                intent.putExtra("HEIGHT", height);
                intent.putExtra("WEIGHT", weight);
                intent.putExtra("AGE", age);
                intent.putExtra("GENDER", gender);
                intent.putExtra("LOCAL", local);
                intent.putExtra("PLACE", place);
                startActivity(intent);
            }
        });

        layout_event_protocol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "서비스 준비중입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        layout_challenge_protocol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "서비스 준비중입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        if(MainActivity.connected_patch_id != null && !MainActivity.connected_patch_id.equals("")) {
            patchName = MainActivity.connected_patch_id;
            text_patch.setVisibility(View.VISIBLE);
            text_patch.setText(patchName);
            if(!patchName.equals("")) {
                indicator_connection.setVisibility(View.VISIBLE);
                indicator_battery.setVisibility(View.VISIBLE);
            }
        }

        publisher = MainActivity.publisher;
        publisher.addObserver(this);
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
    public void update(String name, boolean isConnect, int battery) {
        patchName = name;
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
    public void onBackPressed() { }
}
