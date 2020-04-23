package com.exam.novelt3_1;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.UartConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class OneMileTestActivity extends Activity implements ConnectionObserver, HeartRateObserver {
    TextView text_time, text_userid;
    TextView text_patch, text_notice_warmingup, text_notice_hr;
    TextView text_progress, text_crtHR, text_elapsedTime, text_speed, text_calorie;
    Button btn_out;
    RelativeLayout layout_mile_test;
    ImageView img_ready_to_start, img_ready_to_stop;
    ImageView indicator_connection, indicator_battery;
    ProgressBar progress_distance;

    Animation anim1, anim2;

    static boolean isStarted = false;
    int count = 0;

    String name, height, weight, gender, max_bp, min_bp, bmi, hp, start_date, start_time, local, place;
    int age, patch_id;

    String patch_name;
    boolean isConnected = false, isNoticed = false, isTouched = false, isReadyToStop = false, isFirst = true;
    int batteryRatio, percentage;
    Publisher publisher;
    HRPublisher publisher_hr;

    static int std_fifty, std_sixty, std_seventy, std_eighty, std_ninety, std_hundred;
    int crt_hr, max_hr = 0, sum_hr = 0, cnt = 0, avg_hr_zone1, avg_hr_zone2, avg_hr_zone3, avg_hr_zone4, avg_hr_zone5;
    float max_speed = 5, sum_speed = 5, cnt_speed = 1, avg_speed_zone1, avg_speed_zone2, avg_speed_zone3, avg_speed_zone4, avg_speed_zone5;
    double calorie = 0;
    float total_speed = 5;
    int total_count_speed = 1;
    OneMile_HRZoneGraph HRChart;
    ArrayList<Integer> HRs = new ArrayList<>();
    ArrayList<Float> distances_graph = new ArrayList<>();

    long startTime, startTime_warimingup, isStartedTime;
    float distance;
    float speed = 5;
    int elapsedTime, crt_zone = 0, elapsed_time_warmingup;

    boolean isSmoking = false, isSmoking_past = false;
    int restHR, avg_sitting_time;

    Timer timer, timer_crtTime, timer_elapsed, timer_calorie, timer_warmingup;
    TimerTask task, task_crtTime, task_elapsed, task_calorie, task_warmingup;

    Physicaloid mSerial;
    private int mBaudrate           = 115200;
    private int mDataBits           = UartConfig.DATA_BITS8;
    private int mParity             = UartConfig.PARITY_NONE;
    private int mStopBits           = UartConfig.STOP_BITS1;
    private int mFlowControl        = UartConfig.FLOW_CONTROL_OFF;
    /*byte[] treadmill_start = {(byte)0xA0, (byte)0x00};
    byte[] treadmill_stop = {(byte)0xA1, (byte)0x00};
    byte[] treadmill_speed = {(byte)0xD0, (byte)((int)(Float.parseFloat(String.format("%.1f", speed)) * 10))};*/
    byte[] treadmill_start = {(byte)0xA0};
    byte[] treadmill_stop = {(byte)0xA1};
    byte[] treadmill_speed_up = {(byte)0xD0};
    byte[] treadmill_speed_down = {(byte)0xD1};

    byte[] treadmill_keepAlive = {(byte)0xB0};
    //TreadmillConnectionObserver keepAlive_treadmill;

    IntentFilter filter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_mile_test);

        text_time = (TextView)findViewById(R.id.text_time_mile_test);
        task_crtTime = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer_crtTime = new Timer();
        timer_crtTime.schedule(task_crtTime, 0, 1000);

        text_userid = (TextView)findViewById(R.id.text_userid_mile_test);
        text_patch = (TextView)findViewById(R.id.text_patch_mile_test);
        layout_mile_test = (RelativeLayout) findViewById(R.id.layout_one_mile_test);
        text_crtHR = (TextView)findViewById(R.id.text_heart_rate_miletest);
        text_elapsedTime = (TextView)findViewById(R.id.text_time_miletest);
        text_speed = (TextView)findViewById(R.id.text_speed_miletest);
        text_calorie = (TextView)findViewById(R.id.text_calorie_miletest);
        btn_out = (Button)findViewById(R.id.btn_out);
        indicator_connection = (ImageView)findViewById(R.id.indicator_connection_mile_test);
        indicator_battery = (ImageView)findViewById(R.id.indicator_battery_mile_test);
        text_progress = (TextView)findViewById(R.id.text_progress);
        img_ready_to_start = (ImageView)findViewById(R.id.img_ready_to_start);
        img_ready_to_stop = (ImageView)findViewById(R.id.img_ready_to_stop);
        progress_distance = (ProgressBar)findViewById(R.id.mile_test_progressbar);
        progress_distance.setProgress(0);
        text_notice_warmingup = (TextView)findViewById(R.id.text_notice_warmingup);
        text_notice_hr = (TextView)findViewById(R.id.text_notice_hr);

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
        patch_id = intent.getExtras().getInt("HPATCH");
        local = intent.getExtras().getString("LOCAL");
        place = intent.getExtras().getString("PLACE");

        text_userid.setText(name);

        final Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(2000);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        anim1 = new AlphaAnimation(0.0f, 1.0f);
        anim1.setDuration(2000);
        anim1.setStartOffset(0);
        anim1.setRepeatMode(Animation.REVERSE);
        anim1.setRepeatCount(Animation.INFINITE);

        anim2 = new AlphaAnimation(0.0f, 1.0f);
        anim2.setDuration(2000);
        anim2.setStartOffset(10);
        anim2.setRepeatMode(Animation.REVERSE);
        anim2.setRepeatCount(Animation.INFINITE);

        task = new TimerTask() {
            @Override
            public void run() {
                onControlSpeed(crt_hr);
                sum_speed += speed;
                cnt_speed++;
                if(max_speed < speed) max_speed = speed;

                total_speed += speed;
                total_count_speed++;
            }
        };
        timer = new Timer();

        task_elapsed = new TimerTask() {
            @Override
            public void run() {
                onCalculate_elapsedTime();
            }
        };
        timer_elapsed = new Timer();

        task_calorie = new TimerTask() {
            @Override
            public void run() {
                onCalculateCalorie();
            }
        };
        timer_calorie = new Timer();

        task_warmingup = new TimerTask() {
            @Override
            public void run() {
                onCalculate_elapsedTime_warmingup();
            }
        };
        timer_warmingup = new Timer();

        layout_mile_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                img_ready_to_start.setVisibility(View.INVISIBLE);
                if(!isStarted) {
                    text_notice_warmingup.setVisibility(View.VISIBLE);

                    isStarted = true;
                    onCalculateZones();
                    writeDataToSerial(1);

                    text_speed.setText("5.0 km/h");
                    timer.schedule(task, 1000 * 24, 8000);

                    startTime_warimingup = System.currentTimeMillis();
                    timer_warmingup.schedule(task_warmingup, 1000, 1000);

                    long now = System.currentTimeMillis();
                    isStartedTime = now;
                    Date date = new Date(now);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    start_date = sdf.format(date);
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
                    start_time = sdf1.format(date);

                    //keepAlive_treadmill = new TreadmillConnectionObserver();
                    //keepAlive_treadmill.execute();
                } else {
                    if(isNoticed) {
                        img_ready_to_start.clearAnimation();
                        img_ready_to_start.setVisibility(View.INVISIBLE);
                        isTouched = true;
                    }
                    isReadyToStop = true;
                    if(isReadyToStop) {
                        count += 1;
                        if(count == 3) {
                            img_ready_to_stop.clearAnimation();
                            img_ready_to_stop.setVisibility(View.GONE);
                            img_ready_to_start.clearAnimation();
                            img_ready_to_start.setVisibility(View.GONE);
                            writeDataToSerial(2);
                            onFinished();
                            finish();
                        }
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            count = 0;
                            isReadyToStop = false;
                        }
                    }, 2000);
                }
            }
        });

        btn_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeDataToSerial(2);
                //mSerial.close();
                finish();
            }
        });
        publisher = MainActivity.publisher;
        publisher.addObserver(this);

        if(MainActivity.connected_patch_id != null && !MainActivity.connected_patch_id.equals(" ")) {
            patch_name = MainActivity.connected_patch_id;
            text_patch.setVisibility(View.VISIBLE);
            text_patch.setText(patch_name);
            if(!patch_name.equals("")) {
                indicator_connection.setVisibility(View.VISIBLE);
                indicator_battery.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(getApplicationContext(), "센서 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
        }

        publisher_hr = MainActivity.publisher_hr;
        publisher_hr.addObserver(this);
        HRChart = (OneMile_HRZoneGraph)findViewById(R.id.HRGraph_mile_test);
        DPIHelper helper = new DPIHelper(getApplicationContext(), getWindowManager().getDefaultDisplay());
        HRChart.setTextSize((int)helper.getXDPI() / 16);

        mSerial = new Physicaloid(getApplicationContext());
        filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbRecevier, filter);

        timer_start_speed = new Timer();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onRest();
    }

    private void onRest() {
        final RestDialog dialog = new RestDialog(OneMileTestActivity.this, hp, local, place);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dlg) {
                int crtHR = dialog.hr;
                if(crtHR != 0) {
                    restHR = crtHR;
                }
                isSmoking = dialog.isSmoking;
                isSmoking_past = dialog.isSmoking_past;
                avg_sitting_time = dialog.avg_sitting_time;
                img_ready_to_start.setVisibility(View.VISIBLE);
                //openUsbSerial();
            }
        });
    }

    public void onCalculateZones() {
        std_fifty = (int)((220-age-restHR) * 0.5 + restHR);
        std_sixty = (int)((220-age-restHR) * 0.6 + restHR);
        std_seventy = (int)((220-age-restHR) * 0.7 + restHR);
        std_eighty = (int)((220-age-restHR) * 0.8 + restHR);
        std_ninety = (int)((220-age-restHR) * 0.9 + restHR);
        std_hundred = (int)((220-age-restHR) * 1.0 + restHR);
    }

    public void onCalculate_elapsedTime_warmingup() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(elapsed_time_warmingup < 120) {
                    long now = System.currentTimeMillis();
                    int time = (int) ((now - startTime_warimingup) / 1000);
                    elapsed_time_warmingup = time;
                    int second = time % 60;
                    int minute = time / 60 % 60;
                    String str_minute, str_second;
                    if(minute < 10) {
                        str_minute = "0" + minute;
                    } else str_minute = minute + "";
                    if(second < 10) {
                        str_second = "0" + second;
                    } else str_second = second + "";
                    text_notice_warmingup.setText(Html.fromHtml("2분 워밍업을 시작합니다. " + str_minute + " : " + str_second));
                }
            }
        });
    }

    public void onCalculate_elapsedTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                int time = (int) ((now - startTime) / 1000);
                elapsedTime = time;
                int second = time % 60;
                int minute = time / 60 % 60;
                String str_minute, str_second;
                if(minute < 10) {
                    str_minute = "0" + minute;
                } else str_minute = minute + "";
                if(second < 10) {
                    str_second = "0" + second;
                } else str_second = second + "";
                text_elapsedTime.setText(str_minute + " : " + str_second);

                distance += speed / 3600;

                percentage = (int)(distance / 0.016);

                if(!isNoticed && distance >= 0.96) {
                    img_ready_to_start.setVisibility(View.VISIBLE);
                    img_ready_to_start.setImageResource(R.drawable.img_notice_speed);
                    img_ready_to_start.startAnimation(anim1);
                    isNoticed = true;
                }
                onCalculateCalorie();
            }
        });
    }

    private void onCalculateCalorie() {
        double coef = 0;
        float weight_ = Float.parseFloat(weight);

        if(speed < 6.5) coef = 0.001;
        else if(speed >= 6.5 && speed < 9) coef = 0.002;
        else if(speed >= 9 && speed < 12) coef = 0.003;
        else if(speed >= 12 && speed < 16) coef = 0.004;
        else if(speed >= 16 && speed < 19) coef = 0.005;
        else if(speed >= 19 && speed < 21) coef = 0.006;

        calorie += coef * weight_;

        text_calorie.setText((int)calorie + " kcal");
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

    BroadcastReceiver mUsbRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                if(!mSerial.isOpened()) {
                    openUsbSerial();
                }
            } else if(action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                Toast.makeText(getApplicationContext(), "트레드밀과 연결이 끊어졌습니다. 다시 실행해 주세요.", Toast.LENGTH_SHORT).show();
                mSerial.close();
                finish();
            }
        }
    };

    boolean isConnectedOTG = false;
    public void openUsbSerial() {
        if(mSerial == null) {
            Toast.makeText(getApplicationContext(), "트레드밀과 연결할 수 없습니다. 다시 실행해 주세요.", Toast.LENGTH_SHORT).show();
            //isConnectedOTG = false;
            return;
        }
        if(!mSerial.isOpened()) {
            if(!mSerial.open()) {
                Toast.makeText(getApplicationContext(), "트레드밀과의 연결 상태를 확인하세요.", Toast.LENGTH_SHORT).show();
                //isConnectedOTG = false;
                return;
            } else {
                boolean dtrOn=false;
                boolean rtsOn=false;
                if(mFlowControl == UartConfig.FLOW_CONTROL_ON) {
                    dtrOn = true;
                    rtsOn = true;
                }
                mSerial.setConfig(new UartConfig(mBaudrate, mDataBits, mStopBits, mParity, dtrOn, rtsOn));
                if(!isConnectedOTG) {
                    Toast.makeText(this, "트레드밀과 연결되었습니다.", Toast.LENGTH_SHORT).show();
                    isConnectedOTG = true;
                }
            }
        }
    }

    int count_speed_up_start = 0;
    TimerTask task_start_speed = new TimerTask() {
        @Override
        public void run() {
            //mSerial.write(treadmill_speed_up);
            count_speed_up_start += 1;
            if(count_speed_up_start == 21) {
                if(task_start_speed != null) task_start_speed.cancel();
                if(timer_start_speed != null) timer_start_speed.cancel();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_out.setVisibility(View.VISIBLE);
                        img_ready_to_stop.setVisibility(View.VISIBLE);
                        img_ready_to_stop.startAnimation(anim1);
                    }
                });
            }
        }
    };
    Timer timer_start_speed;
    private void writeDataToSerial(int id) {
        switch(id) {
            case 1:
                //mSerial.write(treadmill_start);         // 이고진 트레드밀 - 시작 속도 1.0, 은성 트레드밀 - 시작 속도 0.8
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(timer_start_speed != null) timer_start_speed.schedule(task_start_speed, 0, 100);
                    }
                }, 3500);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_speed.setText(String.format("%.1f", speed) + " km/h");
                    }
                });
                break;
            case 2:
                //mSerial.write(treadmill_stop);
                speed = 0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_speed.setText(String.format("%.1f", speed) + " km/h");
                    }
                });
                break;
            case 3:
                //mSerial.write(treadmill_speed_up);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_speed.setText(String.format("%.1f", speed) + " km/h");
                    }
                });
                break;
            case 4:
                //mSerial.write(treadmill_speed_down);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_speed.setText(String.format("%.1f", speed) + " km/h");
                    }
                });
                break;
        }
    }

    @Override
    public void update(String name, boolean isConnect, int battery) {
        patch_name = name;
        isConnected = isConnect;
        batteryRatio = battery;

        if(patch_name != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_patch.setVisibility(View.VISIBLE);
                    text_patch.setText(patch_name);
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

    WriteData writeData;
    int total_hr, total_count_hr = 1;

    TimerTask task_beforeStart = new TimerTask() {
        @Override
        public void run() {
            if(crt_hr < std_fifty && !started) {
                if(speed < 15.8) {
                    speed += 0.2;
                    writeDataToSerial(3);
                }
            }
            if(crt_hr >= std_fifty) started = true;
        }
    };

    Timer timer_beforeStart = new Timer();
    boolean onBeforeStart = false, onSpeedControlStarted = false;

    @Override
    public void update(int heart_rate) {
        crt_hr = heart_rate;
        if(isStarted) {
            if(writeData == null) {
                writeData = new WriteData();
                writeData.execute();
                writeData = null;
            }
        }

        if(!onBeforeStart && isStarted) {
            timer_beforeStart.schedule(task_beforeStart, 1000 * 120, 8000);
            onBeforeStart = true;
        }

        if(started) {
            task_beforeStart = null;
            timer_beforeStart = null;

            HRs.add(heart_rate);
            distances_graph.add(distance);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text_crtHR.setText(crt_hr + " bpm");
                if(elapsed_time_warmingup == 120) {
                    text_notice_warmingup.clearAnimation();
                    text_notice_warmingup.setVisibility(View.INVISIBLE);
                    text_notice_hr.setVisibility(View.VISIBLE);
                    text_notice_hr.startAnimation(anim2);
                    text_notice_hr.setText(Html.fromHtml("심박수가 <font color=\"#ffff00\">" + std_fifty + "</font> 이 되면 테스트가 시작됩니다."));
                    elapsed_time_warmingup = 0;
                }
                if(started) {
                    HRChart.add(crt_hr, distance);
                    progress_distance.setProgress(percentage);
                    text_progress.setText(String.format("%.2f", distance) + " km");
                }
            }
        });
        if(max_hr < crt_hr) max_hr = crt_hr;
        sum_hr += crt_hr;
        cnt++;
        total_hr += crt_hr;
        total_count_hr++;
    }

    boolean isFinished = false, started = false, onGoing = false;
    int startTime_zone_1, startTime_zone_2, startTime_zone_3, startTime_zone_4, startTime_zone_5, finishTime_zone_5;
    boolean isZone2 = false, isZone3 = false, isZone4 = false, isZone5 = false;

    public void onControlSpeed(int crt_hr) {
        if(!isFinished && crt_hr != 0 && started) {
            crt_zone = 1;
            if(!onGoing) {
                startTime = System.currentTimeMillis();
                startTime_zone_1 = (int)((startTime - isStartedTime) / 1000);
                timer_elapsed.schedule(task_elapsed, 0, 1000);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_notice_hr.setVisibility(View.INVISIBLE);
                        text_notice_hr.clearAnimation();
                    }
                });
                onGoing = true;
            }
            if(distance >= 0 && distance < 0.32) {
                if(crt_hr < std_fifty) {
                    if(speed < 15.8) {
                        speed += 0.2;
                        writeDataToSerial(3);
                    }
                } else if(crt_hr > std_sixty) {
                    if(speed > 1) {
                        speed -= 0.2;
                        writeDataToSerial(4);
                    }
                }
            } else if(distance >= 0.32 && distance < 0.64) {
                if(!isZone2) {
                    long now = System.currentTimeMillis();
                    startTime_zone_2 = (int)((now - isStartedTime) / 1000);
                    isZone2 = true;
                    avg_speed_zone1 = (sum_speed / cnt_speed);
                    sum_speed = 0;
                    cnt_speed = 0;
                    avg_hr_zone1 = (sum_hr / cnt);
                    sum_hr = 0;
                    cnt = 0;
                }
                crt_zone = 2;
                if(crt_hr < std_sixty) {
                    if(speed < 15.8) {
                        speed += 0.2;
                        writeDataToSerial(3);
                    }
                } else if(crt_hr > std_seventy) {
                    if(speed > 1) {
                        speed -= 0.2;
                        writeDataToSerial(4);
                    }
                }
            } else if(distance >= 0.64 && distance < 0.96) {
                if(!isZone3) {
                    long now = System.currentTimeMillis();
                    startTime_zone_3 = (int)((now - isStartedTime) / 1000);
                    isZone3 = true;
                    avg_speed_zone2 = (sum_speed / cnt_speed);
                    sum_speed = 0;
                    cnt_speed = 0;
                    avg_hr_zone2 = (sum_hr / cnt);
                    sum_hr = 0;
                    cnt = 0;
                }
                crt_zone = 3;
                if(crt_hr < std_seventy) {
                    if(speed < 15.8) {
                        speed += 0.2;
                        writeDataToSerial(3);
                    }
                } else if(crt_hr > std_eighty) {
                    if(speed > 1) {
                        speed -= 0.2;
                        writeDataToSerial(4);
                    }
                }
            } else if(distance >= 0.96 && distance < 1.28) {
                if(!isZone4) {
                    long now = System.currentTimeMillis();
                    startTime_zone_4 = (int)((now - isStartedTime) / 1000);
                    isZone4 = true;
                    avg_speed_zone3 = (sum_speed / cnt_speed);
                    sum_speed = 0;
                    cnt_speed = 0;
                    avg_hr_zone3 = (sum_hr / cnt);
                    sum_hr = 0;
                    cnt = 0;
                }
                crt_zone = 4;
                if(!isTouched) {
                    if(crt_hr < std_eighty) {
                        if(speed < 15.8) {
                            speed += 0.2;
                            writeDataToSerial(3);
                        }
                    } else if(crt_hr > std_ninety) {
                        if(speed > 1) {
                            speed -= 0.2;
                            writeDataToSerial(4);
                        }
                    }
                }
            } else if(distance >= 1.28 && distance < 1.6) {
                if(!isZone5) {
                    long now = System.currentTimeMillis();
                    startTime_zone_5 = (int)((now - isStartedTime) / 1000);
                    isZone5 = true;
                    avg_speed_zone4 = (sum_speed / cnt_speed);
                    sum_speed = 0;
                    cnt_speed = 0;
                    avg_hr_zone4 = (sum_hr / cnt);
                    sum_hr = 0;
                    cnt = 0;
                }
                crt_zone = 5;
                if(!isTouched) {
                    if(crt_hr < std_ninety) {
                        if(speed < 15.8) {
                            speed += 0.2;
                            writeDataToSerial(3);
                        }
                    } else if(crt_hr > std_hundred) {
                        if(speed > 1) {
                            speed -= 0.2;
                            writeDataToSerial(4);
                        }
                    }
                }
            } else if(distance >= 1.6) {
                crt_zone = 5;
                avg_speed_zone5 = (sum_speed / cnt_speed);
                avg_hr_zone5 = (sum_hr / cnt);

                long now = System.currentTimeMillis();
                finishTime_zone_5 = (int)((now - isStartedTime) / 1000);
                isFinished = true;
                writeDataToSerial(2);
                onFinished();
                finish();
            }
        }
    }

    public void onFinished() {
        mSerial.close();
        started = false;
        if(writeData != null)
            writeData = null;

        float[] tmp = new float[distances_graph.size()];
        for(int i = 0; i < distances_graph.size(); i++) {
            tmp[i] = distances_graph.get(i);
        }

        Intent intent = new Intent(getApplicationContext(), OneMileTestResultActivity.class);
        intent.putExtra("NAME", name);
        intent.putExtra("HEIGHT", height);
        intent.putExtra("WEIGHT", weight);
        intent.putExtra("AGE", age);
        intent.putExtra("GENDER", gender);
        intent.putExtra("MAX_BP", max_bp);
        intent.putExtra("MIN_BP", min_bp);
        intent.putExtra("BMI", bmi);
        intent.putExtra("HP", hp);
        intent.putExtra("THIS_DATE", start_date);
        intent.putExtra("START_TIME", start_time);

        intent.putExtra("distance", distance);
        intent.putExtra("percentage", percentage);
        intent.putExtra("max_hr", max_hr);
        intent.putExtra("max_speed", max_speed);
        intent.putExtra("elapsed_time", text_elapsedTime.getText());
        intent.putExtra("elapsedTime", elapsedTime);
        intent.putExtra("calorie", String.valueOf(calorie));
        intent.putExtra("REST_HR", restHR);
        intent.putExtra("AVG_HR", (total_hr / total_count_hr));
        intent.putExtra("AVG_SPEED", (total_speed / total_count_speed));

        intent.putExtra("isSmoking", isSmoking);
        intent.putExtra("isSmoking_past", isSmoking_past);
        intent.putExtra("sittingTime", avg_sitting_time);

        intent.putExtra("LOCAL", local);
        intent.putExtra("PLACE", place);

        intent.putExtra("ZONE_START", startTime_zone_1);
        if((startTime_zone_2 - startTime_zone_1) > 0) intent.putExtra("ZONE_TIME1", (startTime_zone_2 - startTime_zone_1));
        else intent.putExtra("ZONE_TIME1", 0);
        if((startTime_zone_3 - startTime_zone_2) > 0) intent.putExtra("ZONE_TIME2", (startTime_zone_3 - startTime_zone_2));
        else intent.putExtra("ZONE_TIME2", 0);
        if((startTime_zone_4 - startTime_zone_3) > 0) intent.putExtra("ZONE_TIME3", (startTime_zone_4 - startTime_zone_3));
        else intent.putExtra("ZONE_TIME3", 0);
        if((startTime_zone_5 - startTime_zone_4) > 0) intent.putExtra("ZONE_TIME4", (startTime_zone_5 - startTime_zone_4));
        else intent.putExtra("ZONE_TIME4", 0);
        if((finishTime_zone_5 - startTime_zone_5) > 0) intent.putExtra("ZONE_TIME5", (finishTime_zone_5 - startTime_zone_5));
        else intent.putExtra("ZONE_TIME5", 0);

        intent.putExtra("AVG_SPEED1", avg_speed_zone1);
        intent.putExtra("AVG_SPEED2", avg_speed_zone2);
        intent.putExtra("AVG_SPEED3", avg_speed_zone3);
        intent.putExtra("AVG_SPEED4", avg_speed_zone4);
        intent.putExtra("AVG_SPEED5", avg_speed_zone5);

        intent.putExtra("AVG_HR1", avg_hr_zone1);
        intent.putExtra("AVG_HR2", avg_hr_zone2);
        intent.putExtra("AVG_HR3", avg_hr_zone3);
        intent.putExtra("AVG_HR4", avg_hr_zone4);
        intent.putExtra("AVG_HR5", avg_hr_zone5);

        intent.putExtra("HPATCH", patch_id);
        intent.putExtra("CRT_ZONE", crt_zone);

        Bundle bundle = new Bundle();
        intent.putExtra("bundle", bundle);
        intent.putExtra("HRs", HRs);
        intent.putExtra("distances", tmp);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        task = null;
        timer = null;
        task_elapsed = null;
        timer_elapsed = null;
        task_crtTime = null;
        timer_crtTime = null;
        task_calorie = null;
        timer_calorie = null;
        task_start_speed = null;
        timer_start_speed = null;
        task_beforeStart = null;
        timer_beforeStart = null;
        isStarted = false;
        writeData = null;
        publisher_hr.removeObserver(this);
        publisher.removeObserver(this);

        unregisterReceiver(mUsbRecevier);
        super.onDestroy();
    }

    /*class TreadmillConnectionObserver extends AsyncTask<Void, Void, Void> {
        Timer timer;
        TimerTask task;

        public TreadmillConnectionObserver() {
            task = new TimerTask() {
                @Override
                public void run() {
                    mSerial.write(treadmill_keepAlive);
                }
            };
            timer = new Timer();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(mSerial != null && mSerial.open()) {
                timer.schedule(task, 0, 1000);
            }
            return null;
        }
    }*/

    private void onWriteDataToText_HR(final String folder, final String file, final float distance, final int hr) {
        try {
            File dir = new File(folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/NovelT/1mile/" + file, true);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(String.format("%.3f", distance) + "\t" + hr + "\t" + String.format("%.1f", speed) + "\t" + elapsedTime + "\t" + crt_zone);
            writer.newLine();
            writer.flush();
            writer.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class WriteData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            String[] time = start_time.split("_");
            onWriteDataToText_HR(Environment.getExternalStorageDirectory() + "/NovelT/1mile/", name + "_" + time[0] + time[1] + time[2] + "_" + time[3] + time[4] + ".txt", distance, crt_hr);
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mUsbRecevier, filter);
    }

    @Override
    public void onBackPressed() { }
}
